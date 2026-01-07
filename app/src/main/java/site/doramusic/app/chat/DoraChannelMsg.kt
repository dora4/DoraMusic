package site.doramusic.app.chat

data class DoraChannelMsg(
    val msgId: Long,
    val msgSeq: Long = 0,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: Int,
    val msgType: Int,
    val msgContent: String,
    val recall: Boolean = false,
    val ts: Long
)
