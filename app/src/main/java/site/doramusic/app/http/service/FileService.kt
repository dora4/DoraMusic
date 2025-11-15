package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import site.doramusic.app.http.ApiResult

interface FileService : ApiService {

    @Multipart
    @POST("ipfsEmu")
    fun ipfsEmu(@Part part: MultipartBody.Part): Call<ApiResult<String>>
}