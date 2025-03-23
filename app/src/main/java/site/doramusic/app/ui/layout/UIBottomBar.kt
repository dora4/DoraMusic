package site.doramusic.app.ui.layout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import site.doramusic.app.util.MusicUtils
import dora.db.builder.QueryBuilder
import dora.db.dao.DaoFactory
import dora.firebase.SpmUtils
import dora.skin.SkinManager
import dora.util.LogUtils
import dora.util.ScreenUtils
import dora.util.TextUtils
import dora.util.ViewUtils
import dora.widget.ADialogWindow
import dora.widget.DoraDialog
import dora.widget.DoraDialogWindow
import site.doramusic.app.MusicApp
import site.doramusic.app.R
//import site.doramusic.app.annotation.SingleClick
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.PlayModeControl
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.PlaylistItemAdapter
import site.doramusic.app.util.PrefsManager
import site.doramusic.app.widget.MarqueeTextView
import java.util.Locale

/**
 * 底部控制条。
 */
class UIBottomBar(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
        View.OnClickListener, AppConfig {

    var handler: Handler
    private val contentView: View = manager.view
    private lateinit var tvHomeBottomMusicName: MarqueeTextView
    private lateinit var tvHomeBottomArtist: MarqueeTextView
    private lateinit var tvHomeBottomPosition: TextView
    private lateinit var tvHomeBottomDuration: TextView
    private lateinit var btnHomeBottomPlay: ImageButton
    private lateinit var btnHomeBottomPause: ImageButton
    private lateinit var btnHomeBottomNext: ImageButton
    private lateinit var btnHomeBottomMenu: ImageButton
    private lateinit var ivHomeBottomAlbum: ImageView
    private lateinit var playbackProgress: ProgressBar
    private lateinit var defaultAlbumIcon: Bitmap
    private val playModeControl: PlayModeControl by lazy { PlayModeControl(manager.view.context) }
    private val adapter = PlaylistItemAdapter()
    @Volatile
    private var bottomSheetDialog: BottomSheetDialog? = null

    init {
        Apollo.bind(this)
        initViews()
        handler = Handler { msg ->
            when (msg.what) {
                0x100 -> refreshSeekProgress(
                    MediaManager.position(),
                    MediaManager.duration(), MediaManager.pendingProgress()
                )
            }
            false
        }
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylist() {
        adapter.setList(MediaManager.playlist)
    }

    @Receive(ApolloEvent.REFRESH_PROGRESS_BAR)
    fun refreshProgressBar() {
        updateProgressColor()
    }

    fun setSecondaryProgress(progress: Int) {
        playbackProgress.secondaryProgress = progress
    }

    fun initData() {
        val music = DaoFactory.getDao(Music::class.java).selectOne(
            QueryBuilder.create()
                .orderBy(Music.COLUMN_LAST_PLAY_TIME + " desc"))
        val spManager = PrefsManager(manager.view.context)
        val coldLaunchAutoPlay = spManager.getColdLaunchAutoPlay()
        if (music != null && coldLaunchAutoPlay) {
//            val isOk = mediaManager.loadCurMusic(music)
//            if (isOk) {
            MediaManager.refreshPlaylist(arrayListOf(music))
            tvHomeBottomMusicName.text = music.musicName
            tvHomeBottomArtist.text = music.artist
            try {
                val bitmap = MusicUtils.getCachedArtwork(contentView.context, music.albumId.toLong(),
                    defaultAlbumIcon)
                ivHomeBottomAlbum.setBackgroundDrawable(BitmapDrawable(contentView.context
                    .resources, bitmap))
            } catch (e: UnsupportedOperationException) {

                ivHomeBottomAlbum.setBackgroundDrawable(BitmapDrawable(contentView.context
                    .resources, defaultAlbumIcon))
                LogUtils.e(e.toString())
//                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
            refreshUI(0, music.duration, music)
            // 这次播放来不及应用均衡器参数，主打一个启动时以最快速度进行播放
            MediaManager.playById(music.songId)
//            }
        }
    }

    private fun findViewById(id: Int): View {
        return contentView.findViewById(id)
    }

    private fun updateProgressColor() {
        playbackProgress.progressTintList = SkinManager.getLoader().getColorStateList(COLOR_THEME)
    }

    private fun initViews() {
        tvHomeBottomMusicName = findViewById(R.id.tv_home_bottom_music_name) as MarqueeTextView
        tvHomeBottomArtist = findViewById(R.id.tv_home_bottom_artist) as MarqueeTextView
        tvHomeBottomPosition = findViewById(R.id.tv_home_bottom_position) as TextView
        tvHomeBottomDuration = findViewById(R.id.tv_home_bottom_duration) as TextView
        btnHomeBottomPlay = findViewById(R.id.btn_home_bottom_play) as ImageButton
        btnHomeBottomPause = findViewById(R.id.btn_home_bottom_pause) as ImageButton
        btnHomeBottomNext = findViewById(R.id.btn_home_bottom_next) as ImageButton
        btnHomeBottomMenu = findViewById(R.id.btn_home_bottom_menu) as ImageButton

        btnHomeBottomPlay.setOnClickListener(this)
        btnHomeBottomPause.setOnClickListener(this)
        btnHomeBottomNext.setOnClickListener(this)
        btnHomeBottomMenu.setOnClickListener(this)

        playbackProgress = findViewById(R.id.sb_home_bottom_playback) as ProgressBar
        updateProgressColor()
        defaultAlbumIcon = BitmapFactory.decodeResource(
                manager.view.context.resources, R.drawable.bottom_bar_cover_bg)

        ivHomeBottomAlbum = findViewById(R.id.iv_home_bottom_album) as ImageView
        ivHomeBottomAlbum.setOnClickListener {
            drawer.showDrawer()
        }
    }

    private fun refreshSeekProgress(curTime: Int, totalTime: Int, pendingProgress: Int) {
        var curTime = curTime
        var totalTime = totalTime

        curTime /= 1000
        totalTime /= 1000
        val curMinute = curTime / 60
        val curSecond = curTime % 60

        val curTimeString = String.format("%02d:%02d", curMinute, curSecond)
        tvHomeBottomPosition.text = curTimeString

        var rate = 0
        if (totalTime != 0) {
            rate = (curTime.toFloat() / totalTime * 100).toInt()
        }
        playbackProgress.progress = rate
        playbackProgress.secondaryProgress = pendingProgress
    }

    fun refreshUI(curTime: Int, totalTime: Int, music: Music?) {
        if (music == null) return
        var tempTotalTime = totalTime

        tempTotalTime /= 1000
        val totalMinute = tempTotalTime / 60
        val totalSecond = tempTotalTime % 60
        val totalTimeString = String.format(
            Locale.getDefault(), "%02d:%02d", totalMinute,
                totalSecond)

        tvHomeBottomDuration.text = totalTimeString
        if (TextUtils.isNotEqualTo(tvHomeBottomMusicName.text.toString(), music.musicName)) {
            tvHomeBottomMusicName.text = music.musicName
        }

        if (TextUtils.isNotEqualTo(tvHomeBottomArtist.text.toString(), music.artist)) {
            tvHomeBottomArtist.text = music.artist
        }
        if (music.albumId != -1) {
            try {
                val bitmap = MusicUtils.getCachedArtwork(manager.view.context, music.albumId.toLong(),
                    defaultAlbumIcon)
                if (bitmap != null) {
                    ivHomeBottomAlbum.setBackgroundDrawable(BitmapDrawable(manager.view.context
                        .resources, bitmap))
                }
                MediaManager.updateNotification(bitmap, music.musicName, music.artist)
            } catch (e:UnsupportedOperationException) {
                MediaManager.updateNotification(defaultAlbumIcon, music.musicName, music.artist)
                LogUtils.e(e.toString())
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
        } else {
            defaultAlbumIcon?.let {
                MediaManager.updateNotification(it, music.musicName, music.artist)
            }
        }
        refreshSeekProgress(curTime, tempTotalTime, MediaManager.pendingProgress())
    }

    fun showPlay(flag: Boolean) {
        if (flag) {
            SpmUtils.selectContent(manager.view.context, "暂停音乐")
            btnHomeBottomPlay.visibility = View.VISIBLE
            btnHomeBottomPause.visibility = View.GONE
        } else {
            SpmUtils.selectContent(manager.view.context, "播放音乐")
            btnHomeBottomPlay.visibility = View.GONE
            btnHomeBottomPause.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_home_bottom_play -> MediaManager.replay()
            R.id.btn_home_bottom_pause -> MediaManager.pause()
            R.id.btn_home_bottom_next -> MediaManager.next()
            R.id.btn_home_bottom_menu -> showBottomSheetDialog(manager.view.context)
        }
    }

    private fun showBottomSheetDialog(context: Context) {
        // 如果弹窗已经显示，则不再重复打开
        synchronized(this) {
            // 确保方法在多线程环境下是线程安全的
            if (bottomSheetDialog?.isShowing == true) {
                return
            }
            bottomSheetDialog = BottomSheetDialog(context)
            val contentView = LayoutInflater.from(context).inflate(R.layout.view_popup_playlist, null)
            val height = ScreenUtils.getContentHeight() * 2 / 5
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            contentView.layoutParams = layoutParams
            val tvPlaylistPlayMode: TextView = contentView.findViewById(R.id.tv_playlist_playmode)
            val tvPlaylistCount: TextView = contentView.findViewById(R.id.tv_playlist_count)
            val ivPlaylistPlayMode: ImageView = contentView.findViewById(R.id.iv_playlist_playmode)
            val recyclerView: RecyclerView = contentView.findViewById(R.id.rv_playlist)
            val playModeText = playModeControl.printPlayMode(MediaManager.playMode)
            if (playModeText.isNotEmpty()) {
                tvPlaylistPlayMode.visibility = View.VISIBLE
                ivPlaylistPlayMode.visibility = View.VISIBLE
                tvPlaylistCount.visibility = View.VISIBLE
                tvPlaylistPlayMode.text = playModeText
                ivPlaylistPlayMode.setImageResource(playModeControl.getPlayModeImage(MediaManager.playMode))
                tvPlaylistCount.text =
                    "(${String.format(context.getString(R.string.items), MediaManager.playlist.size)})"
            } else {
                tvPlaylistPlayMode.visibility = View.INVISIBLE
                ivPlaylistPlayMode.visibility = View.INVISIBLE
                tvPlaylistCount.visibility = View.INVISIBLE
            }

            adapter.setList(MediaManager.playlist)
            adapter.setOnItemClickListener { _, _, position ->
                MediaManager.playById(MediaManager.playlist[position].songId)
            }

            ViewUtils.configRecyclerView(recyclerView)
            recyclerView.adapter = adapter

            tvPlaylistPlayMode.setOnClickListener {
                playModeControl.changePlayMode(tvPlaylistPlayMode, ivPlaylistPlayMode)
            }
            ivPlaylistPlayMode.setOnClickListener {
                playModeControl.changePlayMode(tvPlaylistPlayMode, ivPlaylistPlayMode)
            }

            bottomSheetDialog?.behavior?.peekHeight = height
            bottomSheetDialog?.setContentView(contentView)

            // 监听弹窗关闭，避免变量引用错误
            bottomSheetDialog?.setOnDismissListener {
                synchronized(this) {
                    bottomSheetDialog = null
                }
            }

            bottomSheetDialog?.show()
        }
    }
}
