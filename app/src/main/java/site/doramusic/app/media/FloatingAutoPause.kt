package site.doramusic.app.media

import android.content.Intent
import android.os.CountDownTimer
import android.widget.TextView
import dora.BaseFloatingWindowService
import dora.util.DensityUtils
import dora.util.ScreenUtils
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig

/**
 * 自动暂停倒计时悬浮窗。
 *
 * 仅负责显示和调整倒计时时间。
 * 实际的自动暂停逻辑由外部播放器/定时器负责。
 */
class FloatingAutoPause : BaseFloatingWindowService() {

    companion object {
        const val ACTION_START =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_START"

        const val ACTION_UPDATE =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_UPDATE"

        const val ACTION_STOP =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_STOP"

        /**
         * 播放器开始播放时调用。
         */
        const val ACTION_PLAY =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_PLAY"

        const val EXTRA_DURATION =
            "site.doramusic.app.extra.AUTO_PAUSE_DURATION"

        private const val EVENT_TYPE_PAUSE_NOW = "pause_now"

        private const val MIN_DURATION = 15 * 60 * 1000L
        private const val MAX_DURATION = 2 * 60 * 60 * 1000L
        private const val STEP_DURATION = 15 * 60 * 1000L
    }

    private var tvTime: TextView? = null

    private var remainingTime = 0L

    private var countDownTimer: CountDownTimer? = null

    /**
     * 倒计时结束后，等待播放器重新开始播放。
     *
     * 主要用于切歌场景：
     *
     * 当前歌曲 -> 切歌 -> 新歌曲开始播放 -> 立即暂停。
     */
    private var waitingForResume = false

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(
                    EXTRA_DURATION,
                    0L
                )
                if (duration > 0L) {
                    waitingForResume = false
                    startCountdown(duration)
                }
            }
            ACTION_UPDATE -> {
                val duration = intent.getLongExtra(
                    EXTRA_DURATION,
                    0L
                )
                if (duration > 0L) {
                    waitingForResume = false
                    startCountdown(duration)
                }
            }
            ACTION_PLAY -> {
                handlePlay()
            }
            ACTION_STOP -> {
                waitingForResume = false
                stopCountdown()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_floating_auto_pause
    }

    override fun getInitialPosition(): IntArray {
        return intArrayOf(
            ScreenUtils.getScreenWidth() - DensityUtils.DP160,
            ScreenUtils.getScreenHeight() - DensityUtils.DP100
        )
    }

    override fun initViews() {
        tvTime = findViewById(
            R.id.tv_floating_auto_pause_time
        )
        val tvMinus = findViewById<TextView>(
            R.id.tv_floating_auto_pause_minus
        )
        val tvPlus = findViewById<TextView>(
            R.id.tv_floating_auto_pause_plus
        )
        tvMinus.setOnClickListener {
            decreaseTime()
        }
        tvPlus.setOnClickListener {
            increaseTime()
        }
        updateTime()
    }

    /**
     * 开始倒计时。
     */
    private fun startCountdown(duration: Long) {
        countDownTimer?.cancel()
        remainingTime = duration.coerceIn(
            MIN_DURATION,
            MAX_DURATION
        )
        updateTime()
        countDownTimer = object : CountDownTimer(
            remainingTime,
            1000L
        ) {

            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateTime()
            }

            override fun onFinish() {
                remainingTime = 0L
                updateTime()
                handleTimeout()
            }
        }.start()
    }

    /**
     * 倒计时结束。
     *
     * 如果当前正在播放，立即暂停。
     *
     * 如果当前没有播放，则等待播放器下一次开始播放。
     * 这样可以覆盖切歌过程中短暂停止播放的情况。
     */
    private fun handleTimeout() {
        stopCountdown()
        if (MediaManager.playState == AppConfig.MPS_PLAYING) {
            MediaManager.pause()
            stopSelf()
        } else {
            waitingForResume = true
        }
    }

    /**
     * 播放器开始播放时调用。
     *
     * 如果倒计时已经结束，并且正在等待切歌完成，
     * 则立即暂停新歌曲。
     */
    private fun handlePlay() {
        if (!waitingForResume) {
            return
        }
        waitingForResume = false
        MediaManager.pause()
        stopSelf()
    }

    /**
     * 增加 15 分钟。
     *
     * 最大不超过 2 小时。
     */
    private fun increaseTime() {
        waitingForResume = false
        val duration = (remainingTime + STEP_DURATION)
            .coerceAtMost(MAX_DURATION)
        startCountdown(duration)
    }

    /**
     * 减少 15 分钟。
     *
     * 小于等于 15 分钟时，直接暂停播放。
     */
    private fun decreaseTime() {
        if (remainingTime <= MIN_DURATION) {
            waitingForResume = false
            stopCountdown()
            remainingTime = 0L
            updateTime()
            MediaManager.pause()
            stopSelf()
            return
        }
        val duration = remainingTime - STEP_DURATION
        startCountdown(duration.coerceAtLeast(MIN_DURATION))
    }

    private fun updateTime() {
        tvTime?.text = formatTime(remainingTime)
    }

    private fun formatTime(time: Long): String {
        val totalSeconds =
            (time / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes =
            (totalSeconds % 3600) / 60
        val seconds =
            totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(
                hours,
                minutes,
                seconds
            )
        } else {
            "%02d:%02d".format(
                minutes,
                seconds
            )
        }
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onDestroy() {
        stopCountdown()
        waitingForResume = false
        tvTime = null
        super.onDestroy()
    }
}