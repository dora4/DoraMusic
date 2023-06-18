package site.doramusic.app.lrc

/**
 * 歌词句子，是一个时间戳和一行歌词组成，如“[00.03.21.56]还记得许多年前的春天”。
 */
class LyricLine(time: Long, text: String) {

    /**
     * 歌詞文本的开始时间戳转换为毫秒数的值，如[00.01.02.34]为62340毫秒。
     */
    var startTime: Long = 0

    /**
     * 一句歌词的实现
     */
    var duringTime: Long = 0

    /**
     * 每个时间戳对应的一行歌词文本,如“[00.03.21.56]还记得许多年前的春天”中的“还记得许多年前的春天”。
     */
    var contentText = ""

    init {
        startTime = time
        contentText = text
    }
}
