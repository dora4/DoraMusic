package site.doramusic.app.track

import site.doramusic.app.http.BaseReq

data class ReqTrackEvent(

    val productName: String,
    val deviceId: String,
    val events: List<TrackEventItem>) : BaseReq() {

    init {
        payload = sort()
    }
}