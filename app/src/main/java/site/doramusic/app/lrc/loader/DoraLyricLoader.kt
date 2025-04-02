package site.doramusic.app.lrc.loader

import android.os.Handler
import android.os.Looper
import dora.http.retrofit.RetrofitManager
import dora.util.TextUtils
import site.doramusic.app.db.Music
import site.doramusic.app.http.DoraCallback
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.lrc.DoraLyric
import site.doramusic.app.lrc.LyricScroller
import site.doramusic.app.lrc.LyricScroller.LyricListener
import java.io.File

/**
 * 朵拉音乐官方歌词加载器，仅供参考，没有使用到。
 */
class DoraLyricLoader(scroller: LyricScroller, listener: LyricListener) : LyricLoader(scroller, listener) {

    override fun searchLrc(music: Music?) {
        if (music != null) {
            val musicName = music.musicName
            val artist = music.artist
            val lrcSaveFileName = "$artist - $musicName.lrc"
            val file = File("$LRC_SAVE_FOLDER/$lrcSaveFileName")
            if (file.exists()) {
                loadLocalLrc(file.absolutePath)
            } else {
                clearLocalLrc()
                val service = RetrofitManager.getService(MusicService::class.java)
                val call = service.searchLrc(musicName, artist)
                call.enqueue(object: DoraCallback<DoraLyric>(){

                    override fun onSuccess(body: DoraLyric) {
                        val lrc = body.lrc
                        if (TextUtils.isNotEmpty(lrc) &&
                            TextUtils.isEqualTo(musicName, body.musicName) &&
                            TextUtils.isEqualTo(artist, body.musicArtist) ) {
                            Handler(Looper.getMainLooper()).post {
                                val lyricSavePath = "$LRC_SAVE_FOLDER/$lrcSaveFileName"
                                lyricScroller.loadLyric(lyricSavePath)
                                lyricScroller.setLyricListener(lyricListener)
                            }
                            saveLrc(lrc!!, lrcSaveFileName)
                        }
                    }

                    override fun onFailure(code: Int, msg: String) {
                        clearLocalLrc()
                    }
                })
            }
        }
    }

    override fun searchLrcBySongId(id: Long, lrcSaveFileName: String) {
        val service = RetrofitManager.getService(MusicService::class.java)
        val call = service.lyric(id)
        call.enqueue(object: DoraCallback<DoraLyric>(){

            override fun onSuccess(body: DoraLyric) {
                val lrc = body.lrc
                if (TextUtils.isNotEmpty(lrc)) {
                    Handler(Looper.getMainLooper()).post {
                        val lyricSavePath = "$LRC_SAVE_FOLDER/$lrcSaveFileName"
                        lyricScroller.loadLyric(lyricSavePath)
                        lyricScroller.setLyricListener(lyricListener)
                    }
                    saveLrc(lrc!!, lrcSaveFileName)
                }
            }

            override fun onFailure(code: Int, msg: String) {
                clearLocalLrc()
            }
        })
    }
}