package com.dorachat.auth

import dora.http.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 用户认证。
 */
interface AuthService : ApiService {

    /**
     * 登录。
     *
     * @see ReqSignIn
     */
    @POST("auth/signIn")
    fun signIn(@Body body: RequestBody): Flow<ApiResult<DoraUser>>

    /**
     * 注销登录。
     *
     * @see ReqToken
     */
    @POST("auth/signOut")
    fun signOut(@Body body: RequestBody): Call<ApiResult<Boolean>>

    /**
     * 检测token。
     *
     * @see ReqToken
     */
    @POST("auth/checkToken")
    fun checkToken(@Body body: RequestBody): Flow<ApiResult<DoraUser>>
}