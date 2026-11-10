package site.doramusic.app.stat

import android.content.Context
import site.doramusic.app.R
import java.util.Locale

/**
 * 听歌统计返回模型。
 * totalDuration：总时长 毫秒
 * totalCount：歌曲首数
 */
data class PlayStat(
    var totalDuration: Long = 0L,
    var totalCount: Int = 0
) {

    /**
     * 毫秒格式化：6小时23分钟。
     * 使用plurals资源自动适配各语言单复数
     */
    fun formatDuration(context: Context): String {
        val totalMin = totalDuration / 1000 / 60
        val h = (totalMin / 60).toInt()
        val m = (totalMin % 60).toInt()
        val res = context.resources
        return if (h > 0) {
            val hourText = res.getQuantityString(R.plurals.stat_hour, h, h)
            val minuteText = res.getQuantityString(R.plurals.stat_minute, m, m)
            if (Locale.getDefault().language == "zh") {
                // 中文无空格拼接
                "$hourText$minuteText"
            } else {
                // 其他语言中间加空格
                "$hourText $minuteText"
            }
        } else {
            res.getQuantityString(R.plurals.stat_minute, m, m)
        }
    }
}
