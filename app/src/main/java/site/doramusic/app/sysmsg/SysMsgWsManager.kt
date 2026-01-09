package site.doramusic.app.sysmsg

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import dora.util.LogUtils
import dora.util.RxBus
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import site.doramusic.app.sysmsg.SysMsgEvent
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
                try {
                    val sysMsg = Gson().fromJson(text, DoraSysMsg::class.java)
                    // 二次校验
                    if (sysMsg == null || sysMsg.title.isBlank()) {
                        LogUtils.w("ws sysmsg invalid payload: $text")
                        return
                    }
                    RxBus.getInstance().post(SysMsgEvent(sysMsg))
                    LogUtils.d("ws 收到系统消息: ${sysMsg.title}")
                } catch (e: Exception) {
                    // 兜底：解析异常不能影响 WS
                    LogUtils.e("ws sysmsg parse error: $text\n${e.toString()}")
                }
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
