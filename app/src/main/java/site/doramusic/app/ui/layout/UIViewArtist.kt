package site.doramusic.app.ui.layout

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
import site.doramusic.app.db.Artist
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.ArtistItemAdapter
import site.doramusic.app.widget.LetterView
import java.util.*

class UIViewArtist(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager) {

    private var rvArtist: RecyclerView? = null
    private var titlebar: DoraTitleBar? = null
    private var adapter: ArtistItemAdapter? = null
    private var statusBarArtist: View? = null
    private var lvArtist: LetterView? = null
    private var tvArtistDialog: TextView? = null
    private val artistDao = DaoFactory.getDao(Artist::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_artist, null)
        initViews(view)
        return view
    }


    private fun initViews(view: View) {
        statusBarArtist = view.findViewById(R.id.statusbar_artist)
        statusBarArtist!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(statusBarArtist!!, "skin_theme_color")
        rvArtist = view.findViewById(R.id.rv_artist)
        lvArtist = view.findViewById(R.id.lv_artist)
        tvArtistDialog = view.findViewById(R.id.tv_artist_dialog)
        rvArtist!!.layoutManager = LinearLayoutManager(view.context)
        rvArtist!!.addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
        titlebar = view.findViewById(R.id.titlebar_artist)
        titlebar!!.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        val artists = artistDao.selectAll() as ArrayList<Artist>
        adapter = ArtistItemAdapter(artists)
        adapter!!.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_ARTIST_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rvArtist!!.adapter = adapter
        lvArtist!!.setOnLetterChangeListener(object : LetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {
                tvArtistDialog!!.text = letter
                val position: Int
                if (letter == "↑") {
                    position = 0
                } else if (letter == "#") {
                    position = adapter!!.itemCount - 1
                } else {
                    position = adapter!!.getPositionForSection(letter[0])
                }
                rvArtist!!.scrollToPosition(position)
            }

        })
        lvArtist!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tvArtistDialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tvArtistDialog!!.visibility = View.VISIBLE
            }
            false
        }
    }
}
