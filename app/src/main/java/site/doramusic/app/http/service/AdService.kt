package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraBannerAd

interface AdService : ApiService {

    @GET("ad/banner/list")
    fun getBannerAds(@Query("productName") productName: String): Call<ApiResult<MutableList<DoraBannerAd>>>

    @GET("ad/banner/enable")
    fun isShowBannerAds(@Query("productName") productName: String): Call<ApiResult<Boolean>>
}