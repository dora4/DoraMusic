package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraHomeBanner

interface CommonService : ApiService {

    @GET("v3/homeBannersGet")
    fun getHomeBannersV3(): Call<ApiResult<MutableList<DoraHomeBanner>>>
}