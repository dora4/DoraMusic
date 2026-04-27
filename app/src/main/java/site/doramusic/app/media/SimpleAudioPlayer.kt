package site.doramusic.app.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import dora.util.LogUtils

import java.io.IOException

/**
 * 考虑了AudioFocus的简单音频播放器，用于临时播放音频，不会加入播放列表。
 */
class SimpleAudioPlayer(private val context: Context) {

    private var userPaused = false

    private var mediaPlayer: MediaPlayer? = null

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioFocusListener = SimpleAudioFocusChangeListener()

    private var stateListener: OnStateChangeListener? = null
    private var completeListener: OnPlayCompleteListener? = null

    fun setOnStateChangeListener(listener: OnStateChangeListener) {
        this.stateListener = listener
    }

    interface OnStateChangeListener {
        fun onPlay()
        fun onPause()
        fun onStop()
    }

    fun setOnPlayCompleteListener(listener: OnPlayCompleteListener) {
        this.completeListener = listener
    }

    interface OnPlayCompleteListener {
        fun onComplete()
    }

    // ===================== 状态机 =====================
    private enum class State {
        IDLE,
        PREPARING,
        PLAYING,
        PAUSED,
        STOPPED
    }

    private var state = State.IDLE
    private var isPlaying = false

    fun isPlaying(): Boolean {
        return state == State.PLAYING && mediaPlayer?.isPlaying == true
    }

    // ===================== 音频焦点 =====================
    private fun requestFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    // ===================== 播放 URL =====================
    fun playByUrl(url: String) {
        if (!requestFocus()) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer?.reset()
            state = State.PREPARING
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
                state = State.PLAYING
                stateListener?.onPlay()
                isPlaying = true
            }
            mediaPlayer?.setOnCompletionListener {
                completeListener?.onComplete()
                state = State.STOPPED
                isPlaying = false
            }
        } catch (e: IllegalArgumentException) {
            releasePlayer()
            LogUtils.e("playByUrl error: ${e.message}")
        } catch (e: IOException) {
            releasePlayer()
            LogUtils.e("playByUrl error: ${e.message}")
        }
    }

    fun playByUri(uri: Uri) {
        if (!requestFocus()) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer?.reset()
            state = State.PREPARING
            mediaPlayer?.setDataSource(context, uri)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
                state = State.PLAYING
                stateListener?.onPlay()
                isPlaying = true
            }
            mediaPlayer?.setOnCompletionListener {
                completeListener?.onComplete()
                state = State.STOPPED
                isPlaying = false
            }
        } catch (e: SecurityException) {
            LogUtils.e("playByUri permission denied: ${e.message}")
            releasePlayer()
        } catch (e: IOException) {
            LogUtils.e("playByUri IO error: ${e.message}")
            releasePlayer()
        } catch (e: Exception) {
            LogUtils.e("playByUri error: ${e.message}")
            releasePlayer()
        }
    }

    // ===================== 播放内置文件 =====================
    fun playByRawId(rawId: Int) {
        if (!requestFocus()) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer?.reset()
            val afd = context.resources.openRawResourceFd(rawId)
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
                state = State.PLAYING
                isPlaying = true
            }
            mediaPlayer?.setOnCompletionListener {
                state = State.STOPPED
                isPlaying = false
            }
        } catch (e: Exception) {
            LogUtils.e("playByRawId error: ${e.message}")
            releasePlayer()
        }
    }

    fun play(source: String) {
        if (source.startsWith("content://")) {
            playByUri(Uri.parse(source))
        } else {
            playByUrl(source)
        }
    }

    // ===================== 暂停 =====================
    fun pause() {
        try {
            mediaPlayer?.let {
                if (state == State.PLAYING) {
                    it.pause()
                    state = State.PAUSED
                    stateListener?.onPause()
                    isPlaying = false
                    userPaused = true   // 用户主动暂停
                }
            }
        } catch (e: Exception) {
            LogUtils.e("pause error: ${e.message}")
        }
    }

    // ===================== 恢复 =====================
    fun resume() {
        try {
            mediaPlayer?.let {
                if (state == State.PAUSED) {
                    if (!requestFocus()) return
                    it.start()
                    state = State.PLAYING
                    isPlaying = true
                    userPaused = false // 用户恢复
                }
            }
        } catch (e: IllegalStateException) {
            LogUtils.e("resume failed: ${e.message}")
        }
    }

    // ===================== 停止并释放 =====================
    fun close() {
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        state = State.IDLE
        stateListener?.onStop()
        isPlaying = false
    }

    // ===================== 音频焦点监听 =====================
    private inner class SimpleAudioFocusChangeListener :
        AudioManager.OnAudioFocusChangeListener {

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    pause()
                    userPaused = false // 系统暂停，不算用户行为
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // 只恢复“系统打断前正在播放”的状态
                    if (!userPaused && state == State.PAUSED) {
                        resume()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    pause()
                    userPaused = false
                }
            }
        }
    }
}