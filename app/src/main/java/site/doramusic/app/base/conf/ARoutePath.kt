package site.doramusic.app.base.conf

interface ARoutePath {

    companion object {

        private const val GROUP_APP = "/app/ui/activity"
        const val ACTIVITY_MAIN = "$GROUP_APP/MainActivity"
        const val ACTIVITY_EQUALIZER = "$GROUP_APP/EqualizerActivity"
        const val ACTIVITY_SETTINGS = "$GROUP_APP/SettingsActivity"
        const val ACTIVITY_SPLASH = "$GROUP_APP/SplashActivity"
        const val ACTIVITY_COLOR_PICKER = "$GROUP_APP/ColorPickerActivity"
        const val ACTIVITY_PROTOCOL = "$GROUP_APP/ProtocolActivity"
        const val ACTIVITY_BROWSER = "$GROUP_APP/BrowserActivity"
        const val ACTIVITY_DONATION = "$GROUP_APP/DonationActivity"
    }
}