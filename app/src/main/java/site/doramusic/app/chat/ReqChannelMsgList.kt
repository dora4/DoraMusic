package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqChannelMsgList(
    // 房间ID
    val roomId: String,
    // 游标，null = 最新
    val cursor: Long?,
    val limit: Int = 20) : BaseReq() {

    init {
        payload = sort()
    }
}
