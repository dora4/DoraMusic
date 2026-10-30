package site.doramusic.app.media

import android.content.Intent
import android.os.CountDownTimer
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import dora.BaseFloatingWindowService
import dora.util.DensityUtils
import dora.util.ScreenUtils
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.util.PrefsManager
import androidx.core.graphics.toColorInt
import dora.util.RxBus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import site.doramusic.app.event.RefreshDarkModeEvent
import kotlin.jvm.java

class FloatingAutoPause : BaseFloatingWindowService() {

    companion object {
        const val ACTION_START =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_START"

        const val ACTION_UPDATE =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_UPDATE"

        const val ACTION_STOP =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_STOP"

        const val ACTION_PLAY =
            "site.doramusic.app.action.FLOATING_AUTO_PAUSE_PLAY"

        const val EXTRA_DURATION =
            "site.doramusic.app.extra.AUTO_PAUSE_DURATION"
        private const val MAX_DURATION = 2 * 60 * 60 * 1000L
        private const val STEP_DURATION = 15 * 60 * 1000L
    }

    private var tvTime: TextView? = null

    private var remainingTime = 0L

    private var countDownTimer: CountDownTimer? = null

    private var waitingForResume = false

    private val disposable = CompositeDisposable()

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
        return R.layout.view_floating_auto_pause
    }

    override fun getInitialPosition(): IntArray {
        return intArrayOf(
            ScreenUtils.getScreenWidth() - DensityUtils.DP100,
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
        val tvCancel = findViewById<TextView>(
            R.id.tv_floating_auto_pause_cancel
        )
        tvMinus.setOnClickListener {
            decreaseTime()
        }
        tvPlus.setOnClickListener {
            increaseTime()
        }
        tvCancel.setOnClickListener {
            cancelAutoPause()
        }
        updateTime()
        // 初始深色模式样式
        refreshDarkMode()
        // 监听深色模式变化
        addDisposable(
            RxBus.getInstance()
                .toObservable(RefreshDarkModeEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    refreshDarkMode()
                }
        )
    }

    private fun refreshDarkMode() {
        val darkMode = PrefsManager(this).getDarkMode()
        val llContainer = findViewById<LinearLayout>(R.id.ll_floating_auto_pause)
        val drawable = ContextCompat.getDrawable(this, R.drawable.shape_floating_border)
        applyDarkModeBackground(
            darkMode,
            llContainer,
            drawable,
            "#37393C".toColorInt(),
            "#E4E6F5".toColorInt()
        )
        applyDarkModeTextColor(
            darkMode,
            findViewById<TextView>(R.id.tv_floating_auto_pause_time),
            "#FFFFFF".toColorInt(),
            getColor(R.color.colorTextPrimary)
        )
        applyDarkModeTextColors(
            darkMode,
            arrayOf(
                findViewById<TextView>(R.id.tv_floating_auto_pause_plus),
                findViewById<TextView>(R.id.tv_floating_auto_pause_minus)
            ),
            "#FFFFFF".toColorInt(),
            getColor(R.color.colorTextSecondary)
        )
    }

    private fun addDisposable(d: Disposable) {
        disposable.add(d)
    }

    private fun startCountdown(duration: Long) {
        countDownTimer?.cancel()
        remainingTime = duration.coerceIn(
            0L,
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

    private fun handleTimeout() {
        stopCountdown()
        if (MediaManager.playState == AppConfig.MPS_PLAYING) {
            MediaManager.pause()
        } else {
            waitingForResume = true
        }
        stopSelf()
    }

    private fun handlePlay() {
        if (!waitingForResume) {
            return
        }
        waitingForResume = false
        MediaManager.pause()
        stopSelf()
    }

    private fun increaseTime() {
        waitingForResume = false
        val duration = (remainingTime + STEP_DURATION)
            .coerceAtMost(MAX_DURATION)
        startCountdown(duration)
    }

    private fun decreaseTime() {
        val duration = remainingTime - STEP_DURATION
        if (duration <= 0L) {
            waitingForResume = false
            stopCountdown()
            remainingTime = 0L
            updateTime()
            MediaManager.pause()
            stopSelf()
            return
        }
        startCountdown(duration)
    }

    private fun cancelAutoPause() {
        waitingForResume = false
        stopCountdown()
        remainingTime = 0L
        updateTime()
        stopSelf()
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
        disposable.clear()
        super.onDestroy()
    }
}