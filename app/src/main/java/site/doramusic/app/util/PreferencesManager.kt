package site.doramusic.app.util

import android.content.Context
import dora.util.SPUtils

class PreferencesManager(val context: Context) {

    fun getColdLaunchAutoPlay(): Boolean {
        return SPUtils.readBoolean(
            context, PREFS_COLD_LAUNCH_AUTO_PLAY,
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

    fun saveSkinColor(skinColor: Int) {
        SPUtils.writeInteger(context, PREFS_SKIN_COLOR, skinColor)
    }

    fun saveFirstLoading(isFirst: Boolean) {
        SPUtils.writeBoolean(context, FIRST_LOADING, isFirst)
    }

    companion object {

        const val PREFS_FILTER_SIZE = "prefs_filter_size"
        const val PREFS_FILTER_TIME = "prefs_filter_time"

        const val PREFS_COLD_LAUNCH_AUTO_PLAY = "prefs_auto_play"
        const val PREFS_SHAKE_CHANGE_MUSIC = "prefs_shake"
        const val PREFS_BASS_BOOST_ENABLE = "prefs_bass_boost_enable"
        const val PREFS_HOT_FIX_ENABLE = "prefs_hot_fix_enable"
        const val PREFS_EQUALIZER_DECIBELS = "prefs_equalizer_decibels"
        const val PREFS_SKIN_COLOR = "prefs_skin_color"
        const val PREFS_SKIN_TYPE = "prefs_skin_type" // 皮肤类型 1.红 2.橙 3.黑 4.绿 5.青 6.蓝 7.紫
        const val FIRST_LOADING = "first_loading"
    }
}
