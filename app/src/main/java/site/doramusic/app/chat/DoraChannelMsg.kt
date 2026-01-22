package site.doramusic.app.chat

/**
 * 聊天室消息实体，这个其实是通用实体类，固定chatType=2。
 */
data class DoraChannelMsg(
    /**
     * 消息ID，不重复。
     */
    var msgId: Long = 0,
    /**
     * 消息序号，同一sessionId下，原子递增，由后端保证线程安全。
     */
    val msgSeq: Long = 0,
    /**
     * 聊天类型，0-私聊，1-群聊，2-聊天室，3-客服。
     */
    val chatType: Int = 2,
    /**
     * 会话ID，相较于原roomId的设计，是更高级别的抽象，聊天室这里固定使用房间ID即可。
     */
    val sessionId: String = "",
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
     * 0-正常，1-已撤回（仅发送不超过2分钟的消息可被撤回）。
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