package site.doramusic.app.chat

data class WsPacket<T>(

    /**
     * channel_msg：聊天室消息。
     */
    val type: String,
    val data: T?
)