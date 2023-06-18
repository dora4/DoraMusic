package site.doramusic.app.http.service

import retrofit2.Call
import retrofit2.http.*
import site.doramusic.app.http.DoraResponse
import site.doramusic.app.http.DoraSign
import site.doramusic.app.http.DoraUser

interface UserService {

    @GET("/checkLogin")
    fun checkLogin(@Query("token") token: String): Call<DoraResponse<DoraUser>>

    @POST("/login")
    @FormUrlEncoded
    fun login(@Field("username") username: String, @Field("password")
    password: String): Call<DoraResponse<DoraUser>>

    @GET("/logout")
    fun logout(@Query("token") token: String): Call<DoraResponse<Long>>

    @POST("/register")
    @FormUrlEncoded
    fun register(@Field("username") username: String, @Field("password") password:
                    String, @Field("phone") phone:String): Call<DoraResponse<DoraUser>>

    @POST("/sign")
    @FormUrlEncoded
    fun sign(@Field("userId") userId: Long): Call<DoraResponse<DoraSign>>
}