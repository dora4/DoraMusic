package site.doramusic.app.upgrade

import com.google.gson.annotations.SerializedName

data class DoraAppInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("appPlatform")
    val appPlatform: Int,
    @SerializedName("versionCode")
    val versionCode: Int,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("updateLog")
    val updateLog: String,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("forceUpdate")
    val forceUpdate: Boolean
)
