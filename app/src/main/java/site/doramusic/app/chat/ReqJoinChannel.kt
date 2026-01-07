package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqJoinChannel(
                     // 房间ID
                     val roomId: String) : BaseReq() {
    init {
        payload = sort()
    }
}