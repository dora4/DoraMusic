package site.doramusic.app.auth

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.http.SecureRequestBuilder
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AuthInterceptor : Interceptor {

    private val refreshLock = ReentrantLock()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val accessToken = TokenStore.accessToken()
        // 请求自动带上 Access Token
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        val response = chain.proceed(request)
        // Access Token过期处理（401）
        if (response.code == 401) {
            response.close() // 先关闭旧 response
            // 保证只有一个线程刷新
            refreshLock.withLock {
                // 可能被其他请求刷新过了
                val latest = TokenStore.accessToken()
                if (!latest.isNullOrEmpty() && request.header("Authorization") != "Bearer $latest") {
                    request = request.newBuilder()
                        .header("Authorization", "Bearer $latest")
                        .build()
                    return chain.proceed(request)
                }
                // 尝试刷新Access Token
                val refreshToken = TokenStore.refreshToken() ?: return signOut(request)
                val newAccess = refreshAccessToken(refreshToken) ?: return signOut(request)
                // 用新的Access Token重试
                request = request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
                return chain.proceed(request)
            }
        }

        return response
    }

    /**
     * 刷新 Access Token
     */
    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val req = ReqToken(refreshToken)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
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
                if (newAccessToken.isNullOrEmpty() || newRefreshToken.isNullOrEmpty()) return null
                // 保存新的Token
                TokenStore.save(newAccessToken, newRefreshToken)
                newAccessToken
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Refresh Token无效，清理登录信息。
     */
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
