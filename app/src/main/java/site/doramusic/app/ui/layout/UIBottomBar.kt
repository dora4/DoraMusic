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
    private val mediaManager: MediaManager? = MusicApp.instance!!.mediaManager
    private val contentView: View = manager.view
    private var tv_home_bottom_music_name: MarqueeTextView? = null
    private var tv_home_bottom_artist: MarqueeTextView? = null
    private var tv_home_bottom_position: TextView? = null
    private var tv_home_bottom_duration: TextView? = null
    private var btn_home_bottom_play: ImageButton? = null
    private var btn_home_bottom_pause: ImageButton? = null
    private var btn_home_bottom_next: ImageButton? = null
    private var btn_home_bottom_menu: ImageButton? = null
    private var iv_home_bottom_album: ImageView? = null
    private var playbackProgress: ProgressBar? = null
    private var defaultAlbumIcon: Bitmap? = null
    private val playModeControl: PlayModeControl = PlayModeControl(manager.view.context)
    private lateinit var popupDialog: DoraDialog
    private val adapter = PlaylistItemAdapter()

    init {
        Apollo.bind(this)
        initViews()
        handler = Handler { msg ->
            when (msg.what) {
                0x100 -> refreshSeekProgress(
                    mediaManager!!.position(),
                    mediaManager.duration(), mediaManager.pendingProgress()
                )
            }
            false
        }
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylistStatus() {
        adapter.setList(mediaManager!!.playlist)
    }

    fun setSecondaryProgress(progress: Int) {
        playbackProgress!!.secondaryProgress = progress
    }

    fun initData() {
        val musics = DaoFactory.getDao(Music::class.java).selectAll()
        val music = DaoFactory.getDao(Music::class.java).selectOne(
            QueryBuilder.create()
                .orderBy(Music.COLUMN_LAST_PLAY_TIME + " desc"))
        if (music != null) {
            mediaManager!!.refreshPlaylist(musics as MutableList<Music>)
            val isOk = mediaManager.loadCurMusic(music)
            if (isOk) {
                tv_home_bottom_music_name!!.text = music.musicName
                tv_home_bottom_artist!!.text = music.artist
                 try {
                     val bitmap = MusicUtils.getCachedArtwork(manager.view.context, music.albumId.toLong(),
                         defaultAlbumIcon)
                     iv_home_bottom_album!!.setBackgroundDrawable(BitmapDrawable(manager.view.context
                         .resources, bitmap))
                 } catch (e: UnsupportedOperationException) {
//                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                 }
                refreshUI(0, music.duration, music)
                val manager = PreferencesManager(manager.view.context)
                val coldLaunchAutoPlay = manager.getColdLaunchAutoPlay()
                if (coldLaunchAutoPlay) {
                    mediaManager.playById(music.songId)
                }
            }
        }
    }

    private fun findViewById(id: Int): View {
        return contentView.findViewById(id)
    }

    fun updateProgressColor() {
        playbackProgress!!.progressTintList = SkinManager.getLoader().getColorStateList("skin_theme_color")
    }

    private fun initViews() {
        tv_home_bottom_music_name = findViewById(R.id.tv_home_bottom_music_name) as MarqueeTextView
        tv_home_bottom_artist = findViewById(R.id.tv_home_bottom_artist) as MarqueeTextView
        tv_home_bottom_position = findViewById(R.id.tv_home_bottom_position) as TextView
        tv_home_bottom_duration = findViewById(R.id.tv_home_bottom_duration) as TextView
        btn_home_bottom_play = findViewById(R.id.btn_home_bottom_play) as ImageButton
        btn_home_bottom_pause = findViewById(R.id.btn_home_bottom_pause) as ImageButton
        btn_home_bottom_next = findViewById(R.id.btn_home_bottom_next) as ImageButton
        btn_home_bottom_menu = findViewById(R.id.btn_home_bottom_menu) as ImageButton

        btn_home_bottom_play!!.setOnClickListener(this)
        btn_home_bottom_pause!!.setOnClickListener(this)
        btn_home_bottom_next!!.setOnClickListener(this)
        btn_home_bottom_menu!!.setOnClickListener(this)

        playbackProgress = findViewById(R.id.sb_home_bottom_playback) as ProgressBar
        updateProgressColor()
        defaultAlbumIcon = BitmapFactory.decodeResource(
                manager.view.context.resources, R.drawable.bottom_bar_cover_bg)

        iv_home_bottom_album = findViewById(R.id.iv_home_bottom_album) as ImageView
        iv_home_bottom_album!!.setOnClickListener {
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
        tv_home_bottom_position!!.text = curTimeString

        var rate = 0
        if (totalTime != 0) {
            rate = (curTime.toFloat() / totalTime * 100).toInt()
        }
        playbackProgress!!.progress = rate
        playbackProgress!!.secondaryProgress = pendingProgress
    }

    fun refreshUI(curTime: Int, totalTime: Int, music: Music?) {
        if (music == null) return
        var totalTime = totalTime
        val tempTotalTime = totalTime

        totalTime /= 1000
        val totalMinute = totalTime / 60
        val totalSecond = totalTime % 60
        val totalTimeString = String.format("%02d:%02d", totalMinute,
                totalSecond)

        tv_home_bottom_duration!!.text = totalTimeString
        if (TextUtils.isNotEqualTo(tv_home_bottom_music_name!!.text.toString(), music.musicName)) {
            tv_home_bottom_music_name!!.text = music.musicName
        }

        if (TextUtils.isNotEqualTo(tv_home_bottom_artist!!.text.toString(), music.artist)) {
            tv_home_bottom_artist!!.text = music.artist
        }
        if (music.albumId != -1) {
            try {

                val bitmap = MusicUtils.getCachedArtwork(manager.view.context, music.albumId.toLong(),
                    defaultAlbumIcon)
                if (bitmap != null) {
                    iv_home_bottom_album!!.setBackgroundDrawable(BitmapDrawable(manager.view.context
                        .resources, bitmap))
                }
            } catch (e:UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
        } else {
            mediaManager!!.updateNotification(defaultAlbumIcon!!, music.musicName, music.artist)
        }
        refreshSeekProgress(curTime, tempTotalTime, mediaManager!!.pendingProgress())
    }

    fun showPlay(flag: Boolean) {
        if (flag) {
            SpmUtils.selectContent(manager.view.context, "暂停音乐")
            btn_home_bottom_play!!.visibility = View.VISIBLE
            btn_home_bottom_pause!!.visibility = View.GONE
        } else {
            SpmUtils.selectContent(manager.view.context, "播放音乐")
            btn_home_bottom_play!!.visibility = View.GONE
            btn_home_bottom_pause!!.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_home_bottom_play -> mediaManager!!.replay()
            R.id.btn_home_bottom_pause -> mediaManager!!.pause()
            R.id.btn_home_bottom_next -> mediaManager!!.next()
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
                val tv_playlist_playmode = contentView.findViewById(R.id.tv_playlist_playmode) as TextView
                val tv_playlist_count = contentView.findViewById(R.id.tv_playlist_count) as TextView
                val iv_playlist_playmode = contentView.findViewById(R.id.iv_playlist_playmode) as ImageView
                val recyclerView = contentView.findViewById(R.id.rv_playlist) as RecyclerView
                tv_playlist_playmode.text = playModeControl.printPlayMode(mediaManager!!.playMode)
                tv_playlist_count.text = "(${mediaManager.playlist!!.size}首)"
                adapter.setList(mediaManager.playlist)
                adapter.setOnItemClickListener { adapter, view, position ->
                    mediaManager.playById(mediaManager.playlist!![position].songId)
                }
                ViewUtils.configRecyclerView(recyclerView)
                recyclerView.adapter = adapter
                iv_playlist_playmode.setImageResource(playModeControl.getPlayModeImage(mediaManager.playMode))
                tv_playlist_playmode.setOnClickListener {
                    playModeControl.changePlayMode(tv_playlist_playmode,
                            iv_playlist_playmode)
                }
                iv_playlist_playmode.setOnClickListener {
                    playModeControl.changePlayMode(tv_playlist_playmode,
                            iv_playlist_playmode)
                }
            }
        })
        popupDialog = DoraDialog.Builder(manager.view.context)
            .create(dialogWindow)
        popupDialog.show()
    }
}
