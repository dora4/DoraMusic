package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqLeaveChannel(
                     // 房间ID
                     val roomId: String) : BaseReq() {
    init {
        payload = sort()
    }
}