package site.doramusic.app.conf

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
        const val ACTIVITY_CHAT_ROOM = "$GROUP_APP/ChatRoomActivity"

        const val ACTIVITY_GALLERY_LIST = "$GROUP_APP/GalleryListActivity"
        const val ACTIVITY_DRAW_CARD = "$GROUP_APP/DrawCardActivity"
        const val ACTIVITY_GUESSING = "$GROUP_APP/GuessingActivity"
        const val ACTIVITY_GUESSING_RANK = "$GROUP_APP/GuessingRankActivity"

        const val ACTIVITY_GUESSING_REWARD = "$GROUP_APP/GuessingRewardActivity"
    }
}
