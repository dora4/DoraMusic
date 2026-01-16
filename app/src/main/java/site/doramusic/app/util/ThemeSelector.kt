package site.doramusic.app.util

import android.content.Context
import android.view.View
import dora.skin.SkinManager
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME

object ThemeSelector {

    fun applyViewTheme(view: View) {
        view.setBackgroundColor(getThemeColor(view.context))
    }

    fun getThemeColor(context: Context) : Int {
        val prefsManager = PrefsManager(context)
        val curSkinSuffix = SkinManager.getCurSkinSuffix()
        return if (curSkinSuffix == "custom") {
            prefsManager.getSkinColor()
        } else {
            SkinManager.getLoader().getColor(COLOR_THEME)
        }
    }
}