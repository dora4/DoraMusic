package site.doramusic.app.chat

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
import site.doramusic.app.auth.TokenStore
import site.doramusic.app.event.ChannelMsgEvent
import java.util.concurrent.TimeUnit

class ChatWsManager {

    private var webSocket: WebSocket? = null
    private val handler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    fun connect(url: String) {
        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${TokenStore.accessToken()}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                LogUtils.d("chat ws 已连接")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                LogUtils.d("chat ws 收到消息：$text")
                try {
                    val msg = gson.fromJson(text, DoraChannelMsg::class.java)
                    if (msg == null || msg.msgContent.isBlank()) {
                        LogUtils.w("chat ws invalid payload: $text")
                        return
                    }
                    RxBus.getInstance().post(ChannelMsgEvent(msg))
                } catch (e: Exception) {
                    LogUtils.e("chat ws parse error: $text\n${e}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                LogUtils.e("chat ws 断开，5 秒后重连\n${t}")
                handler.postDelayed({ connect(url) }, 5000)
            }
        })
    }

    fun send(text: String) {
        webSocket?.send(text)
    }

    fun close() {
        webSocket?.close(1000, "chat ws close")
    }
}
