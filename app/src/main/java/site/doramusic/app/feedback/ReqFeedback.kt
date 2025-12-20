package site.doramusic.app.feedback

import com.google.gson.Gson
import site.doramusic.app.http.BaseReq

data class ReqFeedback(val productName: String,
                       var feedbackType: Int = 0,
                       var feedbackContent: String,
                       var feedbackExtras: String = "") : BaseReq() {


    init {
        feedbackExtras = Gson().toJson(DeviceInfoProvider.collect())
        payload = sort()
    }
}