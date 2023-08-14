package site.doramusic.app.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics

object SpmUtils {

    const val SPM_ID_OPEN_SCREEN_SPLASH = "open_screen_splash"
    const val SPM_ID_CLOSE_SCREEN_SPLASH = "close_screen_splash"
    const val SPM_ID_OPEN_SCREEN_SETTINGS = "open_screen_settings"
    const val SPM_ID_CLOSE_SCREEN_SETTINGS = "close_screen_settings"
    const val SPM_ID_OPEN_SCREEN_PROTOCOL = "open_screen_protocol"
    const val SPM_ID_CLOSE_SCREEN_PROTOCOL = "close_screen_protocol"
    const val SPM_ID_OPEN_SCREEN_MAIN = "open_screen_main"
    const val SPM_ID_CLOSE_SCREEN_MAIN = "close_screen_main"
    const val SPM_ID_OPEN_SCREEN_EQUALIZER = "open_screen_equalizer"
    const val SPM_ID_CLOSE_SCREEN_EQUALIZER = "close_screen_equalizer"
    const val SPM_ID_OPEN_SCREEN_COLOR_CHOICE = "open_screen_color_choice"
    const val SPM_ID_CLOSE_SCREEN_COLOR_CHOICE = "close_screen_color_choice"
    const val SPM_ID_OPEN_SCREEN_BROWSER = "open_screen_browser"
    const val SPM_ID_CLOSE_SCREEN_BROWSER = "close_screen_browser"
    const val SPM_ID_CLICK_BUTTON_PLAY_PAUSE = "click_button_play_pause"
    const val SPM_ID_CLICK_BUTTON_CHANGE_SKIN = "click_button_change_skin"
    const val SPM_ID_TOGGLE_BUTTON_OPEN_SHAKE = "toggle_button_open_shake"
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_SHAKE = "toggle_close_shake"
    const val SPM_ID_TOGGLE_BUTTON_OPEN_BASS_BOOST = "toggle_button_open_bass_boost"
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_BASS_BOOST = "toggle_button_close_bass_boost"
    const val SPM_ID_TOGGLE_BUTTON_OPEN_AUTO_PLAY = "toggle_button_open_auto_play"
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_AUTO_PLAY = "toggle_button_close_auto_play"
    const val SPM_NAME_SCREEN = "屏幕事件"
    const val SPM_TYPE_SCREEN_OPEN = "打开界面"
    const val SPM_TYPE_SCREEN_CLOSE = "关闭界面"
    const val SPM_NAME_BUTTON_CLICK = "按钮事件"
    const val SPM_TYPE_BUTTON_CLICK = "点击按钮"
    const val SPM_NAME_TOGGLE_BUTTON = "开关事件"
    const val SPM_TYPE_TOGGLE_BUTTON_OPEN = "打开开关"
    const val SPM_TYPE_TOGGLE_BUTTON_CLOSE = "关闭开关"

    fun screenEvent(context: Context, id: String, name: String, contentType: String) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }


    fun Activity.spmScreen(id: String, name: String, contentType: String) {
        screenEvent(this, id, name, contentType)
    }
    fun Fragment.spmScreen(id: String, name: String, contentType: String) {
        screenEvent(requireActivity(), id, name, contentType)
    }

    fun logEvent(context: Context, id: String, name: String, contentType: String) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params)
    }

    fun Activity.spm(id: String, name: String, contentType: String) {
        logEvent(this, id, name, contentType)
    }

    fun Fragment.spm(id: String, name: String, contentType: String) {
        logEvent(requireActivity(), id, name, contentType)
    }
}