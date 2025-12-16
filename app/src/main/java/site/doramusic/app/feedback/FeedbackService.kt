package site.doramusic.app.feedback

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.ReqBody

interface FeedbackService : ApiService {

    /**
     * 提交反馈建议。
     * @see ReqFeedback
     */
    @POST("feedback")
    fun commitFeedback(@Body body: ReqBody): Call<ApiResult<Boolean>>
}