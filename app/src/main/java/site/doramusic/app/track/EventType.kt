package site.doramusic.app.track

/**
 * 埋点事件。
 */
interface EventType {

    companion object {

        /**
         * 启动APP（热启动，进入主界面就算）。
         */
        const val EVENT_TYPE_LAUNCH = "launch"

        /**
         * 横幅广告曝光。
         */
        const val EVENT_TYPE_AD_EXPOSURE = "ad_exposure"

        /**
         * 点击横幅广告。
         */
        const val EVENT_TYPE_CLICK_AD = "click_ad"

        /**
         * 进入歌曲列表。
         */
        const val EVENT_TYPE_MUSIC_LIST = "music_list"

        /**
         * 字母导航。
         */
        const val EVENT_TYPE_LETTER_VIEW = "letter_view"

        /**
         * 点击歌曲条目进行播放。
         */
        const val EVENT_TYPE_CLICK_MUSIC_ITEM = "click_music_item"

        /**
         * 想要使用定时暂停。
         */
        const val EVENT_TYPE_SLEEP_TIMER_POPUP = "sleep_timer_popup"

        /**
         * 定时暂停时间选择并开始计时。
         */
        const val EVENT_TYPE_START_SLEEP_TIMER = "start_sleep_timer"

        /**
         * 定时暂停增加时间。
         */
        const val EVENT_TYPE_INCREASE_SLEEP_TIMER = "increase_sleep_timer"

        /**
         * 定时暂停减少时间。
         */
        const val EVENT_TYPE_DECREASE_SLEEP_TIMER = "decrease_sleep_timer"

        /**
         * 取消定时暂停。
         */
        const val EVENT_TYPE_CANCEL_SLEEP_TIMER = "cancel_sleep_timer"

        /**
         * 打开侧边栏。
         */
        const val EVENT_TYPE_SLIDING_DRAWER = "sliding_drawer"

        /**
         * 打开扫描歌曲界面。
         */
        const val EVENT_TYPE_SCAN_MUSIC = "scan_music"

        /**
         * 想要打开聊天室界面。
         */
        const val EVENT_TYPE_WANNA_CHAT_ROOM = "wanna_chat_room"

        /**
         * 打开聊天室界面。
         */
        const val EVENT_TYPE_CHAT_ROOM = "chat_room"

        /**
         * 进入「我的图鉴」。
         */
        const val EVENT_TYPE_GALLERY = "gallery"

        /**
         * 打开换肤界面。
         */
        const val EVENT_TYPE_SELECT_THEME_COLOR = "select_theme_color"

        /**
         * 换肤。
         */
        const val EVENT_TYPE_CHANGE_SKIN = "change_skin"

        /**
         * 打开更换背景界面。
         */
        const val EVENT_TYPE_SELECT_PLAYER_BG = "select_player_bg"

        /**
         * 更换背景。
         */
        const val EVENT_TYPE_CHANGE_BG = "change_bg"

        /**
         * 打开均衡器界面。
         */
        const val EVENT_TYPE_EQUALIZER = "equalizer"

        /**
         * 关闭均衡器。
         */
        const val EVENT_TYPE_CLOSE_EQUALIZER = "close_equalizer"

        /**
         * 使用均衡器。
         */
        const val EVENT_TYPE_USE_EQUALIZER = "use_equalizer"

        /**
         * 打开设置界面。
         */
        const val EVENT_TYPE_SETTINGS = "settings"

        /**
         * 显示快捷播放列表。
         */
        const val EVENT_TYPE_QUICK_PLAYLIST = "quick_playlist"

        /**
         * 切换播放模式。
         */
        const val EVENT_TYPE_CHANGE_PLAY_MODE = "change_play_mode"

        /**
         * 打开播放控制界面。
         */
        const val EVENT_TYPE_PLAYER = "player"

        /**
         * 打开播放控制界面。
         */
        @Deprecated("")
        const val EVENT_TYPE_OPEN_PLAYER = "open_player"

        /**
         * 关闭播放控制界面。
         */
        @Deprecated("")
        const val EVENT_TYPE_CLOSE_PLAYER = "close_player"

        /**
         * 收藏歌曲。
         */
        const val EVENT_TYPE_STAR_MUSIC = "star_music"

        /**
         * 取消收藏歌曲。
         */
        const val EVENT_TYPE_UNSTAR_MUSIC = "unstar_music"

        /**
         * 拖拽播放进度条（拖拽结束时调用，不要频繁调用）。
         */
        const val EVENT_TYPE_SEEK_MUSIC_PROGRESS_BAR = "seek_music_progress_bar"

        /**
         * 拖拽音量调节进度条（拖拽结束时调用，不要频繁调用）。
         */
        const val EVENT_TYPE_SEEK_VOLUME_PROGRESS_BAR = "seek_volume_progress_bar"

        /**
         * 显示音量调节进度条。
         */
        const val EVENT_TYPE_VOLUME_PROGRESS_BAR = "volume_progress_bar"

        /**
         * 反馈。
         */
        const val EVENT_TYPE_FEEDBACK = "feedback"

        /**
         * 播放音乐。
         */
        @Deprecated("使用 EVENT_TYPE_PLAY_FINISH_COMPLETE 和 EVENT_TYPE_PLAY_FINISH_PARTIAL替换")
        const val EVENT_TYPE_PLAY_MUSIC = "play_music"

        /**
         * 暂停音乐。
         */
        @Deprecated("使用 EVENT_TYPE_PLAY_FINISH_COMPLETE 和 EVENT_TYPE_PLAY_FINISH_PARTIAL替换")
        const val EVENT_TYPE_PAUSE_MUSIC = "pause_music"

        /**
         * 完整听完一首歌（播放结束回调onCompletion）。
         * params: played_ms
         */
        const val EVENT_TYPE_PLAY_FINISH_COMPLETE = "play_finish_complete"

        /**
         * 未听完歌曲（切歌/停止/退出/失去焦点等中途结束）。
         * params: played_ms
         */
        const val EVENT_TYPE_PLAY_FINISH_PARTIAL = "play_finish_partial"

        /**
         * 打开登录页面。
         */
        const val EVENT_TYPE_WANNA_SIGN_IN = "wanna_sign_in"

        /**
         * 注销登录。
         */
        const val EVENT_TYPE_SIGN_OUT = "sign_out"

        /**
         * 在扫描歌曲界面点击添加音乐按钮添加音乐。
         */
        const val EVENT_TYPE_ADD_MUSIC = "add_music"

        /**
         * 进入图鉴抽卡界面。
         */
        const val EVENT_TYPE_WANNA_DRAW_CARD = "wanna_draw_card"

        /**
         * 完成图鉴。
         */
        const val EVENT_TYPE_COMPLETE_GALLERY = "complete_gallery"

        /**
         * 检测版本更新。
         */
        const val EVENT_TYPE_CHECK_UPDATE = "check_update"

        /**
         * 切换自动播放开关。
         */
        const val EVENT_TYPE_SWITCH_AUTO_PLAY = "switch_auto_play"

        /**
         * 切换摇一摇开关。
         */
        const val EVENT_TYPE_SWITCH_SHAKING = "switch_shaking"

        /**
         * 切换深色模式开关。
         */
        const val EVENT_TYPE_SWITCH_DARK_MODE = "switch_dark_mode"

        /**
         * 切换重低音开关。
         */
        const val EVENT_TYPE_SWITCH_BASS_BOOST = "switch_bass_boost"

        /**
         * 切换横幅开关（反过来）。
         */
        const val EVENT_TYPE_SWITCH_BANNER = "switch_banner"

        /**
         * 保持屏幕常亮。
         */
        const val EVENT_TYPE_SWITCH_KEEPING_SCREEN_ON = "switch_keeping_screen_on"

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