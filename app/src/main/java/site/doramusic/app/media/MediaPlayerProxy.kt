package site.doramusic.app.media

import android.media.MediaPlayer
import android.os.SystemClock
import dora.util.IoUtils
import dora.util.TextUtils
import java.io.*
import java.net.*
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/**
 * 本地播放器代理，用于边下边播，仅供参考，没有使用到。
 */
class MediaPlayerProxy private constructor(private var audioCachePath: String, // 是否缓存播放文件
                                           private var needCacheAudio: Boolean = true) : MediaPlayer() {

    private var localProxyPort = 9090
    private var bufferingMusicUrlList: MutableList<String> = ArrayList() // 正在缓存的网络音乐地址
    private var onCachedProgressUpdateListener: OnCachedProgressUpdateListener? = null
    private var latestProxyId: Long = 0 // 最新的代理ID
    private var currProxyId: Long = 0 // 当前代理ID
    private var localServerSocket: ServerSocket? = null
    private var remoteHostAndPort = "" // 这个用来到时替换本地地址的
    private var remoteAddress: SocketAddress? = null
    private var socketRequestInfoStr = "" // 音乐的远程socket请求地址
    private var remoteUrl = "" // 远程音乐地址
    private val musicKey = "" // 音乐对象的key
    private var currPlayDegree = 0 // 当前音乐播放进度
    private var cachedFileLength: Long = 0 // 已缓存的文件长度
    private var fileTotalLength: Long = 0 // 要缓存的文件总长度
    private var currMusicCachedProgress = 0 // 当前的音乐缓冲值（seek bar上的缓冲值）
    private var musicControlInterface: MusicControlInterface? = null

    constructor() : this("", false)

    fun setAudioCachePath(path: String) {
        audioCachePath = path
    }

    fun setNeedCacheAudio(isNeedCacheAudio: Boolean) {
        needCacheAudio = isNeedCacheAudio
    }

    /**
     * 把网络URL转为本地URL，127.0.0.1替换网络域名,且设置远程的socket连接地址。
     *
     * @param url 网络URL
     * @return 本地URL
     */
    fun getLocalURLAndSetRemoteSocketAddress(url: String): String {
        return try {
            remoteUrl = url
            if (needCacheAudio) {
                bufferingMusicUrlList.add(remoteUrl)
            }
            var localProxyUrl = ""
            val originalURI = URI.create(url)
            val remoteHost = originalURI.host
            if (TextUtils.isNotEmpty(remoteHost)) {
                if (originalURI.port != -1) { //URL带Port
                    Thread {
                        remoteAddress = InetSocketAddress(
                            remoteHost,
                            originalURI.port
                        )
                    }.start()
                    localProxyUrl = url.replace(remoteHost + ":" + originalURI.port,
                            "$LOCALHOST:$localProxyPort")
                    remoteHostAndPort = remoteHost + ":" + originalURI.port
                } else { //URL不带Port
                    if (TextUtils.isNotEmpty(remoteHost)) {
                        Thread(Runnable {
                            remoteAddress = InetSocketAddress(remoteHost, HTTP_PORT) //使用80端口
                        }).start()
                        localProxyUrl = url.replace(remoteHost, "$LOCALHOST:$localProxyPort")
                        remoteHostAndPort = remoteHost
                    }
                }
            }
            localProxyUrl
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获得真实的socket请求信息。
     *
     * @param localSocket
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTrueSocketRequestInfo(localSocket: Socket) {
        val inLocalSocket = localSocket.getInputStream()
        var trueSocketRequestInfoStr = "" // 保存MediaPlayer的真实HTTP请求
        val localRequest = ByteArray(1024)
        while (inLocalSocket.read(localRequest) != -1) {
            val str = String(localRequest)
            trueSocketRequestInfoStr += str
            if (trueSocketRequestInfoStr.contains("GET")
                    && trueSocketRequestInfoStr.contains("\r\n\r\n")) {
                // 把request中的本地ip改为远程ip
                trueSocketRequestInfoStr = trueSocketRequestInfoStr
                        .replace("$LOCALHOST:$localProxyPort", remoteHostAndPort)
                socketRequestInfoStr = trueSocketRequestInfoStr
                // 如果用户拖动了进度条，因为拖动了滚动条还有Range则表示本地歌曲还未缓存完，不再保存
                if (trueSocketRequestInfoStr.contains("Range")) {
                    needCacheAudio = false
                }
                break
            }
        }
    }

    /**
     * 通过远程socket连接远程请求，并返回remote_socket。
     *
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendRemoteRequest(): Socket { // 创建远程socket用来请求网络数据
        val remoteSocket = Socket()
        remoteSocket.connect(remoteAddress, SOCKET_TIMEOUT)
        remoteSocket.getOutputStream().write(socketRequestInfoStr.toByteArray())
        remoteSocket.getOutputStream().flush()
        return remoteSocket
    }

    /**
     * 处理真实请求信息, 把网络服务器的反馈发到MediaPlayer，网络服务器->代理服务器->MediaPlayer。
     *
     * @param remoteSocket
     * @param localSocket
     */
    private fun processTrueRequestInfo(remoteSocket: Socket, localSocket: Socket) { //如果要写入本地文件的实例声明
        var fileOutputStream: FileOutputStream? = null
        var file: File? = null
        try { // 获取音乐网络数据
            val inRemoteSocket = remoteSocket.getInputStream() ?: return
            val outLocalSocket = localSocket.getOutputStream() ?: return
            // 如果要写入文件，配置相关实例
            if (needCacheAudio) {
                file = File(audioCachePath)
                fileOutputStream = FileOutputStream(file)
            }
            try {
                var readLength: Int
                val remoteReply = ByteArray(4096)
                var firstData = true // 是否循环中第一次获得数据
                // 当从远程还能取到数据且播放器还没切换另一首网络音乐
                while (inRemoteSocket.read(remoteReply, 0, remoteReply.size).also
                        { readLength = it } != -1 && currProxyId == latestProxyId) { // 首先从数据中获得文件总长度
                    try {
                        if (firstData) {
                            firstData = false
                            val str = String(remoteReply, Charset.forName("utf-8"))
                            val pattern = Pattern.compile("Content-Length:\\s*(\\d+)")
                            val matcher = pattern.matcher(str)
                            if (matcher.find()) { // 获取数据的大小
                                fileTotalLength = matcher.group(1)?.toLong() ?: 0
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // 把远程socket拿到的数据用本地socket写到media player中播放
                    try {
                        outLocalSocket.write(remoteReply, 0, readLength)
                        outLocalSocket.flush()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // 计算当前播放时，其在seek bar上的缓冲值,并刷新进度条
                    try {
                        cachedFileLength += readLength.toLong()
                        if (fileTotalLength > 0 && currProxyId == latestProxyId) {
                            currMusicCachedProgress = (cachedFileLength * 100.0f / fileTotalLength).toInt()
                            if (onCachedProgressUpdateListener != null && currMusicCachedProgress <= 100) {
                                onCachedProgressUpdateListener!!.updateCachedProgress(currMusicCachedProgress)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // 如果需要缓存数据到本地，就缓存到本地
                    if (needCacheAudio) {
                        try {
                            fileOutputStream?.write(remoteReply, 0, readLength)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                fileOutputStream!!.flush()
                if (onCachedProgressUpdateListener != null) {
                    onCachedProgressUpdateListener!!.updateCachedProgress(100)
                }
                // 如果是因为切换音乐跳出循环的，当前音乐播放进度，小于 seek bar最大值的1/4，就把当前音乐缓存在本地的数据清除了
                if (currProxyId != latestProxyId && currPlayDegree < 25) {
                    bufferingMusicUrlList.remove(remoteUrl)
                    IoUtils.delete(file)
                }
            } catch (e: Exception) {
                if (file != null) {
                    IoUtils.delete(file)
                }
                bufferingMusicUrlList.remove(remoteUrl)
            } finally {
                inRemoteSocket.close()
                outLocalSocket.close()
                if (fileOutputStream != null) {
                    fileOutputStream.close()
                    // 音频文件缓存完后处理
                    if (file != null && file.exists()) {
                        convert2RightAudioFile(file)
                        if (musicControlInterface != null) {
                            musicControlInterface!!.updateBufferFinishMusicPath(musicKey, file.path)
                            bufferingMusicUrlList.remove(remoteUrl)
                        }
                    }
                }
                localSocket.close()
                remoteSocket.close()
            }
        } catch (e: Exception) {
            if (file != null) {
                IoUtils.delete(file)
            }
            bufferingMusicUrlList.remove(remoteUrl)
        }
    }

    interface MusicControlInterface {
        fun updateBufferFinishMusicPath(musicKey: String?, localPath: String?)
    }

    interface OnCachedProgressUpdateListener {
        fun updateCachedProgress(progress: Int)
    }

    fun setOnCachedProgressUpdateListener(listener: OnCachedProgressUpdateListener?) {
        onCachedProgressUpdateListener = listener
    }

    /**
     * 启动代理服务器。
     */
    fun startProxy() {
        Thread(Runnable {
            try { // 监听MediaPlayer的请求，MediaPlayer->代理服务器
                val localSocket = localServerSocket!!.accept()
                // 获得真实请求信息
                getTrueSocketRequestInfo(localSocket)
                // 保证创建了远程socket地址再进行下一步
                while (remoteAddress == null) {
                    SystemClock.sleep(25)
                }
                // 发送真实socket请求，并返回remote_socket
                val remoteSocket = sendRemoteRequest()
                // 处理真实请求信息
                processTrueRequestInfo(remoteSocket, localSocket)
            } catch (_: Exception) {
            } finally { // 最后释放本地代理server socket
                if (localServerSocket != null) {
                    try {
                        localServerSocket!!.close()
                        localServerSocket = null
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }).start()
    }

    /**
     * 转换为正确的音频文件。
     *
     * @param file
     */
    private fun convert2RightAudioFile(file: File?) {
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            inputStream = FileInputStream(file)
            var read = 0
            while (read > -1) {
                val newRead = inputStream.read()
                if (read == 0 && newRead == 0) {
                    val bs = ByteArray(inputStream.available() + 2)
                    inputStream.read(bs, 2, bs.size - 2)
                    fos = FileOutputStream(file)
                    fos.write(bs)
                    fos.flush()
                    break
                }
                read = newRead
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val LOCALHOST = "127.0.0.1"
        private const val HTTP_PORT = 80
        private const val SOCKET_TIMEOUT = 5000
    }

    init {
        try {
            if (localServerSocket == null || localServerSocket!!.isClosed) {
                // 创建本地socket服务器，用来监听media player请求和给media player提供数据
                localServerSocket = ServerSocket()
                localServerSocket!!.reuseAddress = true
                val socketAddress = InetSocketAddress(InetAddress.getByName(LOCALHOST), localProxyPort)
                localServerSocket!!.bind(socketAddress)
            }
        } catch (e: Exception) {
            try {
                localProxyPort++
                localServerSocket = ServerSocket(localProxyPort, 0, InetAddress.getByName(LOCALHOST))
                localServerSocket!!.reuseAddress = true
            } catch (ignore: Exception) {
            }
        }
    }
}