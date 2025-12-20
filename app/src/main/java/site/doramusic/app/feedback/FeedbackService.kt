package site.doramusic.app.feedback

import dora.http.retrofit.ApiService
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

interface FeedbackService : ApiService {

    /**
     * 提交反馈建议。
     */
    @POST("feedback/add")
    fun commitFeedback(@Body req: RequestBody): Call<ApiResult<Boolean>>
}