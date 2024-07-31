package site.doramusic.app.ui.layout

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
import dora.skin.SkinManager
import dora.widget.DoraLoadingDialog
import dora.widget.DoraTitleBar
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.ui.adapter.MusicItemAdapter
import site.doramusic.app.widget.LetterView

class UIViewMusic(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager), AppConfig {

    private var from: Int = 0
    private var table: OrmTable? = null
    private lateinit var statusBarMusic: View
    private lateinit var titleBar: DoraTitleBar
    private var defaultArtwork: Bitmap? = null
    private lateinit var rvMusic: RecyclerView
    private lateinit var adapter: MusicItemAdapter
    private lateinit var lvMusic: LetterView
    private val mediaManager: MediaManager? = MusicApp.instance!!.mediaManager
    private lateinit var tvMusicDialog: TextView
    private val musicDao = DaoFactory.getDao(Music::class.java)
    private val loadingDialog: DoraLoadingDialog = DoraLoadingDialog(manager.view.context)

    init {
        Apollo.bind(this)
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylistStatus() {
        adapter?.notifyDataSetChanged()
    }

    private fun initViews(view: View) {
        statusBarMusic = view.findViewById(R.id.statusbar_music)
        statusBarMusic.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(statusBarMusic!!, "skin_theme_color")
        lvMusic = view.findViewById(R.id.lv_music)
        tvMusicDialog = view.findViewById(R.id.tv_music_dialog)
        titleBar = view.findViewById(R.id.titlebar_music)
        titleBar.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        defaultArtwork = BitmapFactory.decodeResource(view.resources,
                R.mipmap.ic_launcher)

        rvMusic = view.findViewById(R.id.rv_music)

        (view.context as MainActivity).volumeControlStream = AudioManager.STREAM_MUSIC

        rvMusic.layoutManager = LinearLayoutManager(view.context)
        rvMusic.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        adapter = MusicItemAdapter()
        when (from) {
            AppConfig.ROUTE_START_FROM_LOCAL -> {
                loadingDialog.show()
                Thread {
                    val playlist = musicDao.selectAll()
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter.setList(playlist)
                        adapter.sort()
                        installItemClick()
                        rvMusic.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }.start()
            }
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                loadingDialog.show()
                Thread {
                    val artist = table as Artist?
                    val artists = musicDao.select(
                        QueryBuilder.create().where(
                            WhereBuilder.create().addWhereEqualTo("artist", artist!!.name)
                        )
                    )
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter.setList(artists)
                        adapter.sort()
                        installItemClick()
                        rvMusic.adapter = adapter
                        loadingDialog.dismissWithAnimation()
                    }
                }.start()
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                loadingDialog.show()
                Thread {
                    val album = table as Album?
                    val albums = musicDao.select(
                        QueryBuilder.create().where(
                            WhereBuilder.create().addWhereEqualTo("album_id", album!!.album_id)
                        )
                    )
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter.setList(albums)
                        adapter.sort()
                        installItemClick()
                        rvMusic.adapter = adapter
                        loadingDialog.dismissWithAnimation()
                    }
                }.start()
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                loadingDialog.show()
                Thread {
                    val folder = table as Folder?
                    val music = musicDao.select(
                        QueryBuilder.create().where(
                            WhereBuilder.create()
                                .addWhereEqualTo("folder", folder!!.path)
                        )
                    )
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter.setList(music)
                        adapter.sort()
                        installItemClick()
                        rvMusic.adapter = adapter
                        loadingDialog.dismissWithAnimation()
                    }
                }.start()
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                loadingDialog.show()
                Thread {
                    val favorite = musicDao.select(
                        QueryBuilder.create()
                            .where(WhereBuilder.create().addWhereEqualTo("favorite", 1))
                    )
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter.setList(favorite)
                        adapter.sort()
                        installItemClick()
                        rvMusic.adapter = adapter
                        loadingDialog.dismissWithAnimation()
                    }
                }.start()
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                adapter.setList(musicDao.select(QueryBuilder.create()
                        .where(WhereBuilder.create().addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0))
                        .orderBy(Music.COLUMN_LAST_PLAY_TIME + " desc")))
                lvMusic.visibility = View.GONE
            }
        }
        installItemClick()
        rvMusic.adapter = adapter
        lvMusic.setOnLetterChangeListener(object : LetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {
                tvMusicDialog.text = letter
                val position: Int = when (letter) {
                    "↑" -> {
                        0
                    }
                    "#" -> {
                        adapter.itemCount - 1
                    }
                    else -> {
                        adapter.getPositionForSection(letter[0])
                    }
                }
                val linearLayoutManager: LinearLayoutManager = rvMusic.layoutManager as LinearLayoutManager
                linearLayoutManager.scrollToPositionWithOffset(position, 0)
            }
        })
        lvMusic.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tvMusicDialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tvMusicDialog!!.visibility = View.VISIBLE
            }
            false
        }
    }

    private fun installItemClick() {
        adapter.setOnItemClickListener { adapter, view, position ->
            val playlist = adapter.data as MutableList<Music>
            mediaManager?.refreshPlaylist(playlist)
            mediaManager?.playById(playlist[position].songId)
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
