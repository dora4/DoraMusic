package site.doramusic.app.event

/**
 * 播放器背景变化事件。
 *
 * @param backgroundRes 当前背景资源。
 * 为 null 时表示恢复默认背景。
 */
data class PlayerBackgroundChangedEvent(
    val backgroundRes: Int?
)