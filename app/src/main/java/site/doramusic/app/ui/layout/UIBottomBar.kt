package site.doramusic.app.ui.layout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dora.db.builder.QueryBuilder
import dora.db.dao.DaoFactory
import dora.firebase.SpmUtils
import dora.skin.SkinManager
import dora.util.LogUtils
import dora.util.RxBus
import dora.util.ScreenUtils
import dora.util.TextUtils
import dora.widget.DoraMarqueeTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Music
import site.doramusic.app.event.PlaylistChangedEvent
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.PlayModeControl
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.PlaylistItemAdapter
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.PrefsManager
import site.doramusic.app.util.ThemeSelector
import java.util.Locale

/**
 * 底部控制条。
 */
class UIBottomBar(drawer: IPlayerLyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
    View.OnClickListener, AppConfig {

    /** 外部要用，不能 private。 */
    var handler: Handler
    private val contentView: View = manager.view
    private lateinit var tvHomeBottomMusicName: DoraMarqueeTextView
    private lateinit var tvHomeBottomArtist: DoraMarqueeTextView
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
    // 不再使用全局adapter，弹窗每次新建
    @Volatile
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var disposable: CompositeDisposable? = null

    private fun addDisposable(d: Disposable) {
        if (disposable == null) {
            disposable = CompositeDisposable()
        }
        disposable!!.add(d)
    }

    private fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    init {
        initViews()
        handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                0x100 -> {
                    try {
                        refreshSeekProgress(
                            MediaManager.position(),
                            MediaManager.duration(), MediaManager.pendingProgress())
                    } catch (ignore: IllegalStateException) {
                    }
                }
            }
            false
        }
        addDisposable(
            RxBus.getInstance()
                .toObservable(PlaylistChangedEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    refreshBottomSheetPlaylist()
                    if (MediaManager.playlist.isEmpty() || MediaManager.curMusic == null) {
                        resetToDefault()
                    }
                })
    }

    fun setSecondaryProgress(progress: Int) {
        playbackProgress.secondaryProgress = progress
    }

    fun initData() {
        val music = DaoFactory.getDao(Music::class.java).selectOne(
            QueryBuilder.create()
                .orderBy("-${Music.COLUMN_LAST_PLAY_TIME}"))
        val spManager = PrefsManager(manager.view.context)
        val coldLaunchAutoPlay = spManager.isColdLaunchAutoPlay()
        if (music != null && coldLaunchAutoPlay) {
            MediaManager.refreshPlaylist(arrayListOf(music))
            tvHomeBottomMusicName.text = music.musicName
            tvHomeBottomArtist.text = music.artist
            try {
                val bitmap = MusicUtils.getCachedArtwork(contentView.context, music.albumId.toLong(),
                    defaultAlbumIcon)
                ivHomeBottomAlbum.background = bitmap.toDrawable(
                    contentView.context
                        .resources
                )
            } catch (e: UnsupportedOperationException) {
                ivHomeBottomAlbum.background = defaultAlbumIcon.toDrawable(
                    contentView.context
                        .resources
                )
                LogUtils.e(e.toString())
                // java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
            refreshUI(0, music.duration, music)
            MediaManager.playById(music.songId)
        }
    }

    private fun findViewById(id: Int): View {
        return contentView.findViewById(id)
    }

    fun updateProgressColor() {
        playbackProgress.progressTintList =
            ColorStateList.valueOf(ThemeSelector.getThemeColor(contentView.context))
    }

    private fun initViews() {
        tvHomeBottomMusicName = findViewById(R.id.tv_home_bottom_music_name) as DoraMarqueeTextView
        tvHomeBottomArtist = findViewById(R.id.tv_home_bottom_artist) as DoraMarqueeTextView
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
                    ivHomeBottomAlbum.setBackgroundDrawable(
                        bitmap.toDrawable(
                            manager.view.context
                                .resources
                        ))
                }
                MediaManager.updateNotification(bitmap, music.musicName, music.artist)
            } catch (e: UnsupportedOperationException) {
                MediaManager.updateNotification(defaultAlbumIcon, music.musicName, music.artist)
                LogUtils.e(e.toString())
                // java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
            }
        } else {
            defaultAlbumIcon.let {
                MediaManager.updateNotification(it, music.musicName, music.artist)
            }
        }
        refreshSeekProgress(curTime, tempTotalTime, MediaManager.pendingProgress())
    }

    /**
     * 删除当前歌曲后，底部控制条恢复默认（无播放）状态。
     */
    private fun resetToDefault() {
        tvHomeBottomMusicName.text = contentView.context.getString(R.string.brand)
        tvHomeBottomArtist.text = contentView.context.getString(R.string.slogan)
        tvHomeBottomPosition.text = contentView.context.getString(R.string._00_00)
        tvHomeBottomDuration.text = contentView.context.getString(R.string._00_00)
        playbackProgress.progress = 0
        playbackProgress.secondaryProgress = 0
        ivHomeBottomAlbum.background = defaultAlbumIcon.toDrawable(
            contentView.context.resources
        )
        showPlay(true)
        try {
            MediaManager.cancelNotification()
        } catch (ignored: Exception) {
        }
    }

    fun showPlay(flag: Boolean) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        if (flag) {
            TrackAnalysis.report(scope, EventType.EVENT_TYPE_PLAY_MUSIC)
            // 核心功能不再纳入统计
            // SpmUtils.selectContent(manager.view.context, "暂停音乐")
            btnHomeBottomPlay.visibility = View.VISIBLE
            btnHomeBottomPause.visibility = View.GONE
        } else {
            TrackAnalysis.report(scope, EventType.EVENT_TYPE_PAUSE_MUSIC)
            // 核心功能不再纳入统计
            // SpmUtils.selectContent(manager.view.context, "播放音乐")
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

    /**
     * 刷新快捷播放列表头部的歌曲数量。
     */
    private fun updatePlaylistCount(tvPlaylistCount: TextView) {
        tvPlaylistCount.text =
            "(${String.format(
                ContextCompat.getString(manager.view.context, R.string.items),
                MediaManager.playlist.size
            )})"
    }

    private var sheetAdapter: PlaylistItemAdapter? = null
    private var sheetRv: RecyclerView? = null
    private var sheetCountTv: TextView? = null

    /**
     * 播放队列变化后，弹窗刷新
     */
    private fun refreshBottomSheetPlaylist() {
        sheetAdapter?.setList(ArrayList(MediaManager.playlist))
        sheetAdapter?.notifyDataSetChanged()
        sheetCountTv?.let { updatePlaylistCount(it) }
    }

    private fun showBottomSheetDialog(context: Context) {
        synchronized(this) {
            if (bottomSheetDialog?.isShowing == true) {
                return
            }
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            TrackAnalysis.report(scope, EventType.EVENT_TYPE_SHOW_QUICK_PLAYLIST)
            SpmUtils.selectContent(context, "打开快捷播放列表")
            bottomSheetDialog = BottomSheetDialog(context)
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_playlist, null)
            val height = ScreenUtils.getContentHeight() * 2 / 5
            contentView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            val llPlaylist: LinearLayout = contentView.findViewById(R.id.ll_playlist)
            val prefsManager = PrefsManager(contentView.context)
            if (prefsManager.getSkinType() == 0) {
                llPlaylist.setBackgroundColor(prefsManager.getSkinColor())
            } else {
                llPlaylist.setBackgroundColor(SkinManager.getLoader().getColor(COLOR_THEME))
            }
            val tvPlaylistPlayMode: TextView = contentView.findViewById(R.id.tv_playlist_playmode)
            val tvPlaylistCount: TextView = contentView.findViewById(R.id.tv_playlist_count)
            val ivPlaylistPlayMode: ImageView = contentView.findViewById(R.id.iv_playlist_playmode)
            val recyclerView: RecyclerView = contentView.findViewById(R.id.rv_playlist)

            sheetRv = recyclerView
            sheetCountTv = tvPlaylistCount
            val adapter = PlaylistItemAdapter()
            sheetAdapter = adapter

            val playModeText = playModeControl.printPlayMode(MediaManager.playMode)
            if (playModeText.isNotEmpty()) {
                tvPlaylistPlayMode.visibility = View.VISIBLE
                ivPlaylistPlayMode.visibility = View.VISIBLE
                tvPlaylistCount.visibility = View.VISIBLE
                tvPlaylistPlayMode.text = playModeText
                ivPlaylistPlayMode.setImageResource(
                    playModeControl.getPlayModeImage(MediaManager.playMode)
                )
                updatePlaylistCount(tvPlaylistCount)
            } else {
                tvPlaylistPlayMode.visibility = View.INVISIBLE
                ivPlaylistPlayMode.visibility = View.INVISIBLE
                tvPlaylistCount.visibility = View.INVISIBLE
            }
            // 重要！给到的列表就是排序好的，不需要排序。再次排序会把最近播放按时间顺序排序的打乱。
            adapter.setNeedSort(false)
            adapter.setList(MediaManager.playlist)
            adapter.setOnItemClickListener { _, _, position ->
                val targetMusic = MediaManager.playlist[position]
                MediaManager.playById(targetMusic.songId)
            }
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = adapter
            tvPlaylistPlayMode.setOnClickListener {
                playModeControl.changePlayMode(tvPlaylistPlayMode, ivPlaylistPlayMode)
            }
            ivPlaylistPlayMode.setOnClickListener {
                playModeControl.changePlayMode(tvPlaylistPlayMode, ivPlaylistPlayMode)
            }
            bottomSheetDialog?.behavior?.peekHeight = height
            bottomSheetDialog?.setContentView(contentView)
            bottomSheetDialog?.setOnDismissListener {
                synchronized(this) {
                    bottomSheetDialog = null
                    sheetAdapter = null
                    sheetRv = null
                    sheetCountTv = null
                }
            }
            bottomSheetDialog?.show()
        }
    }
}
