package site.doramusic.app.ui.layout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import dora.db.async.OrmTask
import dora.db.async.OrmTaskListener
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable
import dora.firebase.SpmUtils
import dora.util.RxBus
import dora.util.ToastUtils
import dora.widget.DoraBottomDialog
import dora.widget.DoraLetterView
import dora.widget.DoraLoadingDialog
import dora.widget.DoraTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.MUSIC_LIST_MAX_LIST
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_ALBUM
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_ARTIST
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_FAVORITE
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_FOLDER
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_LATEST
import site.doramusic.app.conf.AppConfig.Companion.ROUTE_START_FROM_LOCAL
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.event.PlayMusicEvent
import site.doramusic.app.event.PlaylistChangedEvent
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MusicScanner
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.MusicItemAdapter
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.ThemeSelector

class UIViewMusic(drawer: IPlayerLyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
    AppConfig {

    private var from: Int = 0
    private var table: OrmTable? = null
    private lateinit var statusBarMusic: View
    private lateinit var titleBar: DoraTitleBar
    private lateinit var rvMusic: RecyclerView
    private lateinit var adapter: MusicItemAdapter
    private lateinit var lvMusic: DoraLetterView
    private lateinit var tvMusicDialog: TextView
    private val musicDao = DaoFactory.getDao(Music::class.java)
    private val loadingDialog: DoraLoadingDialog by lazy { DoraLoadingDialog(manager.view.context) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var disposable: CompositeDisposable? = null

    private fun addDisposable(d: Disposable) {
        if (disposable == null) {
            disposable = CompositeDisposable()
        }
        disposable!!.add(d)
    }

    private fun dispose() {
        disposable?.dispose()
    }

    init {
        addDisposable(
            RxBus.getInstance()
                .toObservable(PlayMusicEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.notifyDataSetChanged()
                })
    }

    private fun updateMusicListUI(musics: MutableList<Music>, sort: Boolean = true) {
        adapter = MusicItemAdapter().apply {
            setNeedSort(sort)
            setList(musics)
            setOnItemClickListener { _, _, position ->
                if (position >= adapter.data.size) {
                    return@setOnItemClickListener
                }
                val music = adapter.getItem(position)
                if (music.songId == -1) {
                    return@setOnItemClickListener
                }
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_CLICK_MUSIC_ITEM,
                    mapOf("source" to when(from) {
                        ROUTE_START_FROM_ARTIST -> "artist"
                        ROUTE_START_FROM_ALBUM -> "album"
                        ROUTE_START_FROM_FOLDER -> "folder"
                        ROUTE_START_FROM_FAVORITE -> "star"
                        ROUTE_START_FROM_LATEST -> "recent"
                        // ROUTE_START_FROM_LOCAL
                        else -> "local"
                    })
                )
                if (MediaManager.playlist != data) {
                    MediaManager.refreshPlaylist(data)
                }
                MediaManager.playById(music.songId)
            }
            setOnItemLongClickListener { adapter, view, position ->
                if (position >= MUSIC_LIST_MAX_LIST) {
                    true
                }
                DoraBottomDialog().show(manager.view.context as Activity, R.layout.dialog_music_info) {
                    val music = adapter.getItem(position) as Music
                    val tvMusicNameArtist = it.findViewById<TextView>(R.id.tv_music_name_artist)
                    val tvMusicDuration = it.findViewById<TextView>(R.id.tv_music_duration)
                    val tvMusicFilePath = it.findViewById<TextView>(R.id.tv_music_file_path)
                    val tvCopy = it.findViewById<TextView>(R.id.tv_copy)
                    val tvDelete = it.findViewById<TextView>(R.id.tv_delete)
                    val musicNameArtist = "${music.musicName} - ${music.artist}"
                    tvMusicNameArtist.text = musicNameArtist
                    tvMusicDuration.text = context.getString(
                        R.string.duration_colon,
                        MusicUtils.formatTime(music.duration.toLong())
                    )
                    tvMusicFilePath.text = context.getString(R.string.path_colon, music.folder)
                    tvCopy.setOnClickListener {
                        dismiss()
                        copyText(musicNameArtist, it.context)
                    }
                    tvDelete.setOnClickListener {
                        dismiss()
                        deleteMusicAt(position)
                    }
                }
                false
            }
        }
        rvMusic.adapter = adapter
        if (!sort) lvMusic.visibility = View.GONE
    }

    /**
     * 删除指定位置的歌曲：
     * 1. 若正在播放的是这首歌，先停播并取消通知。
     * 2. 后台删除数据库记录及关联信息。
     * 3. 成功后从当前列表移除并刷新。
     */
    private fun deleteMusicAt(position: Int) {
        if (position < 0 || position >= adapter.data.size) return
        val activity = manager.view.context as? Activity ?: return
        val music = adapter.getItem(position)
        scope.launch {
            val ok = try {
                // 从播放队列移除（当前播放会停，非当前不停）
                MediaManager.removeFromPlaylist(music)
                RxBus.getInstance().post(PlaylistChangedEvent())
                // 删数据库
                MusicScanner.deleteMusic(activity, music)
            } catch (e: Exception) {
                false
            }
            withContext(Dispatchers.Main) {
                if (ok) {
                    adapter.remove(position)
                    adapter.notifyItemRemoved(position)
                    RxBus.getInstance().post(RefreshHomeItemEvent())
                    ToastUtils.showShort(activity.getString(R.string.removed))
                } else {
                    ToastUtils.showShort(activity.getString(R.string.remove_failed))
                }
            }
        }
    }

    private fun copyText(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("music_info", text)
        clipboard.setPrimaryClip(clip)
        ToastUtils.showShort(context.getString(R.string.copied))
    }

    private fun createMusicTaskListener(activity: Activity, updateUI: (MutableList<Music>) -> Unit): OrmTaskListener<Music> {
        return object : OrmTaskListener<Music> {
            override fun onCompleted(task: OrmTask<Music>) {
                activity.runOnUiThread {
                    val musics = task.result(object : TypeToken<MutableList<Music>>() {}.rawType) as MutableList<Music>
                    updateUI(musics)
                    loadingDialog.dismiss()
                }
            }

            override fun onFailed(task: OrmTask<Music>, e: OrmTaskException) {
                activity.runOnUiThread {
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun initViews(view: View) {
        val activity = view.context as Activity
        statusBarMusic = view.findViewById<View>(R.id.statusbar_music).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight())
            ThemeSelector.applyViewTheme(this)
        }

        lvMusic = view.findViewById(R.id.lv_music)
        tvMusicDialog = view.findViewById(R.id.tv_music_dialog)
        titleBar = view.findViewById(R.id.titlebar_music)
        ThemeSelector.applyViewTheme(titleBar)
        rvMusic = view.findViewById(R.id.rv_music)

        setupTitleBar()
        setupRecyclerView(activity)
        setupLetterView()

        loadingDialog.show()
        loadMusicData(activity)
    }

    private fun setupTitleBar() {
        titleBar.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                // 预留菜单点击逻辑
            }
        })
    }

    private fun setupRecyclerView(activity: Activity) {
        rvMusic.layoutManager = LinearLayoutManager(activity)
        rvMusic.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        activity.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLetterView() {
        val skinThemeColor = ThemeSelector.getThemeColor(manager.view.context)
        lvMusic.hoverTextColor = skinThemeColor
        lvMusic.setOnLetterChangeListener(object : DoraLetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {
                tvMusicDialog.text = letter
                val position = when (letter) {
                    "↑" -> 0
                    "#" -> adapter.itemCount - 1
                    else -> adapter.getPositionForSection(letter[0])
                }
                (rvMusic.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            }
        })
        lvMusic.setOnTouchListener { _, event ->
            tvMusicDialog.visibility = if (event.action == MotionEvent.ACTION_UP) View.GONE else View.VISIBLE
            if (event.action == MotionEvent.ACTION_UP) {
                // 用户使用了字母导航
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_LETTER_VIEW,
                    mapOf("source" to when(from) {
                        ROUTE_START_FROM_ARTIST -> "artist"
                        ROUTE_START_FROM_ALBUM -> "album"
                        ROUTE_START_FROM_FOLDER -> "folder"
                        ROUTE_START_FROM_FAVORITE -> "star"
                        ROUTE_START_FROM_LATEST -> "recent" // 理论上是不会出现这个的
                        // ROUTE_START_FROM_LOCAL
                        else -> "local"
                    })
                )
            }
            false
        }
    }

    private fun loadMusicData(activity: Activity) {
        val listener = createMusicTaskListener(activity) { musics ->
            updateMusicListUI(musics)
        }
        when (from) {
            ROUTE_START_FROM_LOCAL -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "local"))
                SpmUtils.selectContent(activity, "从【我的歌曲】进入歌曲列表")
                musicDao.selectAllAsync(listener)
            }
            ROUTE_START_FROM_ARTIST -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "artist"))
                SpmUtils.selectContent(activity, "从【歌手】进入歌曲列表")
                val artist = table as Artist
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo(Music.COLUMN_ARTIST, artist.name), listener)
            }
            ROUTE_START_FROM_ALBUM -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "album"))
                SpmUtils.selectContent(activity, "从【专辑】进入歌曲列表")
                val album = table as Album
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo(Music.COLUMN_ALBUM_ID, album.album_id), listener)
            }
            ROUTE_START_FROM_FOLDER -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "folder"))
                SpmUtils.selectContent(activity, "从【文件夹】进入歌曲列表")
                val folder = table as Folder
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo(Music.COLUMN_FOLDER, folder.path), listener)
            }
            ROUTE_START_FROM_FAVORITE -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "favorite"))
                SpmUtils.selectContent(activity, "从【我的收藏】进入歌曲列表")
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo(Music.COLUMN_FAVORITE, Music.IS_FAVORITE), listener)
            }
            ROUTE_START_FROM_LATEST -> {
                TrackAnalysis.report(scope, EventType.EVENT_TYPE_MUSIC_LIST,
                    mapOf("source" to "recent"))
                SpmUtils.selectContent(activity, "从【最近播放】进入歌曲列表")
                musicDao.selectAsync(
                    QueryBuilder.create()
                        .where(WhereBuilder.create().addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0))
                        // 按最后播放时间降序
                        .orderBy("-${Music.COLUMN_LAST_PLAY_TIME}")
                        .limit(AppConfig.MAX_RECENT_MUSIC_NUM),
                    createMusicTaskListener(activity) { musics ->
                        updateMusicListUI(musics, false)
                    }
                )
            }
            else -> return
        }
    }

    override fun getView(from: Int, table: OrmTable?): View {
        val contentView = inflater.inflate(R.layout.view_ui_music, null)
        this.from = from
        this.table = table
        initViews(contentView)
        return contentView
    }
}
