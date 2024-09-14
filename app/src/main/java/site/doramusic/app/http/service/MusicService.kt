package site.doramusic.app.http.service

import dora.http.retrofit.ApiService
import retrofit2.Call
import retrofit2.http.*
import site.doramusic.app.http.DoraResponse
import site.doramusic.app.lrc.DoraLyric

interface MusicService : ApiService {

    @GET("searchLrc")
    fun searchLrc(@Query("musicName") musicName: String,
                  @Query("musicArtist") musicArtist: String): Call<DoraResponse<DoraLyric>>

    @FormUrlEncoded
    @POST("lyric")
    fun lyric(@Field("id") id: Long): Call<DoraResponse<DoraLyric>>
}