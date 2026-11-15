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
     * 毫秒格式化：
     * - h>0, m=0 → 只显示小时，例：6小时
     * - h>0, m>0 → 6小时23分钟（中文无空格，其他语言空格分隔）
     * - h=0, m>0 → 23分钟
     * - h=0, m=0 → 0分钟
     * 使用plurals资源自动适配各语言单复数
     */
    fun formatDuration(context: Context): String {
        val totalMin = totalDuration / 1000 / 60
        val h = (totalMin / 60).toInt()
        val m = (totalMin % 60).toInt()
        val res = context.resources

        return when {
            h > 0 && m == 0 -> {
                // 只有小时，分钟为0，只展示小时
                res.getQuantityString(R.plurals.stat_hour, h, h)
            }
            h > 0 && m > 0 -> {
                val hourText = res.getQuantityString(R.plurals.stat_hour, h, h)
                val minuteText = res.getQuantityString(R.plurals.stat_minute, m, m)
                if (Locale.getDefault().language == "zh") {
                    "$hourText$minuteText"
                } else {
                    "$hourText $minuteText"
                }
            }
            else -> {
                // h=0，只展示分钟（包含m=0，输出0分钟）
                res.getQuantityString(R.plurals.stat_minute, m, m)
            }
        }
    }
}
