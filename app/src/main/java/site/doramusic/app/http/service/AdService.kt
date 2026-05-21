package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import site.doramusic.app.http.ApiResult
import site.doramusic.app.http.DoraBannerAd
import site.doramusic.app.http.DoraConfig

/**
 * 通用广告服务。
 */
interface AdService : ApiService {

    companion object {
        const val CONF_ENABLE_BANNER_AD = "enable_banner_ad"
        const val CONF_ENABLE_GUESSING = "enable_guessing"
        const val CONF_GUESSING_TOPIC = "guessing_topic"
    }

    /**
     * 获取该应用的横幅列表。
     * 示例：http://dorachat.com:9696/api/ad/banner/list?productName=doramusic
     */
    @GET("ad/banner/list")
    fun getBannerAds(@Query("productName") productName: String): Call<ApiResult<MutableList<DoraBannerAd>>>

    /**
     * 检测是否显示该应用的横幅广告。
     * 示例：http://dorachat.com:9696/api/config/get?productName=doramusic&configName=enable_banner_ad
     */
    @GET("config/get")
    fun isShowBannerAds(@Query("productName") productName: String,
                        @Query("configName") configName: String = CONF_ENABLE_BANNER_AD): Call<ApiResult<DoraConfig>>

    /**
     * 竞猜活动是否开启。
     */
    @GET("config/get")
    fun isGuessingOpened(@Query("productName") productName: String,
                         @Query("configName") configName: String = CONF_ENABLE_GUESSING): Call<ApiResult<DoraConfig>>

    /**
     * 获取竞猜主题信息。
     */
    @GET("config/get")
    fun getGuessingTopic(@Query("productName") productName: String,
                         @Query("configName") configName: String = CONF_GUESSING_TOPIC): Call<ApiResult<DoraConfig>>
}
