package site.doramusic.app.http.guessing

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class ReqGuessingBet(

    @SerializedName("token")
    val token: String,

    @SerializedName("guessingId")
    val guessingId: Long,

    @SerializedName("itemId")
    val itemId: Long,

    @SerializedName("score")
    val score: Long
) {

    fun toRequestBody() : RequestBody {
        return Gson().toJson(this).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}