package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqDeleteChannelMsg(
                     // 房间ID
                     val roomId: String,
                     // 消息ID
                     val msgId: Long) : BaseReq() {
    init {
        payload = sort()
    }
}