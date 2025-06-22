package site.doramusic.app.util

import android.content.Context
import dora.util.SPUtils

class PrefsManager(val context: Context) {

    fun getColdLaunchAutoPlay(): Boolean {
        return SPUtils.readBoolean(
            context, PREFS_COLD_LAUNCH_AUTO_PLAY,
            false
        )
    }

    fun getColdLaunchAutoConnectVPN(): Boolean {
        return SPUtils.readBoolean(
            context, PREFS_COLD_LAUNCH_AUTO_CONNECT_VPN,
            false
        )
    }

    fun getShakeChangeMusic(): Boolean {
        return SPUtils.readBoolean(context, PREFS_SHAKE_CHANGE_MUSIC, true)
    }

    fun getBassBoost(): Boolean {
        return SPUtils.readBoolean(context, PREFS_BASS_BOOST_ENABLE, false)
    }

    fun getFilterSize(): Boolean {
        return SPUtils.readBoolean(context, PREFS_FILTER_SIZE, false)
    }

    fun getFilterTime(): Boolean {
        return SPUtils.readBoolean(context, PREFS_FILTER_TIME, false)
    }

    fun getEqualizerDecibels(): String {
        return SPUtils.readString(context, PREFS_EQUALIZER_DECIBELS, "")
    }

    fun getSkinType(): Int {
        return SPUtils.readInteger(context, PREFS_SKIN_TYPE, 1)
    }

    fun getSkinColor(): Int {
        return SPUtils.readInteger(context, PREFS_SKIN_COLOR, 0)
    }

    fun saveColdLaunchAutoPlay(flag: Boolean) {
        SPUtils.writeBoolean(context, PREFS_COLD_LAUNCH_AUTO_PLAY, flag)
    }

    fun saveColdLaunchAutoConnectVPN(flag: Boolean) {
        SPUtils.writeBoolean(context, PREFS_COLD_LAUNCH_AUTO_CONNECT_VPN, flag)
    }

    fun saveShakeChangeMusic(flag: Boolean) {
        SPUtils.writeBoolean(context, PREFS_SHAKE_CHANGE_MUSIC, flag)
    }

    fun saveBassBoost(flag: Boolean) {
        SPUtils.writeBoolean(context, PREFS_BASS_BOOST_ENABLE, flag)
    }

    fun saveFilterSize(size: Boolean) {
        SPUtils.writeBoolean(context, PREFS_FILTER_SIZE, size)
    }

    fun saveFilterTime(time: Boolean) {
        SPUtils.writeBoolean(context, PREFS_FILTER_TIME, time)
    }

    fun saveEqualizerDecibels(decibels: IntArray) {
        val sb = StringBuilder()
        for (i in decibels.indices) {
            sb.append(decibels[i])
            if (i != decibels.size - 1) {
                sb.append(",")
            }
        }
        SPUtils.writeString(context, PREFS_EQUALIZER_DECIBELS, sb.toString())
    }

    fun saveSkinType(skinType: Int) {
        SPUtils.writeInteger(context, PREFS_SKIN_TYPE, skinType)
    }

    companion object {

        /**
         * 过滤小文件。
         */
        const val PREFS_FILTER_SIZE = "prefs_filter_size"

        /**
         * 过滤时长短的文件。
         */
        const val PREFS_FILTER_TIME = "prefs_filter_time"

        /**
         * 启用冷启动自动播放歌曲。
         */
        const val PREFS_COLD_LAUNCH_AUTO_PLAY = "prefs_auto_play"

        /**
         * 启用冷启动自动连接VPN。
         */
        const val PREFS_COLD_LAUNCH_AUTO_CONNECT_VPN = "prefs_auto_vpn"

        /**
         * 启用摇一摇切换歌曲。
         */
        const val PREFS_SHAKE_CHANGE_MUSIC = "prefs_shake"

        /**
         * 启用重低音。
         */
        const val PREFS_BASS_BOOST_ENABLE = "prefs_bass_boost_enable"

        /**
         * 重低音参数。
         */
        const val PREFS_EQUALIZER_DECIBELS = "prefs_equalizer_decibels"

        /**
         * 皮肤类型为0时的自定义颜色。
         */
        const val PREFS_SKIN_COLOR = "prefs_skin_color"

        /**
         * 皮肤类型：
         * 1.初恋青，#04D5D5
         * 2.活力橙，#FF6600
         * 3.曜石黑，#292421
         * 4.叶绿，#009944
         * 5.鸽血红，#DB0000
         * 6.天蓝，#389CFF
         * 7.魅惑紫，#BB86FC
         * 8.金黄，#FFD700
         * 9.少女粉，#FFC0CB
         * 10.暗金，#C8B245
         */
        const val PREFS_SKIN_TYPE = "prefs_skin_type"
    }
}
