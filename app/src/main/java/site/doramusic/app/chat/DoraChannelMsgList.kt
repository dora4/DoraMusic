package site.doramusic.app.chat

data class DoraChannelMsgList(
    val list: List<DoraChannelMsg>,
    val nextCursor: Long?,   // 下一页游标
    val hasMore: Boolean
)
