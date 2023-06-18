package site.doramusic.app.ui.layout

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lwh.jackknife.widget.LetterView
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Artist
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.ArtistItemAdapter
import java.util.*

class ArtistUI(context: Context, manager: UIManager) : UIFactory(context, manager) {

    private var rv_artist: RecyclerView? = null
    private var backBtn: ImageButton? = null
    private var adapter: ArtistItemAdapter? = null
    private var statusbar_artist: View? = null
    private var lv_artist: LetterView? = null
    private var tv_artist_dialog: TextView? = null
    private val artistDao = DaoFactory.getDao(Artist::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_artist, null)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        statusbar_artist = view.findViewById(R.id.statusbar_artist)
        statusbar_artist!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())

        rv_artist = view.findViewById(R.id.rv_artist)
        lv_artist = view.findViewById(R.id.lv_artist)
        tv_artist_dialog = view.findViewById(R.id.tv_artist_dialog)
        rv_artist!!.layoutManager = LinearLayoutManager(context)
        rv_artist!!.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        backBtn = view.findViewById(R.id.backBtn)
        backBtn!!.setOnClickListener { manager.setCurrentItem() }
        val artists = artistDao.selectAll() as ArrayList<Artist>
        adapter = ArtistItemAdapter(artists)
        adapter!!.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_ARTIST_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rv_artist!!.adapter = adapter
        lv_artist!!.setOnLetterChangeListener { s ->
            tv_artist_dialog!!.text = s
            val position: Int
            if (s == "↑") {
                position = 0
            } else if (s == "#") {
                position = adapter!!.itemCount - 1
            } else {
                position = adapter!!.getPositionForSection(s[0])
            }
            rv_artist!!.scrollToPosition(position)
        }
        lv_artist!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tv_artist_dialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tv_artist_dialog!!.visibility = View.VISIBLE
            }
            false
        }
    }
}
