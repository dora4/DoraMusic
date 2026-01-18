package site.doramusic.app.chat

import android.os.Handler
import android.os.Looper
import com.dorachat.auth.AuthManager
import com.dorachat.auth.DoraChatSDK
import com.google.gson.Gson
import dora.util.LogUtils
import dora.util.RxBus
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * 聊天用的WebSocket要有状态机。
 */
object ChatWsManager {

    @Volatile
    private var state = WsState.IDLE

    private val lock = Any()

    private var webSocket: WebSocket? = null
    private val handler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private var wsUrl: String? = null

    fun connect(url: String) {
        synchronized(lock) {
            if (state == WsState.CONNECTED || state == WsState.CONNECTING) {
                return
            }
            state = WsState.CONNECTING
            wsUrl = url
        }
        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        // 认证ws的正确方式，不要使用头包发token
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${AuthManager.getAccessToken()}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                synchronized(lock) {
                    if (state != WsState.CONNECTING) {
                        ws.close(1000, "invalid state")
                        return
                    }
                    state = WsState.CONNECTED
                }
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
                synchronized(lock) {
                    if (state == WsState.CLOSED) return
                    state = WsState.IDLE
                }
                handler.postDelayed({
                    wsUrl?.let { connect(it) }
                }, 5000)
            }
        })
    }

    fun send(text: String) {
        synchronized(lock) {
            if (state != WsState.CONNECTED) return
            webSocket?.send(text)
        }
    }

    fun close() {
        synchronized(lock) {
            state = WsState.CLOSED
            handler.removeCallbacksAndMessages(null)
            webSocket?.close(1000, "chat ws close")
        }
    }

    enum class WsState {
        IDLE,
        CONNECTING,
        CONNECTED,
        CLOSED
    }
}
