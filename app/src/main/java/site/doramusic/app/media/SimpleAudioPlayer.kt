package site.doramusic.app.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import dora.util.LogUtils

import java.io.IOException

/**
 * 考虑了AudioFocus的简单音频播放器，用于临时播放音频，不会加入播放列表。
 */
class SimpleAudioPlayer(private var context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusListener: AudioManager.OnAudioFocusChangeListener = SimpleAudioFocusChangeListener()

    /**
     * 请求音频焦点，Android系统会自动管理这些音频的播放与暂停。
     */
    private fun requestFocus(): Boolean {
        val result = audioManager.requestAudioFocus(audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    /**
     * 调用这个方法之前不能调用其他play方法，调用后必须exit才可以调用其他play方法。
     *
     * @param rawId
     */
    fun playByRawId(rawId: Int) {
        mediaPlayer = MediaPlayer.create(context, rawId)
        if (requestFocus()) mediaPlayer?.start()
    }

    /**
     * 用来临时播放在线歌曲。
     */
    fun playByUrl(url: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        if (requestFocus()) {
            try {
                mediaPlayer?.stop()
                //防止第二次调用报java.lang.IllegalStateException
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(url)
                mediaPlayer?.prepare()
                mediaPlayer?.setOnPreparedListener {
                    mediaPlayer?.start()
                }
            } catch (e: IllegalArgumentException) {
                mediaPlayer?.release()
                LogUtils.e("歌曲播放失败:$url,${e.message}")
            } catch (e: IOException) {
                mediaPlayer?.release()
                LogUtils.e("歌曲播放失败:$url,${e.message}")
            }
        }
    }

    fun replay() {
        if (requestFocus()) mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun exit() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private inner class SimpleAudioFocusChangeListener : AudioManager.OnAudioFocusChangeListener {

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                AudioManager.AUDIOFOCUS_GAIN -> replay()
                AudioManager.AUDIOFOCUS_LOSS -> pause()
            }
        }
    }
}
