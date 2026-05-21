package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class GuestSession(

    @SerializedName("token")
    val token: String,

    @SerializedName("userId")
    val userId: String
)