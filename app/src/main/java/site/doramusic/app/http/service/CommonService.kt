package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraHomeBanner

interface CommonService : ApiService {

    @GET("homeBannersGet")
    fun getHomeBanners(): Call<ApiResult<MutableList<DoraHomeBanner>>>

    @GET("homeBannersCheck")
    fun checkHomeBanners(@Query("productName") productName: String): Call<ApiResult<Boolean>>
}