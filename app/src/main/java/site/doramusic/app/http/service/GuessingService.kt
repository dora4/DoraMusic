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
import site.doramusic.app.http.DoraGuessingReward
import site.doramusic.app.http.DoraGuessingUser
import site.doramusic.app.http.GuestSession

interface GuessingService : ApiService {

    companion object {

        /**
         * 深情端口的朵拉音乐服务器。
         */
        const val SERVER_URL = "http://dorachat.com:3000"
    }

    /**
     * 生成游客令牌。
     */
    @GET("guest/init")
    fun initGuest(): Call<ApiResult<GuestSession>>

    /**
     * 检测游客令牌。
     */
    @GET("guest/checkToken")
    fun checkGuestToken(@Query("token") token: String): Call<ApiResult<Boolean>>

    /**
     * 设置昵称。
     */
    @POST("guessing/setNickname")
    fun setNickname(@Body body: RequestBody): Call<ApiResult<Boolean>>

    /**
     * 获取竞猜用户信息。
     */
    @POST("guessing/profile")
    fun getProfile(@Body body: RequestBody): Call<ApiResult<DoraGuessingUser>>

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
     * 我的竞猜，结算后的竞猜奖励结果列表。
     */
    @POST("guessing/reward")
    fun getRewardList(@Body body: RequestBody): Call<ApiResult<MutableList<DoraGuessingReward>>>

    /**
     * 领取积分。
     */
    @POST("guessing/claim")
    fun claim(@Body body: RequestBody): Call<ApiResult<Long>>

    /**
     * 竞猜排行榜。0 - 胜率榜，1 - 盈亏榜，2 - 投注榜。
     */
    @GET("guessing/rank")
    fun getRank(@Query("type") type: Int): Call<ApiResult<MutableList<DoraGuessingRank>>>
}
