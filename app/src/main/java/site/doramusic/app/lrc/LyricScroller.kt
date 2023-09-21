package site.doramusic.app.lrc

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

/**
 * 歌词的显示控制。
 */
class LyricScroller {

    /**
     * 句子集合。
     */
    private val lines: ArrayList<LyricLine>? = ArrayList()
    private var lyricListener: LyricListener? = null
    /**
     * 是否有本地歌词。
     */
    private var hasLocalLyric = false
    /**
     * 当前正在播放的歌词句子的在句子集合中的索引号。
     */
    var indexOfCurrentSentence = -1
    /**
     * 用于缓存的一个正则表达式对象，识别[]中的内容，不包括中括号。
     */
    private val bracketPattern = Pattern
        .compile("(?<=\\[).*?(?=\\])")
    private val timePattern = Pattern
        .compile("(?<=\\[)(\\d{2}:\\d{2}\\.?\\d{0,3})(?=\\])")
    private val charset = "utf-8"

    /**
     * 用于向外通知歌词载入、变化的监听器。
     */
    interface LyricListener {
        /**
         * 歌词载入时调用。
         *
         * @param lyricLines     歌词文本处理后的所有歌词句子
         * @param indexOfCurSentence 正在播放的句子在句子集合中的索引号
         */
        fun onLyricLoaded(
            lyricLines: MutableList<LyricLine>,
            indexOfCurSentence: Int
        )

        /**
         * 歌词变化时调用。
         *
         * @param indexOfCurSentence 正在播放的句子在句子集合中的索引号
         */
        fun onLyricSentenceChanged(indexOfCurSentence: Int)
    }

    val lyricSentences: List<LyricLine>?
        get() = lines

    fun setLyricListener(listener: LyricListener?) {
        lyricListener = listener
    }

    /**
     * 根据歌词文件的路径，读取出歌词文本并解析。
     *
     * @param lyricPath 歌词文件路径
     * @return true表示存在歌词，false表示不存在歌词
     */
    fun loadLyric(lyricPath: String?): Boolean {
        hasLocalLyric = false
        lines!!.clear()
        if (lyricPath != null) {
            val file = File(lyricPath)
            if (file.exists()) {
                Log.i(TAG, "歌词文件存在")
                hasLocalLyric = true
                try {
                    val fr = FileInputStream(file)
                    val isr = InputStreamReader(fr, charset)
                    val br = BufferedReader(isr)
                    var line: String
                    // 逐行分析歌词文本
                    while ((br.readLine().also { line = it }) != null) {
                        Log.i(TAG, "lyric line:$line")
                        parseLine(line)
                    }
                    // 按时间排序句子集合
                    Collections.sort(lines,
                        Comparator { object1, object2 ->
                            // 内嵌，匿名的compare类
                            if (object1.startTime > object2
                                    .startTime
                            ) {
                                1
                            } else if (object1.startTime < object2
                                    .startTime
                            ) {
                                -1
                            } else {
                                0
                            }
                        })
                    for (i in 0 until lines.size - 1) {
                        lines[i].duringTime = lines[i + 1].startTime
                    }
                    lines[lines.size - 1]
                        .duringTime = Int.MAX_VALUE.toLong()
                    fr.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                }
            } else {
                Log.i(TAG, "歌词文件不存在")
            }
        }
        // 如果有谁在监听，通知它歌词载入完啦，并把载入的句子集合也传递过去
        if (lyricListener != null) {
            lyricListener!!.onLyricLoaded(
                lines,
                indexOfCurrentSentence
            )
        }
        if (hasLocalLyric) {
            Log.i(
                TAG, ("Lyric file existed.Lyric has " + lines.size
                        + " Sentences")
            )
        } else {
            Log.i(TAG, "Lyric file does not existed")
        }
        return hasLocalLyric
    }

    /**
     * 根据传递过来的已播放的毫秒数，计算应当对应到句子集合中的哪一句，再通知监听者播放到的位置。
     *
     * @param millisecond 已播放的毫秒数
     */
    fun notifyTime(millisecond: Long) {
        if (hasLocalLyric && (lines != null) && (lines.size != 0)) {
            val newLyricIndex = seekSentenceIndex(millisecond)
            if (newLyricIndex != -1 && newLyricIndex != indexOfCurrentSentence) { // 如果找到的歌词和现在的不是一句。
                if (lyricListener != null) { // 告诉一声，歌词已经变成另外一句啦！
                    lyricListener!!.onLyricSentenceChanged(newLyricIndex)
                }
                indexOfCurrentSentence = newLyricIndex
            }
        }
    }

