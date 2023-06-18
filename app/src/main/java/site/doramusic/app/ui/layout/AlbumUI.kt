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
import site.doramusic.app.db.Album
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.AlbumItemAdapter
import java.util.*

class AlbumUI(context: Context, manager: UIManager) : UIFactory(context, manager) {

    private var statusbar_album: View? = null
    private var backBtn: ImageButton? = null
    private var rv_album: RecyclerView? = null
    private var adapter: AlbumItemAdapter? = null
    private var lv_album: LetterView? = null
    private var tv_album_dialog: TextView? = null
    private val albumDao = DaoFactory.getDao(Album::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_album, null)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        statusbar_album = view.findViewById(R.id.statusbar_album)
        statusbar_album!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        backBtn = view.findViewById(R.id.backBtn)
        backBtn!!.setOnClickListener { manager.setCurrentItem() }
        rv_album = view.findViewById(R.id.rv_album)
        lv_album = view.findViewById(R.id.lv_album)
        tv_album_dialog = view.findViewById(R.id.tv_album_dialog)
        rv_album!!.layoutManager = LinearLayoutManager(context)
        rv_album!!.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        val albums = albumDao.selectAll() as ArrayList<Album>
        adapter = AlbumItemAdapter(albums)
        adapter!!.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_ALBUM_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rv_album!!.adapter = adapter
        lv_album!!.setOnLetterChangeListener { s ->
            tv_album_dialog!!.text = s
            val position: Int
            when (s) {
                "↑" -> {
                    position = 0
                }
                "#" -> {
                    position = adapter!!.itemCount - 1
                }
                else -> {
                    position = adapter!!.getPositionForSection(s[0])
                }
            }
            rv_album!!.scrollToPosition(position)
        }
        lv_album!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tv_album_dialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tv_album_dialog!!.visibility = View.VISIBLE
            }
            false
        }
    }
}
