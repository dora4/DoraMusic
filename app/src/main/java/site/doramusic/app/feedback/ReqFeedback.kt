package site.doramusic.app.feedback

import com.google.gson.Gson
import site.doramusic.app.http.BaseReq

data class ReqFeedback(
    val productName: String = "doramusic",
    val feedbackContent: String,
    var feedbackExtras: String = ""
) : BaseReq() {

    init {
        feedbackExtras = Gson().toJson(DeviceInfoProvider.collect())
        payload = sort()
    }
}
