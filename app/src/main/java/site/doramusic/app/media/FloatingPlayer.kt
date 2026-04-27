package site.doramusic.app.media

import android.content.Intent
import android.widget.ImageView
import dora.BaseFloatingWindowService
import dora.util.DensityUtils
import dora.util.ScreenUtils
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URL

class FloatingPlayer : BaseFloatingWindowService() {

    private lateinit var audioPlayer: SimpleAudioPlayer
    private var currentUrl: String? = null
    private var isInitialized = false
    private var ivPauseResume: ImageView? = null

    private fun updatePlayerIcon(isPlaying: Boolean) {
        ivPauseResume?.setImageResource(
            if (isPlaying) R.drawable.ic_player_pause
            else R.drawable.ic_player_play
        )
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

    override fun getLayoutId(): Int {
        return R.layout.layout_floating_player
    }

    override fun getInitialPosition(): IntArray {
        return intArrayOf(ScreenUtils.getScreenWidth() - DensityUtils.DP100,
            ScreenUtils.getScreenHeight() - DensityUtils.DP100)
    }

    override fun initViews() {
        ivPauseResume = findViewById(R.id.iv_floating_pause_resume)
        val ivStop = findViewById<ImageView>(R.id.iv_floating_stop)
        ivPauseResume?.setOnClickListener {
            if (audioPlayer.isPlaying()) {
                pause()
            } else {
                resume()
            }
        }
        ivStop.setOnClickListener {
            stopSelf()
        }
    }
}