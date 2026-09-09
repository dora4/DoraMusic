package site.doramusic.app.track

import android.os.Build
import dora.http.DoraHttp.rxResult
import dora.http.retrofit.RetrofitManager
import dora.util.ApkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.http.SecureRequestBuilder

object TrackAnalysis {

    fun report(scope: CoroutineScope, eventName: String) {
        val brand = Build.BRAND ?: ""
        val model = Build.MODEL ?: ""
        val os = "Android"
        val osVersion = Build.VERSION.RELEASE ?: ""
        val appVersion = ApkUtils.getVersionName()
        scope.launch {
            val req = ReqTrackEvent(
                AppConfig.PRODUCT_NAME, "$brand-$model-$os-$osVersion-$appVersion",
                arrayListOf(
                    TrackEventItem(eventName, System.currentTimeMillis(), null)
                )
            )
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC) ?: return@launch
            rxResult { RetrofitManager.getService(TrackService::class.java).track(body.toRequestBody()) }
        }
    }
}