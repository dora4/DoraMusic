package site.doramusic.app.media

import android.content.Context
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import dora.util.ToastUtils

import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig

/**
 * 音乐播放模式的控制类。
 */
class PlayModeControl(internal val context: Context) : AppConfig {

    private val mediaManager: MediaManager? = MusicApp.instance!!.mediaManager

    /**
     * 刷新按钮的状态。
     *
     * @param playModeBtn
     */
    fun refreshButtonStatus(playModeBtn: ImageButton) {
        when (mediaManager!!.playMode) {
            AppConfig.MPM_LIST_LOOP_PLAY -> {   //列表循环
                playModeBtn.setImageResource(R.drawable.icon_playmode_list_loop)
            }
            AppConfig.MPM_SINGLE_LOOP_PLAY -> {  //单曲循环
                playModeBtn.setImageResource(R.drawable.icon_playmode_single_loop)
            }
            AppConfig.MPM_RANDOM_PLAY -> {   //随机播放
                playModeBtn.setImageResource(R.drawable.icon_playmode_random)
            }
            AppConfig.MPM_ORDER_PLAY -> {    //顺序播放
                playModeBtn.setImageResource(R.drawable.icon_playmode_order)
            }
        }
    }

    /**
     * 改变播放模式。
     *
     * @param playModeBtn
     */
    fun changePlayMode(playModeBtn: ImageButton) {
        when (mediaManager!!.playMode) {
            AppConfig.MPM_LIST_LOOP_PLAY -> {
                playModeBtn.setImageResource(R.drawable.icon_playmode_single_loop)
                mediaManager.playMode = AppConfig.MPM_SINGLE_LOOP_PLAY
                ToastUtils.showShort(context, "单曲循环")
            }
            AppConfig.MPM_SINGLE_LOOP_PLAY -> {
                playModeBtn.setImageResource(R.drawable.icon_playmode_random)
                mediaManager.playMode = AppConfig.MPM_RANDOM_PLAY
                ToastUtils.showShort(context, "随机播放")
            }
            AppConfig.MPM_RANDOM_PLAY -> {
                playModeBtn.setImageResource(R.drawable.icon_playmode_order)
                mediaManager.playMode = AppConfig.MPM_ORDER_PLAY
                ToastUtils.showShort(context, "顺序播放")
            }
            AppConfig.MPM_ORDER_PLAY -> {
                playModeBtn.setImageResource(R.drawable.icon_playmode_list_loop)
                mediaManager.playMode = AppConfig.MPM_LIST_LOOP_PLAY
                ToastUtils.showShort(context, "列表循环")
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
            AppConfig.MPM_LIST_LOOP_PLAY -> return R.drawable.icon_playmode_list_loop
            AppConfig.MPM_ORDER_PLAY -> return R.drawable.icon_playmode_order
            AppConfig.MPM_RANDOM_PLAY -> return R.drawable.icon_playmode_random
            AppConfig.MPM_SINGLE_LOOP_PLAY -> return R.drawable.icon_playmode_single_loop
        }
        throw IllegalArgumentException("无效的播放模式")
    }

    fun printPlayMode(playMode: Int): String {
        when (playMode) {
            AppConfig.MPM_LIST_LOOP_PLAY -> return "列表循环"
            AppConfig.MPM_ORDER_PLAY -> return "顺序播放"
            AppConfig.MPM_RANDOM_PLAY -> return "随机播放"
            AppConfig.MPM_SINGLE_LOOP_PLAY -> return "单曲循环"
        }
        throw IllegalArgumentException("无效的播放模式")
    }

    fun changePlayMode(textView: TextView, imageView: ImageView) {
        when (mediaManager!!.playMode) {
            AppConfig.MPM_LIST_LOOP_PLAY -> {   //列表循环 -> 顺序播放
                textView.text = "顺序播放"
                mediaManager.playMode = AppConfig.MPM_ORDER_PLAY
                imageView.setImageResource(R.drawable.icon_playmode_order)
            }
            AppConfig.MPM_ORDER_PLAY -> {    //顺序播放 -> 随机播放
                textView.text = "随机播放"
                imageView.setImageResource(R.drawable.icon_playmode_random)
                mediaManager.playMode = AppConfig.MPM_RANDOM_PLAY
            }
            AppConfig.MPM_RANDOM_PLAY -> {   //随机播放 -> 单曲循环
                textView.text = "单曲循环"
                imageView.setImageResource(R.drawable.icon_playmode_single_loop)
                mediaManager.playMode = AppConfig.MPM_SINGLE_LOOP_PLAY
            }
            AppConfig.MPM_SINGLE_LOOP_PLAY -> {  //单曲循环 -> 列表循环
                textView.text = "列表循环"
                imageView.setImageResource(R.drawable.icon_playmode_list_loop)
                mediaManager.playMode = AppConfig.MPM_LIST_LOOP_PLAY
            }
        }
    }
}
