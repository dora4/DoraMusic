package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqChannelMsgList(
    // 房间ID
    val roomId: String,
    // 游标，null = 最新
    val cursor: Long?,
    // 后端用于标记消息发送顺序的原子递增序号，用于防止被并发写穿
    val msgSeq: Long,
    // 每页拉取消息数量
    val limit: Int = 20) : BaseReq() {

    init {
        payload = sort()
    }
}
