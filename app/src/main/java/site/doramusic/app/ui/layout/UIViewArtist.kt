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
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Artist
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.ArtistItemAdapter
import site.doramusic.app.widget.LetterView
import java.util.*

class UIViewArtist(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager) {

    private lateinit var rvArtist: RecyclerView
    private lateinit var titlebar: DoraTitleBar
    private lateinit var adapter: ArtistItemAdapter
    private lateinit var statusBarArtist: View
    private lateinit var lvArtist: LetterView
    private lateinit var tvArtistDialog: TextView
    private val artistDao = DaoFactory.getDao(Artist::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_artist, null)
        initViews(view)
        return view
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        statusBarArtist = view.findViewById(R.id.statusbar_artist)
        statusBarArtist.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(statusBarArtist, COLOR_THEME)
        rvArtist = view.findViewById(R.id.rv_artist)
        lvArtist = view.findViewById(R.id.lv_artist)
        tvArtistDialog = view.findViewById(R.id.tv_artist_dialog)
        rvArtist.layoutManager = LinearLayoutManager(view.context)
        rvArtist.addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
        titlebar = view.findViewById(R.id.titlebar_artist)
        titlebar.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        val artists = artistDao.selectAll() as ArrayList<Artist>
        adapter = ArtistItemAdapter(artists)
        adapter.setOnItemClickListener { adapter, _, position ->
            manager.setContentType(AppConfig.ROUTE_ARTIST_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rvArtist.adapter = adapter
        lvArtist.setOnLetterChangeListener(object : LetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {
                tvArtistDialog.text = letter
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
                rvArtist.scrollToPosition(position)
            }

        })
        lvArtist.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tvArtistDialog.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tvArtistDialog.visibility = View.VISIBLE
            }
            false
        }
    }
}
