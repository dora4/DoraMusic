package site.doramusic.app.http.service

import retrofit2.Call
import retrofit2.http.*
import site.doramusic.app.http.DoraPatch
import site.doramusic.app.http.DoraResponse

interface UpdateService {

    @POST("/updateApk")
    @FormUrlEncoded
    fun updateApk(@Field("versionCode") versionCode: Int): Call<DoraResponse<String>>

    @GET("/latestPatch")
    fun getLatestPatchInfo(@Query("versionName") versionName: String): Call<DoraResponse<DoraPatch>>
}