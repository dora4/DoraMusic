package site.doramusic.app.auth

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.http.SecureRequestBuilder

class AuthInterceptor : Interceptor {

    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        TokenStore.accessToken()?.let {
            request = request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        }
        val response = chain.proceed(request)
        if (response.code == 401) {
            response.close()
            synchronized(refreshLock) {
                // 可能已被其他请求刷新
                val latest = TokenStore.accessToken()
                if (!latest.isNullOrEmpty()
                    && request.header("Authorization") != "Bearer $latest"
                ) {
                    return chain.proceed(
                        request.newBuilder()
                            .header("Authorization", "Bearer $latest")
                            .build()
                    )
                }
                val refreshToken = TokenStore.refreshToken()
                    ?: return signOut(request)
                val newAccess = refreshAccessToken(refreshToken)
                    ?: return signOut(request)
                return chain.proceed(
                    request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                )
            }
        } else {
            return response
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val req = ReqToken(refreshToken)
            val body = SecureRequestBuilder.build(
                req,
                SecureRequestBuilder.SecureMode.ENC
            )
            val json = Gson().toJson(body)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(AppConfig.URL_AUTH_SERVER + "/auth/refresh")
                .post(requestBody)
                .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val str = resp.body?.string() ?: return null
                val data = JSONObject(str).optJSONObject("data") ?: return null
                val newAccessToken = data.optString("accessToken")
                val newRefreshToken = data.optString("refreshToken")
                TokenStore.save(newAccessToken, newRefreshToken)
                newAccessToken
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun signOut(request: Request): Response {
        TokenStore.clear()
        LoginExpiredBus.postOnce()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("Token expired")
            .body("{}".toResponseBody())
            .build()
    }
}
