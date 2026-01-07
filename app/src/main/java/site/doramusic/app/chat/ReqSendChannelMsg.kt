package site.doramusic.app.chat

import site.doramusic.app.http.BaseReq

data class ReqSendChannelMsg(
                     // 房间ID
                     val roomId: String,
                     // 消息类型：文字、图片、文件？
                     val msgType: Int,
                     // 消息内容：text或JSON字符串
                     val msgContent: String) : BaseReq() {
    init {
        payload = sort()
    }
}