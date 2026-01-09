package site.doramusic.app.http

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 请求数据最终实体。
 */
data class ReqBody(
    val mode: String = "",
    val key: String = "",
    val data: String = "",
    val sign: String = "",
) {

    fun toRequestBody() : RequestBody {
        return Gson().toJson(this).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}