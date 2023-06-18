package site.doramusic.app.http

/**
 * 朵拉音乐歌曲库的音乐。
 */
class DoraMusic {

    /**
     * 音乐ID。
     */
    var id: String? = null
    /**
     * 歌曲名。
     */
    var name: String? = null
    /**
     * 歌手名。
     */
    var artist: String? = null
    /**
     * 专辑名。
     */
    var album: String? = null
    /**
     * 歌曲文件类型，如MP3、FLAC、WAV。
     */
    var type: String? = null
    /**
     * 歌曲库服务器URL。
     */
    var baseUrl: String? = null
    /**
     * 歌曲文件的MD5值。
     */
    var md5: String? = null

    override fun toString(): String {
        return "DoraMusic{" +
                "_id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", type='" + type + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", md5='" + md5 + '\'' +
                '}'
    }
}