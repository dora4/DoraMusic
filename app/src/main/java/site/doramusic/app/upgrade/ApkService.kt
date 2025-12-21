package site.doramusic.app.upgrade

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

interface ApkService : ApiService {

    /**
     * 检测版本更新。
     */
    @POST("checkUpdateApk")
    @FormUrlEncoded
    fun checkUpdate(@Field("productName") productName: String): Call<ApiResult<DoraAppInfo>>
}