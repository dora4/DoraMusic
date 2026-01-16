package site.doramusic.app.chat

/**
 * 聊天室消息实体。
 */
data class DoraChannelMsg(
    /**
     * 消息ID。
     */
    var msgId: Long = 0,
    /**
     * 消息序号，后端原子递增，防止被并发写穿。
     */
    val msgSeq: Long = 0,
    /**
     * 房间ID。
     */
    val roomId: String = "",
    /**
     * 发送者的ERC20地址。
     */
    val senderId: String = "",
    /**
     * 发送者的昵称。
     */
    val senderName: String = "",
    /**
     * 发送者头像的URL。
     */
    val senderAvatar: String = "",
    /**
     * 发送者角色，0-普通成员。
     */
    val senderRole: Int = 0,
    /**
     * 0-文字消息，100-撤回消息。
     */
    var msgType: Int = 0,
    /**
     * 消息内容。
     */
    var msgContent: String = "",
    /**
     * 0-正常，1-已撤回（仅发送不超过2分钟的消息）。
     */
    var recall: Int = 0,
    /**
     * ALL-全频道（需要role不为0），ERC20地址逗号隔开。
     */
    val atUserIds: String? = null,
    /**
     * 引用/回复的消息ID。
     */
    val replyMsgId: Long = 0,
    /**
     * 消息时间戳。
     */
    val ts: Long = System.currentTimeMillis(),
)