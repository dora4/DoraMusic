package site.doramusic.app.track

/**
 * 埋点事件。
 */
interface EventType {

    companion object {

        /**
         * 点击横幅广告。
         */
        const val EVENT_TYPE_AD_EXPOSURE = "ad_exposure"

        /**
         * 反馈。
         */
        const val EVENT_TYPE_FEEDBACK = "feedback"

        /**
         * 通知栏收藏歌曲。
         */
        const val EVENT_TYPE_STAR_FROM_NOTIFICATION = "star_from_notification"

        /**
         * 通知栏取消收藏歌曲。
         */
        const val EVENT_TYPE_UNSTAR_FROM_NOTIFICATION = "unstar_from_notification"

        /**
         * 播放音乐。
         */
        const val EVENT_TYPE_PLAY_MUSIC = "play_music"

        /**
         * 暂停音乐。
         */
        const val EVENT_TYPE_PAUSE_MUSIC = "pause_music"

        /**
         * 显示快捷播放列表。
         */
        const val EVENT_TYPE_SHOW_QUICK_PLAYLIST = "show_quick_playlist"


        /**
         * 打开播放控制界面。
         */
        const val EVENT_TYPE_OPEN_PLAYER = "open_player"


        /**
         * 关闭播放控制界面。
         */
        const val EVENT_TYPE_CLOSE_PLAYER = "close_player"

        /**
         * 从「我的歌曲」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_LOCAL = "playlist_from_local"

        /**
         * 从「歌手」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_ARTIST = "playlist_from_artist"

        /**
         * 从「专辑」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_ALBUM = "playlist_from_album"

        /**
         * 从「文件夹」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_FOLDER = "playlist_from_folder"

        /**
         * 从「我的收藏」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_FAVORITE = "playlist_from_favorite"

        /**
         * 从「最近播放」进入歌曲列表。
         */
        const val EVENT_TYPE_PLAYLIST_FROM_LATEST = "playlist_from_latest"

        /**
         * 打开登录页面。
         */
        const val EVENT_TYPE_SIGN_IN = "sign_in"

        /**
         * 注销登录。
         */
        const val EVENT_TYPE_SIGN_OUT = "sign_out"

        /**
         * 检测版本更新。
         */
        const val EVENT_TYPE_CHECK_UPDATE = "check_update"

        /**
         * 开启自动播放。
         */
        const val EVENT_TYPE_ENABLE_AUTO_PLAY = "enable_auto_play"

        /**
         * 开启摇一摇。
         */
        const val EVENT_TYPE_ENABLE_SHAKING = "enable_shaking"

        /**
         * 开启深色模式。
         */
        const val EVENT_TYPE_ENABLE_DARK_MODE = "enable_dark_mode"

        /**
         * 开启重低音。
         */
        const val EVENT_TYPE_ENABLE_BASS_BOOST = "enable_bass_boost"

        /**
         * 显示横幅。
         */
        const val EVENT_TYPE_ENABLE_BANNER = "enable_banner"

        /**
         * 保持屏幕常亮。
         */
        const val EVENT_TYPE_ENABLE_KEEPING_SCREEN_ON = "enable_keeping_screen_on"

        /**
         * 关闭自动播放。
         */
        const val EVENT_TYPE_DISABLE_AUTO_PLAY = "disable_auto_play"

        /**
         * 关闭摇一摇。
         */
        const val EVENT_TYPE_DISABLE_SHAKING = "disable_shaking"

        /**
         * 关闭深色模式。
         */
        const val EVENT_TYPE_DISABLE_DARK_MODE = "disable_dark_mode"

        /**
         * 关闭重低音。
         */
        const val EVENT_TYPE_DISABLE_BASS_BOOST = "disable_bass_boost"

        /**
         * 不显示横幅。
         */
        const val EVENT_TYPE_DISABLE_BANNER = "disable_banner"

        /**
         * 不保持屏幕常亮。
         */
        const val EVENT_TYPE_DISABLE_KEEPING_SCREEN_ON = "disable_keeping_screen_on"

        /**
         * 扫描歌曲。
         */
        const val EVENT_TYPE_SCAN_MUSIC = "scan_music"

        /**
         * 进入聊天室。
         */
        const val EVENT_TYPE_CHAT_ROOM = "chat_room"

        /**
         * 进入「我的图鉴」。
         */
        const val EVENT_TYPE_GALLERY = "gallery"

        /**
         * 换肤。
         */
        const val EVENT_TYPE_CHANGE_SKIN = "change_skin"

        /**
         * 均衡器。
         */
        const val EVENT_TYPE_EQUALIZER = "equalizer"

        /**
         * 竞猜。
         */
        const val EVENT_TYPE_GUESSING = "guessing"

        /**
         * 分享给好友。
         */
        const val EVENT_TYPE_SHARE = "share"

        /**
         * 查看用户协议和隐私政策。
         */
        const val EVENT_TYPE_AGREEMENT = "agreement"

        /**
         * 代理播放。
         */
        const val EVENT_TYPE_PROXY_PLAYBACK = "proxy_playback"
    }
}