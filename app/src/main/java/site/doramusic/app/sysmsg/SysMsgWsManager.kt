package site.doramusic.app.sysmsg

import android.os.Handler
import android.os.Looper
import dora.util.LogUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class SysMsgWsManager {

    private var webSocket: WebSocket? = null
    private val handler = Handler(Looper.getMainLooper())

    fun connect(url: String) {
        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                LogUtils.d("ws已连接")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                LogUtils.d("ws收到系统消息: $text")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                LogUtils.e("ws断开，5 秒后重连\n${t.toString()}")
                handler.postDelayed({ connect(url) }, 5000)
            }
        })
    }

    fun close() {
        webSocket?.close(1000, "ws close")
    }
}
