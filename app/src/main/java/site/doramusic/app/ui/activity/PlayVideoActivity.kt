package site.doramusic.app.ui.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dora.BaseActivity
import dora.firebase.SpmUtils.spmTutorialBegin
import dora.firebase.SpmUtils.spmTutorialComplete
import dora.util.NavigationBarUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.databinding.ActivityPlayVideoBinding
import site.doramusic.app.util.ThemeSelector

class PlayVideoActivity : BaseActivity<ActivityPlayVideoBinding>() {

    private var player: ExoPlayer? = null
    private var videoTitle: String? = null
    private var videoUrl: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_play_video
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun onSetNavigationBar() {
        super.onSetNavigationBar()
        NavigationBarUtils.setNavigationBarColor(this, Color.BLACK)
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        videoTitle = intent.getStringExtra(AppConfig.EXTRA_TITLE)
        videoUrl = intent.getStringExtra(AppConfig.EXTRA_URL)
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: ActivityPlayVideoBinding
    ) {
        binding.titleBar.title = videoTitle ?: getString(R.string.app_name)
        binding.statusbarPlayVideo.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarPlayVideo)
        ThemeSelector.applyViewTheme(binding.titleBar)
        if (!videoUrl.isNullOrEmpty()) {
            player = ExoPlayer.Builder(this).build()
            binding.videoView.player = player
            player?.setMediaItem(
                MediaItem.fromUri(Uri.parse(videoUrl))
            )
            player?.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            spmTutorialBegin()
                        }

                        Player.STATE_ENDED -> {
                            spmTutorialComplete()
                        }
                    }
                }
            })
            player?.prepare()
            player?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放播放器
        player?.release()
        player = null
    }
}