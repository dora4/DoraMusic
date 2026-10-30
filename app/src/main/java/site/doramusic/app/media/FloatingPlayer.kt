package site.doramusic.app.media

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import dora.BaseFloatingWindowService
import dora.util.DensityUtils
import dora.util.RxBus
import dora.util.ScreenUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URI
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URL
import site.doramusic.app.event.RefreshDarkModeEvent
import site.doramusic.app.util.PrefsManager

class FloatingPlayer : BaseFloatingWindowService() {

    private lateinit var audioPlayer: SimpleAudioPlayer
    private var currentUrl: String? = null
    private var isInitialized = false
    private var ivPauseResume: ImageView? = null
    private var ivStop: ImageView? = null
    private val disposable = CompositeDisposable()

    private fun updatePlayerIcon(isPlaying: Boolean) {
        ivPauseResume?.let {
            val darkMode = PrefsManager(this).getDarkMode()
            applyDarkModeDrawable(darkMode,
                it, getDrawable(if (isPlaying) R.drawable.ic_player_pause else
                    R.drawable.ic_player_play),
                "#FFFFFF".toColorInt(), "#1A2025".toColorInt())
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioPlayer = SimpleAudioPlayer(this)
        audioPlayer.setOnPlayCompleteListener(object : SimpleAudioPlayer.OnPlayCompleteListener {
            override fun onComplete() {
                stopSelf()
            }
        })
        audioPlayer.setOnStateChangeListener(object : SimpleAudioPlayer.OnStateChangeListener {

            override fun onPlay() {
                updatePlayerIcon(true)
            }

            override fun onPause() {
                updatePlayerIcon(false)
            }

            override fun onStop() {
            }
        })
        isInitialized = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 优先URI
        val uri: Uri? = intent?.getParcelableExtra(EXTRA_URI)
        if (uri != null) {
            playByUri(uri)
            return START_STICKY
        }
        val url = intent?.getStringExtra(EXTRA_URL)
        if (!url.isNullOrEmpty()) {
            if (url != currentUrl) {
                currentUrl = url
                playByUrl(url)
            } else {
                resume()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        audioPlayer.close()
        ivPauseResume = null
        disposable.clear()
        super.onDestroy()
    }

    fun resume() {
        audioPlayer.resume()
        updatePlayerIcon(true)
    }

    fun pause() {
        audioPlayer.pause()
        updatePlayerIcon(false)
    }

    fun playByUrl(url: String) {
        audioPlayer.playByUrl(url)
    }

    fun playByUri(uri: Uri) {
        audioPlayer.playByUri(uri)
    }

    override fun getLayoutId(): Int {
        return R.layout.view_floating_player
    }

    override fun getInitialPosition(): IntArray {
        return intArrayOf(ScreenUtils.getScreenWidth() - DensityUtils.DP100,
            ScreenUtils.getScreenHeight() - DensityUtils.DP100)
    }

    override fun initViews() {
        ivPauseResume = findViewById(R.id.iv_floating_pause_resume)
        ivStop = findViewById<ImageView>(R.id.iv_floating_stop)
        ivPauseResume!!.setOnClickListener {
            if (audioPlayer.isPlaying()) {
                pause()
            } else {
                resume()
            }
        }
        ivStop!!.setOnClickListener {
            stopSelf()
        }
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

    private fun addDisposable(d: Disposable) {
        disposable.add(d)
    }

    private fun refreshDarkMode() {
        if (ivPauseResume == null || ivStop == null) return
        val darkMode = PrefsManager(this).getDarkMode()
        val llContainer = findViewById<LinearLayout>(R.id.ll_floating_player)
        val drawable = ContextCompat.getDrawable(this, R.drawable.shape_floating_border)
        applyDarkModeBackground(
            darkMode,
            llContainer,
            drawable,
            "#37393C".toColorInt(),
            "#E4E6F5".toColorInt()
        )
        val isPlaying = isInitialized && audioPlayer.isPlaying()
        applyDarkModeDrawable(
            darkMode,
            ivPauseResume!!,
            getDrawable(
                if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play
            ),
            "#FFFFFF".toColorInt(),
            "#1A2025".toColorInt()
        )
        applyDarkModeDrawable(darkMode,
            ivStop!!, ContextCompat.getDrawable(this,
                R.drawable.ic_player_stop),
            "#FFFFFF".toColorInt(), "#1A2025".toColorInt())
    }
}