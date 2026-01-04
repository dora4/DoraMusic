package site.doramusic.app.auth

import dora.http.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import site.doramusic.app.http.ApiResult

/**
 * 用户认证。
 */
interface AuthService : ApiService {

    /**
     * 登录。
     *
     * @see site.doramusic.app.auth.ReqSignIn
     */
    @POST("auth/signIn")
    fun signIn(@Body body: RequestBody): Call<ApiResult<DoraUser>>

    /**
     * 注销登录。
     *
     * @see site.doramusic.app.auth.ReqToken
     */
    @POST("auth/signOut")
    fun signOut(@Body body: RequestBody): Call<ApiResult<Boolean>>

    /**
     * 检测token。
     *
     * @see site.doramusic.app.auth.ReqToken
     */
    @POST("auth/checkToken")
    fun checkToken(@Body body: RequestBody): Flow<ApiResult<DoraUser>>
}