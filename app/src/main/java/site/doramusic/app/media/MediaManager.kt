package site.doramusic.app.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import android.os.RemoteException
import dora.util.LogUtils

import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Music

/**
 * 通过它调用AIDL远程服务接口。
 */
class MediaManager(internal val context: Context) : IMediaService.Stub(), AppConfig {

    private var mediaService: IMediaService? = null
    private val serviceConnection: ServiceConnection
    private var onCompletionListener: MusicControl.OnConnectCompletionListener? = null

    init {
        this.serviceConnection = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mediaService = asInterface(service)
                if (mediaService != null) {
                    //音频服务启动的标志
                    LogUtils.i("MediaManager:connected")
                    onCompletionListener!!.onConnectCompletion(mediaService)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                //音频服务断开的标志
                LogUtils.i("MediaManager:disconnected")
            }
        }
    }

    fun setOnCompletionListener(l: MusicControl.OnConnectCompletionListener) {
        onCompletionListener = l
    }

    fun connectService() {
        val intent = Intent(AppConfig.MEDIA_SERVICE)
        intent.setClass(context, MediaService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun disconnectService() {
        context.unbindService(serviceConnection)
        context.stopService(Intent(AppConfig.MEDIA_SERVICE))
    }

    override fun play(pos: Int): Boolean {
        try {
            return mediaService?.play(pos) ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun playById(id: Int): Boolean {
        try {
            return mediaService?.playById(id) ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun playByPath(path: String) {
        try {
            mediaService?.playByPath(path)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun playByUrl(music: Music, url: String) {
        try {
            mediaService?.playByUrl(music, url)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun replay(): Boolean {
        try {
            return mediaService?.replay() ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun pause(): Boolean {
        try {
            return mediaService?.pause() ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun prev(): Boolean {
        try {
            return mediaService?.prev() ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun next(): Boolean {
        try {
            return mediaService?.next() ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun stop() {
        try {
            mediaService?.stop() ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun duration(): Int {
        try {
            return mediaService?.duration() ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun setCurMusic(music: Music) {
        try {
            mediaService?.setCurMusic(music) ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun position(): Int {
        try {
            return mediaService?.position() ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun pendingProgress(): Int {
        try {
            return mediaService?.pendingProgress() ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun seekTo(progress: Int): Boolean {
        try {
            return mediaService?.seekTo(progress) ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return false
    }

    override fun refreshPlaylist(playlist: MutableList<Music>?) {
        try {
            mediaService?.refreshPlaylist(playlist)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun setBassBoost(strength: Int) {
        try {
            mediaService?.setBassBoost(strength)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun setEqualizer(bandLevels: IntArray) {
        try {
            mediaService?.setEqualizer(bandLevels)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun getEqualizerFreq(): IntArray? {
        try {
            return mediaService?.equalizerFreq
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getPlayState(): Int {
        try {
            return mediaService?.playState ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun getPlayMode(): Int {
        try {
            return mediaService?.playMode ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return 0
    }

    override fun setPlayMode(mode: Int) {
        try {
            mediaService?.playMode = mode
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun getCurMusicId(): Int {
        try {
            return mediaService?.curMusicId ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    override fun loadCurMusic(music: Music): Boolean {
        try {
            return mediaService?.loadCurMusic(music) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun getCurMusic(): Music? {
        try {
            return mediaService?.curMusic
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getPlaylist(): MutableList<Music>? {
        try {
            return mediaService?.playlist
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun updateNotification(bitmap: Bitmap, title: String, name: String) {
        try {
            mediaService?.updateNotification(bitmap, title, name)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun cancelNotification() {
        try {
            mediaService?.cancelNotification()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}
