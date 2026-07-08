package site.doramusic.app.track

import dora.http.DoraHttp.rxResult
import dora.http.retrofit.RetrofitManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.feedback.DeviceInfoProvider
import site.doramusic.app.http.SecureRequestBuilder

object TrackAnalysis {

    fun report(scope: CoroutineScope, eventName: String) {
        scope.launch {
            val req = ReqTrackEvent(
                AppConfig.PRODUCT_NAME, DeviceInfoProvider.collect().toString(),
                arrayListOf(
                    TrackEventItem(eventName, System.currentTimeMillis(), null)
                )
            )
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC) ?: return@launch
            rxResult { RetrofitManager.getService(TrackService::class.java).track(body.toRequestBody()) }
        }
    }
}