package site.doramusic.app.http

import com.google.gson.annotations.SerializedName

data class DoraGuessingUser(

    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("nickname")
    val nickname: String? = null
)