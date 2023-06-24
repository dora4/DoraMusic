package site.doramusic.app.ui.layout

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import com.lwh.jackknife.widget.LetterView
import com.lwh.jackknife.xskin.SkinLoader
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
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
import site.doramusic.app.widget.LoadingDialog

class MusicUI(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager), AppConfig {

    private var from: Int = 0
    private var table: OrmTable? = null
    private var statusbar_music: View? = null
    private var titlebar: DoraTitleBar? = null
    private var defaultArtwork: Bitmap? = null
    private var rv_music: RecyclerView? = null
    private var adapter: MusicItemAdapter? = null
    private var lv_music: LetterView? = null
    private val mediaManager: MediaManager? = MusicApp.instance!!.mediaManager
    private var tv_music_dialog: TextView? = null
    private val musicDao = DaoFactory.getDao(Music::class.java)
    private val loadingDialog: LoadingDialog = LoadingDialog(manager.view.context)

    init {
        Apollo.bind(this)
    }

    @Receive(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
    fun refreshPlaylistStatus() {
        adapter?.notifyDataSetChanged()
    }

    private fun initViews(view: View) {
        statusbar_music = view.findViewById(R.id.statusbar_music)
        statusbar_music!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        statusbar_music!!.background = SkinLoader.getInstance().getDrawable("skin_theme_color")
        lv_music = view.findViewById(R.id.lv_music)
        tv_music_dialog = view.findViewById(R.id.tv_music_dialog)
        titlebar = view.findViewById(R.id.titlebar_music)
        titlebar!!.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        defaultArtwork = BitmapFactory.decodeResource(view.resources,
                R.mipmap.ic_launcher)

        rv_music = view.findViewById(R.id.rv_music)

        (view.context as MainActivity).volumeControlStream = AudioManager.STREAM_MUSIC

        rv_music!!.layoutManager = LinearLayoutManager(view.context)
        rv_music!!.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        adapter = MusicItemAdapter()
        when (from) {
            AppConfig.ROUTE_START_FROM_LOCAL -> {
                loadingDialog.show()
                Thread(Runnable {
                    val playlist = musicDao.selectAll()
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter!!.setList(playlist)
                        adapter!!.sort()
                        installItemClick()
                        rv_music!!.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }).start()
            }
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                loadingDialog.show()
                Thread(Runnable {
                    val artist = table as Artist?
                    val artists = musicDao.select(QueryBuilder.create().where(
                            WhereBuilder.create().addWhereEqualTo("artist", artist!!.name)))
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter!!.setList(artists)
                        adapter!!.sort()
                        installItemClick()
                        rv_music!!.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }).start()
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                loadingDialog.show()
                Thread(Runnable {
                    val album = table as Album?
                    val albums = musicDao.select(QueryBuilder.create().where(
                            WhereBuilder.create().addWhereEqualTo("album_id", album!!.album_id)))
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter!!.setList(albums)
                        adapter!!.sort()
                        installItemClick()
                        rv_music!!.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }).start()
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                loadingDialog.show()
                Thread(Runnable {
                    val folder = table as Folder?
                    val music = musicDao.select(QueryBuilder.create().where(WhereBuilder.create().addWhereEqualTo("folder", folder!!.path)))
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter!!.setList(music)
                        adapter!!.sort()
                        installItemClick()
                        rv_music!!.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }).start()
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                loadingDialog.show()
                Thread(Runnable {
                    val favorite = musicDao.select(QueryBuilder.create().where(WhereBuilder.create().addWhereEqualTo("favorite", 1)))
                    (view.context as Activity).runOnUiThread {
                        adapter = MusicItemAdapter()
                        adapter!!.setList(favorite)
                        adapter!!.sort()
                        installItemClick()
                        rv_music!!.adapter = adapter
                        loadingDialog.dismiss()
                    }
                }).start()
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                adapter!!.setList(musicDao.select(QueryBuilder.create()
                        .where(WhereBuilder.create().addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0))
                        .orderBy(Music.COLUMN_LAST_PLAY_TIME + " desc")))
                lv_music!!.visibility = View.GONE
            }
        }
        installItemClick()
        rv_music!!.adapter = adapter
        lv_music!!.setOnLetterChangeListener { s ->
            tv_music_dialog!!.text = s
            val position: Int
            if (s == "↑") {
                position = 0
            } else if (s == "#") {
                position = adapter!!.itemCount - 1
            } else {
                position = adapter!!.getPositionForSection(s[0])
            }
            val linearLayoutManager: LinearLayoutManager = rv_music!!.layoutManager as LinearLayoutManager
            linearLayoutManager.scrollToPositionWithOffset(position, 0)
        }
        lv_music!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tv_music_dialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tv_music_dialog!!.visibility = View.VISIBLE
            }
            false
        }
    }

    private fun installItemClick() {
        adapter!!.setOnItemClickListener { adapter, view, position ->
            val playlist = adapter.data as MutableList<Music>
            mediaManager!!.refreshPlaylist(playlist)
            mediaManager.playById(playlist[position].songId)
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
