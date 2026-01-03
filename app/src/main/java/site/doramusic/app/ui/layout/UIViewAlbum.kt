package site.doramusic.app.ui.layout

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
import dora.skin.SkinManager
import dora.widget.DoraLetterView
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Album
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.AlbumItemAdapter
import java.util.*

class UIViewAlbum(drawer: IPlayerLyricDrawer, manager: UIManager) : UIFactory(drawer, manager) {

    private lateinit var statusBarAlbum: View
    private lateinit var titlebar: DoraTitleBar
    private lateinit var rvAlbum: RecyclerView
    private lateinit var adapter: AlbumItemAdapter
    private lateinit var lvAlbum: DoraLetterView
    private lateinit var tvAlbumDialog: TextView
    private val albumDao = DaoFactory.getDao(Album::class.java)

    override fun getView(from: Int, table: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_album, null)
        initViews(view)
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        statusBarAlbum = view.findViewById(R.id.statusbar_album)
        statusBarAlbum.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(statusBarAlbum, COLOR_THEME)
        titlebar = view.findViewById(R.id.titlebar_album)
        titlebar.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        rvAlbum = view.findViewById(R.id.rv_album)
        lvAlbum = view.findViewById(R.id.lv_album)
        tvAlbumDialog = view.findViewById(R.id.tv_album_dialog)
        rvAlbum.layoutManager = LinearLayoutManager(view.context)
        rvAlbum.addItemDecoration(DividerItemDecoration(view.context, RecyclerView.VERTICAL))
        val albums = albumDao.selectAll() as ArrayList<Album>
        adapter = AlbumItemAdapter(albums)
        adapter.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_ALBUM_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rvAlbum.adapter = adapter
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        lvAlbum.hoverTextColor = skinThemeColor
        lvAlbum.setOnLetterChangeListener(object : DoraLetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {

                tvAlbumDialog.text = letter
                val position: Int
                when (letter) {
                    "↑" -> {
                        position = 0
                    }
                    "#" -> {
                        position = adapter.itemCount - 1
                    }
                    else -> {
                        position = adapter.getPositionForSection(letter[0])
                    }
                }
                rvAlbum.scrollToPosition(position)
            }

        })
        lvAlbum.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tvAlbumDialog.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tvAlbumDialog.visibility = View.VISIBLE
            }
            false
        }
    }
}
