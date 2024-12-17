package site.doramusic.app.ui.layout

import android.app.Activity
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
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import dora.db.async.OrmTask
import dora.db.async.OrmTaskListener
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable
import dora.skin.SkinManager
import dora.widget.DoraLoadingDialog
import dora.widget.DoraTitleBar
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_ALBUM
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_ARTIST
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_FAVORITE
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_FOLDER
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_LATEST
import site.doramusic.app.base.conf.AppConfig.Companion.ROUTE_START_FROM_LOCAL
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.MusicItemAdapter
import site.doramusic.app.widget.LetterView

class UIViewMusic(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
    AppConfig {

    private var from: Int = 0
    private var table: OrmTable? = null
    private lateinit var statusBarMusic: View
    private lateinit var titleBar: DoraTitleBar
    private lateinit var rvMusic: RecyclerView
    private lateinit var adapter: MusicItemAdapter
    private lateinit var lvMusic: LetterView
    private lateinit var tvMusicDialog: TextView
    private val mediaManager: MediaManager = MusicApp.app.mediaManager
    private val musicDao = DaoFactory.getDao(Music::class.java)
    private val loadingDialog: DoraLoadingDialog by lazy { DoraLoadingDialog(manager.view.context) }

    init {
        Apollo.bind(this)
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylistStatus() {
        adapter.notifyDataSetChanged()
    }

    private fun updateMusicListUI(musics: List<Music>, showSidebar: Boolean = true) {
        adapter = MusicItemAdapter().apply {
            setList(musics)
            setOnItemClickListener { baseQuickAdapter, _, position ->
                val playlist = baseQuickAdapter.data as MutableList<Music>
                mediaManager.refreshPlaylist(playlist)
                val music = playlist[position]
                if (music.songId != -1) {
                    mediaManager.playById(music.songId)
                }
            }
        }
        rvMusic.adapter = adapter
        if (!showSidebar) lvMusic.visibility = View.GONE
    }

    private fun createMusicTaskListener(activity: Activity, updateUI: (List<Music>) -> Unit): OrmTaskListener<Music> {
        return object : OrmTaskListener<Music> {
            override fun onCompleted(task: OrmTask<Music>) {
                activity.runOnUiThread {
                    val musics = task.result(object : TypeToken<List<Music>>() {}.rawType) as List<Music>
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
            SkinManager.getLoader().setBackgroundColor(this, "skin_theme_color")
        }

        lvMusic = view.findViewById(R.id.lv_music)
        tvMusicDialog = view.findViewById(R.id.tv_music_dialog)
        titleBar = view.findViewById(R.id.titlebar_music)
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

    private fun setupLetterView() {
        lvMusic.setOnLetterChangeListener(object : LetterView.OnLetterChangeListener {
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
            false
        }
    }

    private fun loadMusicData(activity: Activity) {
        val listener = createMusicTaskListener(activity) { musics ->
            updateMusicListUI(musics)
        }
        when (from) {
            ROUTE_START_FROM_LOCAL -> musicDao.selectAllAsync(listener)
            ROUTE_START_FROM_ARTIST -> {
                val artist = table as Artist
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo("artist", artist.name), listener)
            }
            ROUTE_START_FROM_ALBUM -> {
                val album = table as Album
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo("album_id", album.album_id), listener)
            }
            ROUTE_START_FROM_FOLDER -> {
                val folder = table as Folder
                musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo("folder", folder.path), listener)
            }
            ROUTE_START_FROM_FAVORITE -> musicDao.selectAsync(WhereBuilder.create().addWhereEqualTo("favorite", 1), listener)
            ROUTE_START_FROM_LATEST -> musicDao.selectAsync(
                QueryBuilder.create()
                    .where(WhereBuilder.create().addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0))
                    .orderBy("${Music.COLUMN_LAST_PLAY_TIME} desc"),
                createMusicTaskListener(activity) { musics ->
                    updateMusicListUI(musics, showSidebar = false)
                }
            )
            else -> return
        }
    }

    override fun getView(from: Int, obj: OrmTable?): View {
        val contentView = inflater.inflate(R.layout.view_ui_music, null)
        this.from = from
        table = obj
        initViews(contentView)
        return contentView
    }
}