    private fun seekSentenceIndex(millisecond: Long): Int {
        var findStart = 0
        if (indexOfCurrentSentence >= 0) { // 如果已经指定了歌词，则现在位置开始
            findStart = indexOfCurrentSentence
        }
        try {
            val lyricTime = lines!![findStart].startTime
            if (millisecond > lyricTime) { // 如果想要查找的时间在现在字幕的时间之后
                // 如果开始位置经是最后一句了，直接返回最后一句。
                if (findStart == (lines.size - 1)) {
                    return findStart
                }
                var new_index = findStart + 1
                // 找到第一句开始时间大于输入时间的歌词
                while ((new_index < lines.size
                            && lines[new_index].startTime <= millisecond)
                ) {
                    ++new_index
                }
                // 这句歌词的前一句就是我们要找的了。
                return new_index - 1
            } else if (millisecond < lyricTime) { // 如果想要查找的时间在现在字幕的时间之前
                // 如果开始位置经是第一句了，直接返回第一句。
                if (findStart == 0) {
                    return 0
                }
                var new_index = findStart - 1
                // 找到开始时间小于输入时间的歌词
                while ((new_index > 0
                            && lines[new_index].startTime > millisecond)
                ) {
                    --new_index
                }
                // 就是它了。
                return new_index
            } else { // 不用找了
                return findStart
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            Log.i(TAG, "新的歌词载入了，所以产生了越界错误，不用理会，返回0")
            return 0
        }
    }

    /**
     * 解析每行歌词文本,一行文本歌词可能对应多个时间戳。
     */
    private fun parseLine(line: String) {
        if ((line == "")) {
            return
        }
        var content: String
        val timeLength: Int
        var index: Int
        val matcher = timePattern.matcher(line)
        var lastIndex = -1 // 最后一个时间标签的下标
        var lastLength = -1 // 最后一个时间标签的长度
        // 一行文本歌词可能对应多个时间戳，如“[01:02.3][01:11:22.import site.doramusic.app.R;]在这阳光明媚的春天里”
// 一行也可能包含多个句子，如“[01:02.3]在这阳光明媚的春天里[01:02:22.33]我的眼泪忍不住流淌”
        val times: MutableList<String> =
            ArrayList()
        // 寻找出本行所有时间戳，存入times中
        while (matcher.find()) { // 匹配的是中括号里的字符串，如01:02.3，01:11:22.33
            val s = matcher.group()
            index = line.indexOf("[$s]")
            if (lastIndex != -1 && index - lastIndex > lastLength + 2) { // 如果大于上次的大小，则中间夹了别的内容在里面
// 这个时候就要分段了
                content = trimBracket(
                    line.substring(
                        lastIndex + lastLength + 2, index
                    )
                )
                for (string: String in times) { // 将每个时间戳对应的一份句子存入句子集合
                    val t = parseTime(string)
                    if (t != -1L) {
                        Log.i(
                            TAG,
                            "line content match-->$content"
                        )
                        lines!!.add(LyricLine(t, content))
                    }
                }
                times.clear()
            }
            times.add(s)
            lastIndex = index
            lastLength = s.length
            Log.i(TAG, "time match--->$s")
        }
        // 如果列表为空，则表示本行没有分析出任何标签
        if (times.isEmpty()) {
            return
        }
        timeLength = lastLength + 2 + lastIndex
        if (timeLength > line.length) {
            content = trimBracket(line.substring(line.length))
        } else {
            content = trimBracket(line.substring(timeLength))
        }
        Log.i(TAG, "line content match-->$content")
        // 将每个时间戳对应的一份句子存入句子集合
        for (s: String in times) {
            val t = parseTime(s)
            if (t != -1L) {
                lines!!.add(LyricLine(t, content))
            }
        }
    }

    /**
     * 去除指定字符串中包含[XXX]形式的字符串。
     */
    private fun trimBracket(content: String): String {
        var s: String
        var result = content
        val matcher = bracketPattern.matcher(content)
        while (matcher.find()) {
            s = matcher.group()
            result = result.replace("[$s]", "")
        }
        return result
    }

    /**
     * 将歌词的时间字符串转化成毫秒数，如果参数是00:01:23.45。
     */
    private fun parseTime(strTime: String): Long {
        var beforeDot = "00:00:00"
        var afterDot: String? = "0"
        // 将字符串按小数点拆分成整秒部分和小数部分。
        val dotIndex = strTime.indexOf(".")
        when {
            dotIndex < 0 -> {
                beforeDot = strTime
            }
            dotIndex == 0 -> {
                afterDot = strTime.substring(1)
            }
            else -> {
                beforeDot = strTime.substring(0, dotIndex) // 00:01:23
                afterDot = strTime.substring(dotIndex + 1) // 45
            }
        }
        var intSeconds: Long = 0
        var counter = 0
        while (beforeDot.length > 0) {
            val colonPos = beforeDot.indexOf(":")
            try {
                if (colonPos > 0) { // 找到冒号了。
                    intSeconds *= 60
                    intSeconds += Integer.valueOf(
                        beforeDot.substring(
                            0,
                            colonPos
                        )
                    ).toLong()
                    beforeDot = beforeDot.substring(colonPos + 1)
                } else if (colonPos < 0) { // 没找到，剩下都当一个数处理了。
                    intSeconds *= 60
                    intSeconds += Integer.valueOf(beforeDot).toLong()
                    beforeDot = ""
                } else { // 第一个就是冒号，不可能！
                    return -1
                }
            } catch (e: NumberFormatException) {
                return -1
            }
            ++counter
            if (counter > 3) { // 不会超过小时，分，秒吧。
                return -1
            }
        }
        val totalTime = String.format("%d.%s", intSeconds, afterDot)
        val doubleSeconds = java.lang.Double.valueOf(totalTime) // 转成小数83.45
        return (doubleSeconds * 1000).toLong() // 转成毫秒8345
    }

    companion object {
        private val TAG = LyricScroller::class.java.simpleName
    }
}