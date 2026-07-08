package site.doramusic.app.track

import dora.http.retrofit.ApiService
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

interface TrackService : ApiService {

    companion object {
        const val SERVER_URL = "http://dorachat.com:9696/api/"
    }

    /**
     * @see site.doramusic.app.track.ReqTrackEvent
     */
    @POST("track/event")
    fun track(@Body body: RequestBody):
            Observable<ApiResult<Unit>>
}