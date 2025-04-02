package site.doramusic.app.event

/**
 * 刷新播放状态和进度事件。
 */
class PlayMusicEvent(val playState: Int, val pendingProgress: Int) {
}