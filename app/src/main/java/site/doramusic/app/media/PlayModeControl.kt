package site.doramusic.app.media

import android.content.Context
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import dora.util.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis

/**
 * 音乐播放模式的控制类。
 */
class PlayModeControl(private val context: Context) : AppConfig {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 刷新按钮的状态。
     *
     * @param playModeBtn
     */
    fun refreshButtonStatus(playModeBtn: ImageButton) {
        when (MediaManager.playMode) {
            AppConfig.MPM_PLAYLIST_LOOP -> {   // 列表循环
                playModeBtn.setImageResource(R.drawable.ic_playmode_list_loop)
            }
            AppConfig.MPM_SINGLE_TRACK_LOOP -> {  // 单曲循环
                playModeBtn.setImageResource(R.drawable.ic_playmode_single_loop)
            }
            AppConfig.MPM_SHUFFLE_PLAYBACK -> {   // 随机播放
                playModeBtn.setImageResource(R.drawable.ic_playmode_random)
            }
            AppConfig.MPM_SEQUENTIAL_PLAYBACK -> {    // 顺序播放
                playModeBtn.setImageResource(R.drawable.ic_playmode_order)
            }
        }
    }

    /**
     * 改变播放模式。
     *
     * @param playModeBtn
     */
    fun changePlayMode(playModeBtn: ImageButton) {
        when (MediaManager.playMode) {
            AppConfig.MPM_PLAYLIST_LOOP -> {   // 列表循环 -> 顺序播放
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "player",
                        "from" to "playlist loop",
                        "to" to "sequential playback",
                    )
                )
                playModeBtn.setImageResource(R.drawable.ic_playmode_order)
                MediaManager.playMode = AppConfig.MPM_SEQUENTIAL_PLAYBACK
                ToastUtils.showShort(context.getString(R.string.sequential_playback))
            }
            AppConfig.MPM_SEQUENTIAL_PLAYBACK -> {   // 顺序播放 -> 随机播放
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "player",
                        "from" to "sequential playback",
                        "to" to "shuffle playback",
                    )
                )
                playModeBtn.setImageResource(R.drawable.ic_playmode_random)
                MediaManager.playMode = AppConfig.MPM_SHUFFLE_PLAYBACK
                ToastUtils.showShort(context.getString(R.string.shuffle_playback))
            }
            AppConfig.MPM_SHUFFLE_PLAYBACK -> {   // 随机播放 -> 单曲循环
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "player",
                        "from" to "shuffle playback",
                        "to" to "single track loop",
                    )
                )
                playModeBtn.setImageResource(R.drawable.ic_playmode_single_loop)
                MediaManager.playMode = AppConfig.MPM_SINGLE_TRACK_LOOP
                ToastUtils.showShort(context.getString(R.string.single_track_loop))
            }
            AppConfig.MPM_SINGLE_TRACK_LOOP -> {  // 单曲循环 -> 列表循环
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "player",
                        "from" to "single track loop",
                        "to" to "playlist loop",
                    )
                )
                playModeBtn.setImageResource(R.drawable.ic_playmode_list_loop)
                MediaManager.playMode = AppConfig.MPM_PLAYLIST_LOOP
                ToastUtils.showShort(context.getString(R.string.playlist_loop))
            }
        }
    }

    /**
     * 获取播放模式的图片。
     *
     * @param playMode
     * @return
     */
    fun getPlayModeImage(playMode: Int): Int {
        when (playMode) {
            AppConfig.MPM_PLAYLIST_LOOP -> return R.drawable.ic_playmode_list_loop
            AppConfig.MPM_SEQUENTIAL_PLAYBACK -> return R.drawable.ic_playmode_order
            AppConfig.MPM_SHUFFLE_PLAYBACK -> return R.drawable.ic_playmode_random
            AppConfig.MPM_SINGLE_TRACK_LOOP -> return R.drawable.ic_playmode_single_loop
        }
        throw IllegalArgumentException("无效的播放模式")
    }

    fun printPlayMode(playMode: Int): String {
        when (playMode) {
            AppConfig.MPM_PLAYLIST_LOOP -> return context.getString(R.string.playlist_loop)
            AppConfig.MPM_SEQUENTIAL_PLAYBACK -> return context.getString(R.string.sequential_playback)
            AppConfig.MPM_SHUFFLE_PLAYBACK -> return context.getString(R.string.shuffle_playback)
            AppConfig.MPM_SINGLE_TRACK_LOOP -> return context.getString(R.string.single_track_loop)
        }
        return ""
    }

    fun changePlayMode(textView: TextView, imageView: ImageView) {
        when (MediaManager.playMode) {
            AppConfig.MPM_PLAYLIST_LOOP -> {   // 列表循环 -> 顺序播放
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                            "source" to "quick",
                            "from" to "playlist loop",
                            "to" to "sequential playback",
                        )
                )
                textView.text = context.getString(R.string.sequential_playback)
                MediaManager.playMode = AppConfig.MPM_SEQUENTIAL_PLAYBACK
                imageView.setImageResource(R.drawable.ic_playmode_order)
            }
            AppConfig.MPM_SEQUENTIAL_PLAYBACK -> {    // 顺序播放 -> 随机播放
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "quick",
                        "from" to "sequential playback",
                        "to" to "shuffle playback",
                    )
                )
                textView.text = context.getString(R.string.shuffle_playback)
                imageView.setImageResource(R.drawable.ic_playmode_random)
                MediaManager.playMode = AppConfig.MPM_SHUFFLE_PLAYBACK
            }
            AppConfig.MPM_SHUFFLE_PLAYBACK -> {   // 随机播放 -> 单曲循环
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "quick",
                        "from" to "shuffle playback",
                        "to" to "single track loop",
                    )
                )
                textView.text = context.getString(R.string.single_track_loop)
                imageView.setImageResource(R.drawable.ic_playmode_single_loop)
                MediaManager.playMode = AppConfig.MPM_SINGLE_TRACK_LOOP
            }
            AppConfig.MPM_SINGLE_TRACK_LOOP -> {  // 单曲循环 -> 列表循环
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CHANGE_PLAY_MODE,
                    mapOf(
                        "source" to "quick",
                        "from" to "single track loop",
                        "to" to "playlist loop",
                    )
                )
                textView.text = context.getString(R.string.playlist_loop)
                imageView.setImageResource(R.drawable.ic_playmode_list_loop)
                MediaManager.playMode = AppConfig.MPM_PLAYLIST_LOOP
            }
        }
    }
}
