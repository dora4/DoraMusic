package site.doramusic.app.sysmsg

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult

/**
 * 通用系统消息。
 */
interface SysMsgService : ApiService {

    /**
     * 获取系统公告。
     */
    @GET("sysmsg/list")
    fun getSysMsgList(@Query("productName") productName: String): Call<ApiResult<MutableList<DoraSysMsg>>>
}