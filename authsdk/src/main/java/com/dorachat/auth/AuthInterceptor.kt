package com.dorachat.auth

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AuthInterceptor : Interceptor {

    private val refreshLock = ReentrantLock()

    private fun shouldRefresh(
        response: Response,
        request: Request
    ): Boolean {
        val config = DoraChatSDK.getConfig()
        if (config?.autoRefreshToken != true) return false
        if (response.code != 401) return false
        val path = request.url.encodedPath
        if (path.contains("/auth/refresh")) return false
        if (TokenStore.refreshToken().isNullOrEmpty()) return false
        return true
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val accessToken = TokenStore.accessToken()
        // 1️⃣ 加上 Access Token
        if (!accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        val response = chain.proceed(request)
        // 2️⃣ Access Token 过期处理（401）
        if (response.code == 401) {
            if (!shouldRefresh(response, request)) {
                return signOut(request)
            }
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
                // 尝试刷新 Access Token
                val refreshToken = TokenStore.refreshToken() ?: return signOut(request)
                val newAccess = refreshAccessToken(refreshToken) ?: return signOut(request)
                // 用新的 Access Token 重试
                request = request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
                return chain.proceed(request)
            }
        }
        return response
    }

    /**
     * 刷新 Access Token。
     */
    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val req = ReqToken(refreshToken)
            val body = SecureRequestBuilder.build(req, SecureRequestBuilder.SecureMode.ENC)
            val json = Gson().toJson(body)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            val baseUrl = DoraChatSDK.getConfig()?.apiBaseUrl
            val request = Request.Builder()
                .url("${baseUrl}auth/refresh")
                .post(requestBody)
                .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val str = resp.body?.string() ?: return null
                val root = JSONObject(str)
                val code = root.optString("code")
                if (code != ApiCode.SUCCESS) {
                    if (code == ApiCode.ERROR_SIGN_IN_EXPIRED) {
                        // refresh token 失效 → 强制退出
                        signOut(request)
                    }
                    return null
                }
                val data = root.optJSONObject("data") ?: return null
                val newAccessToken = data.optString("accessToken")
                val newRefreshToken = data.optString("refreshToken")
                if (newAccessToken.isNullOrEmpty() || newRefreshToken.isNullOrEmpty()) return null
                // 3️⃣ 保存新的 Token
                TokenStore.save(newAccessToken, newRefreshToken)
                newAccessToken
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Refresh Token 无效，清理登录信息。
     */
    private fun signOut(request: Request): Response {
        TokenStore.clear()
        SignInExpiredBus.postOnce()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("Token expired")
            .body("{}".toResponseBody())
            .build()
    }
}
