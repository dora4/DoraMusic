package site.doramusic.app.http

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 请求数据最终实体。
 *
 * @see SecureRequestBuilder.SecureMode
 */
data class ReqBody(
    /**
     * 指定安全模式的名称，如ENC。
     */
    val mode: String = "",
    /**
     * RSA公钥加密后的AES Key。
     */
    val key: String = "",
    /**
     * 传输的数据，可使用动态AES Key进行加密。
     */
    val data: String = "",
    /**
     * 客户端签名，ENC_SIGN模式下，才需要传这个参数。
     */
    val sign: String = "",
) {

    fun toRequestBody() : RequestBody {
        return Gson().toJson(this).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}