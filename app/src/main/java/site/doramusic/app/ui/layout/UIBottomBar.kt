package site.doramusic.app.ui.layout

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import site.doramusic.app.util.MusicUtils
import dora.db.builder.QueryBuilder
import dora.db.dao.DaoFactory
import dora.firebase.SpmUtils
import dora.skin.SkinManager
import dora.util.LogUtils
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
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.PlayModeControl
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.PlaylistItemAdapter
import site.doramusic.app.util.PreferencesManager
import site.doramusic.app.widget.MarqueeTextView

/**
 * 底部控制条。
 */
class UIBottomBar(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
        View.OnClickListener, AppConfig {

    var handler: Handler
    private val mediaManager: MediaManager by lazy { MusicApp.app.mediaManager }
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
    private var defaultAlbumIcon: Bitmap? = null
    private val playModeControl: PlayModeControl by lazy { PlayModeControl(manager.view.context) }
    private lateinit var popupDialog: DoraDialog
    private val adapter = PlaylistItemAdapter()

    init {
        Apollo.bind(this)
        initViews()
        handler = Handler { msg ->
            when (msg.what) {
                0x100 -> refreshSeekProgress(
                    mediaManager.position(),
                    mediaManager.duration(), mediaManager.pendingProgress()
                )
            }
            false
        }
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylistStatus() {
        adapter.setList(mediaManager.playlist)
    }

    fun setSecondaryProgress(progress: Int) {
        playbackProgress.secondaryProgress = progress
    }

    fun initData() {
        val music = DaoFactory.getDao(Music::class.java).selectOne(
            QueryBuilder.create()
                .orderBy(Music.COLUMN_LAST_PLAY_TIME + " desc"))
        val spManager = PreferencesManager(manager.view.context)
        val coldLaunchAutoPlay = spManager.getColdLaunchAutoPlay()
        if (music != null && coldLaunchAutoPlay) {
//            val isOk = mediaManager.loadCurMusic(music)
//            if (isOk) {
            mediaManager.refreshPlaylist(arrayListOf(music))
            tvHomeBottomMusicName.text = music.musicName
            tvHomeBottomArtist.text = music.artist
            try {
                val bitmap = MusicUtils.getCachedArtwork(contentView.context, music.albumId.toLong(),
                    defaultAlbumIcon)
                ivHomeBottomAlbum.setBackgroundDrawable(BitmapDrawable(contentView.context
                    .resources, bitmap))
            } catch (e: UnsupportedOperationException) {
                LogUtils.e(e.toString())
//                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
            refreshUI(0, music.duration, music)
            mediaManager.playById(music.songId)
//            }
        }
    }

    private fun findViewById(id: Int): View {
        return contentView.findViewById(id)
    }

    fun updateProgressColor() {
        playbackProgress.progressTintList = SkinManager.getLoader().getColorStateList("skin_theme_color")
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
        val totalTimeString = String.format("%02d:%02d", totalMinute,
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
            } catch (e:UnsupportedOperationException) {
                LogUtils.e(e.toString())
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
        } else {
            defaultAlbumIcon?.let {
                mediaManager.updateNotification(it, music.musicName, music.artist)
            }
        }
        refreshSeekProgress(curTime, tempTotalTime, mediaManager.pendingProgress())
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
            R.id.btn_home_bottom_play -> mediaManager.replay()
            R.id.btn_home_bottom_pause -> mediaManager.pause()
            R.id.btn_home_bottom_next -> mediaManager.next()
            R.id.btn_home_bottom_menu -> showPlaylistDialog()
        }
    }

//    @SingleClick
    private fun showPlaylistDialog() {
        val dialogWindow = DoraDialogWindow(R.layout.view_popup_playlist,
                ADialogWindow.DEFAULT_SHADOW_COLOR)   //0x60000000
        dialogWindow.setCanTouchOutside(false)    //仅当设置了阴影背景有效，默认false，阴影处控件不可点
        dialogWindow.gravity = Gravity.BOTTOM
        dialogWindow.setOnInflateListener(object : DoraDialogWindow.OnInflateListener {

            @SuppressLint("SetTextI18n")
            override fun onInflateFinish(contentView: View) {
                val tvPlaylistPlayMode: TextView = contentView.findViewById(R.id.tv_playlist_playmode)
                val tvPlaylistCount: TextView = contentView.findViewById(R.id.tv_playlist_count)
                val ivPlaylistPlayMode: ImageView = contentView.findViewById(R.id.iv_playlist_playmode)
                val recyclerView: RecyclerView = contentView.findViewById(R.id.rv_playlist)
                tvPlaylistPlayMode.text = playModeControl.printPlayMode(mediaManager.playMode)
                "(${mediaManager.playlist.size}首)".also { tvPlaylistCount.text = it }
                adapter.setList(mediaManager.playlist)
                adapter.setOnItemClickListener { adapter, view, position ->
                    mediaManager.playById(mediaManager.playlist[position].songId)
                }
                ViewUtils.configRecyclerView(recyclerView)
                recyclerView.adapter = adapter
                ivPlaylistPlayMode.setImageResource(playModeControl.getPlayModeImage(mediaManager.playMode))
                tvPlaylistPlayMode.setOnClickListener {
                    playModeControl.changePlayMode(tvPlaylistPlayMode,
                            ivPlaylistPlayMode)
                }
                ivPlaylistPlayMode.setOnClickListener {
                    playModeControl.changePlayMode(tvPlaylistPlayMode,
                            ivPlaylistPlayMode)
                }
            }
        })
        popupDialog = DoraDialog.Builder(manager.view.context)
            .create(dialogWindow)
        popupDialog.show()
    }
}
