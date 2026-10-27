package site.doramusic.app.util

import android.content.Context
import android.content.res.Configuration

object PadUtils {

    /**
     * 是否平板（短边>=600dp，Android标准大屏）。
     */
    fun isPad(context: Context): Boolean {
        val metrics = context.resources.displayMetrics
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        val minDp = minOf(widthDp, heightDp)
        return minDp >= 600
    }

    /**
     * 是否横屏（当前方向：宽 > 高）。
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 是否横屏平板。
     */
    fun isPadLandscape(context: Context): Boolean {
        return isPad(context) && isLandscape(context)
    }
}
