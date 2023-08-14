package site.doramusic.app.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics

object SpmUtils {

    const val SPM_ID_OPEN_SCREEN_SPLASH = 100001
    const val SPM_ID_CLOSE_SCREEN_SPLASH = 100002
    const val SPM_ID_OPEN_SCREEN_SETTINGS = 100003
    const val SPM_ID_CLOSE_SCREEN_SETTINGS = 100004
    const val SPM_ID_OPEN_SCREEN_PROTOCOL = 100005
    const val SPM_ID_CLOSE_SCREEN_PROTOCOL = 100006
    const val SPM_ID_OPEN_SCREEN_MAIN = 100007
    const val SPM_ID_CLOSE_SCREEN_MAIN = 100008
    const val SPM_ID_OPEN_SCREEN_EQUALIZER = 100009
    const val SPM_ID_CLOSE_SCREEN_EQUALIZER = 100010
    const val SPM_ID_OPEN_SCREEN_COLOR_CHOICE = 100011
    const val SPM_ID_CLOSE_SCREEN_COLOR_CHOICE = 100012
    const val SPM_ID_OPEN_SCREEN_BROWSER = 100013
    const val SPM_ID_CLOSE_SCREEN_BROWSER = 100014
    const val SPM_ID_CLICK_BUTTON_PLAY_PAUSE = 100015
    const val SPM_ID_CLICK_BUTTON_CHANGE_SKIN = 100016
    const val SPM_ID_TOGGLE_BUTTON_OPEN_SHAKE = 100017
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_SHAKE = 100018
    const val SPM_ID_TOGGLE_BUTTON_OPEN_BASS_BOOST = 100019
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_BASS_BOOST = 100020
    const val SPM_ID_TOGGLE_BUTTON_OPEN_AUTO_PLAY = 100021
    const val SPM_ID_TOGGLE_BUTTON_CLOSE_AUTO_PLAY = 100022
    const val SPM_NAME_SCREEN = "屏幕事件"
    const val SPM_TYPE_SCREEN_OPEN = "打开界面"
    const val SPM_TYPE_SCREEN_CLOSE = "关闭界面"
    const val SPM_NAME_BUTTON_CLICK = "按钮事件"
    const val SPM_TYPE_BUTTON_CLICK = "点击按钮"
    const val SPM_NAME_TOGGLE_BUTTON = "开关事件"
    const val SPM_TYPE_TOGGLE_BUTTON_OPEN = "打开开关"
    const val SPM_TYPE_TOGGLE_BUTTON_CLOSE = "关闭开关"

    fun screenEvent(context: Context, id: Int, name: String, contentType: String) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        params.putInt(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }


    fun Activity.spmScreen(id: Int, name: String, contentType: String) {
        screenEvent(this, id, name, contentType)
    }
    fun Fragment.spmScreen(id: Int, name: String, contentType: String) {
        screenEvent(requireActivity(), id, name, contentType)
    }

    fun logEvent(context: Context, id: Int, name: String, contentType: String) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        params.putInt(FirebaseAnalytics.Param.ITEM_ID, id)
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params)
    }

    fun Activity.spm(id: Int, name: String, contentType: String) {
        logEvent(this, id, name, contentType)
    }

    fun Fragment.spm(id: Int, name: String, contentType: String) {
        logEvent(requireActivity(), id, name, contentType)
    }
}