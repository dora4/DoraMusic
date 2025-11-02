package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraBannerAd

/**
 * 通用横幅广告服务。
 */
interface AdService : ApiService {

    /**
     * 获取该应用的横幅列表。
     */
    @GET("ad/banner/list")
    fun getBannerAds(@Query("productName") productName: String): Call<ApiResult<MutableList<DoraBannerAd>>>

    /**
     * 检测是否显示该应用的横幅。
     */
    @GET("ad/banner/enable")
    fun isShowBannerAds(@Query("productName") productName: String): Call<ApiResult<Boolean>>
}