package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult

interface FileService : ApiService {

    @Multipart
    @POST("ipfsEmu")
    fun ipfsEmu(@Part part: MultipartBody.Part): Call<ApiResult<String>>

    /**
     * 获取文件的真实路径。
     */
    @GET("ipfsGet")
    fun ipfsGet(@Query("cid") cid: String): Call<ApiResult<String>>
}