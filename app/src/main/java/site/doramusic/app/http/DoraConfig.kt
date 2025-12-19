package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraConfig(
    @SerializedName("id")
    val id: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("configName")
    val configName: String,

    @SerializedName("configValue")
    val configValue: String,

    @SerializedName("configDesc")
    val configDesc: String,

    @SerializedName("visible")
    val visible: Int
)
