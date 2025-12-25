package site.doramusic.app.sysmsg

import dora.http.retrofit.ApiService
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

/**
 * 通用系统消息。
 */
interface SysMsgService : ApiService {

    @POST("sysmsg/list")
    fun getSysMsgList(@Body req: RequestBody): Call<ApiResult<List<DoraSysMsg>>>
}