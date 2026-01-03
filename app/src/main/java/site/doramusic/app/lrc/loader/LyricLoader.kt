package site.doramusic.app.lrc.loader

import dora.util.GlobalContext
import dora.util.IoUtils
import dora.util.TextUtils
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.db.Music
import site.doramusic.app.lrc.LyricScroller
import site.doramusic.app.lrc.LyricScroller.LyricListener
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

/**
 * 抽象的歌词加载器。
 */
abstract class LyricLoader(protected var lyricScroller: LyricScroller, protected var lyricListener: LyricListener) {

    companion object {
        @JvmStatic
        protected val LRC_SAVE_FOLDER = AppConfig.FOLDER_LRC
        protected const val FLAC = ".flac"
        protected const val MP3 = ".mp3"
        protected const val LRC = ".lrc"
    }

    fun clearLocalLrc() {
        loadLocalLrc(null)
    }

    fun loadLocalLrc(path: String?) {
        lyricScroller.loadLyric(path)
        lyricScroller.setLyricListener(lyricListener)
    }

    /**
     * 获取歌词路径<br></br>
     * 先从已下载文件夹中查找，如果不存在，则从歌曲文件所在文件夹查找。
     *
     * @return 如果存在返回路径，否则返回null
     */
    fun getLrcFilePath(music: Music): String {
        var lrcFilePath = LRC_SAVE_FOLDER + getLrcFileName(music.artist, music.musicName)
        if (!exists(lrcFilePath)) {
            lrcFilePath = music.data.replace(FLAC, LRC).replace(MP3, LRC)
            if (!exists(lrcFilePath)) {
                lrcFilePath = ""
            }
        }
        return lrcFilePath
    }

    private fun mkdirs(dir: String): String {
        val file = File(dir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return dir
    }

    private fun exists(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    fun getFlacFileName(artist: String?, title: String?): String {
        return getFileName(artist, title) + FLAC
    }

    fun getMp3FileName(artist: String?, title: String?): String {
        return getFileName(artist, title) + MP3
    }

    fun getLrcFileName(artist: String?, title: String?): String {
        return getFileName(artist, title) + LRC
    }

    fun getFileName(artist: String?, title: String?): String {
        var artist = artist
        var title = title
        if (TextUtils.isEmpty(artist)) {
            artist = GlobalContext.get().getString(R.string.unknown)
        }
        if (TextUtils.isEmpty(title)) {
            title = GlobalContext.get().getString(R.string.unknown)
        }
        artist = stringFilter(artist)
        title = stringFilter(title)
        return "$artist - $title"
    }

    /**
     * 过滤特殊字符(\/:*?"<>|)
     */
    private fun stringFilter(str: String?): String? {
        if (str == null) {
            return null
        }
        val regEx = "[/:*?\"<>|]"
        val p = Pattern.compile(regEx)
        val m = p.matcher(str)
        return m.replaceAll("")
    }

    abstract fun searchLrc(music: Music?)

    abstract fun searchLrcBySongId(id: Long, lrcSaveFileName: String)

    protected fun saveLrc(lyric: String, lrcFileName: String) {
        val lyricFile = File("$LRC_SAVE_FOLDER/$lrcFileName")
        if (!lyricFile.parentFile?.exists()!!) {
            lyricFile.parentFile?.mkdirs()
        }
        try {
            IoUtils.write(lyric.toByteArray(), lyricFile.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}