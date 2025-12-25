package site.doramusic.app.sysmsg

import com.google.gson.annotations.SerializedName

/**
 * 系统消息。
 */
data class DoraSysMsg(

    @SerializedName("id")
    val id: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("visible")
    val visible: Int
)