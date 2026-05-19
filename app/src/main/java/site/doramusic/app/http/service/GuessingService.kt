package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraGuessingInfoWithItems
import site.doramusic.app.http.DoraGuessingRank

interface GuessingService : ApiService {

    companion object {
        const val SERVER_URL = "http://dorachat.com:3000"
    }

    /**
     * 获取竞猜列表。
     */
    @POST("guessing/list")
    fun getList(@Body body: RequestBody): Call<ApiResult<MutableList<DoraGuessingInfoWithItems>>>

    /**
     * 投注。
     */
    @POST("guessing/bet")
    fun bet(@Body body: RequestBody): Call<ApiResult<Boolean>>

    /**
     * 领取积分。
     */
    @POST("guessing/claim")
    fun claim(@Body body: RequestBody): Call<ApiResult<Long>>

    /**
     * 竞猜排行榜。0 - 胜率榜，1 - 盈利榜，2 - 投注榜。
     */
    @GET("guessing/rank")
    fun getRank(@Query("type") type: Int): Call<ApiResult<MutableList<DoraGuessingRank>>>
}