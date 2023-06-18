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
import site.doramusic.app.db.Folder
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.FolderItemAdapter
import java.util.*

class FolderUI(context: Context, manager: UIManager) : UIFactory(context, manager) {

    private var statusbar_folder: View? = null
    private var backBtn: ImageButton? = null
    private var adapter: FolderItemAdapter? = null
    private var rv_folder: RecyclerView? = null

    private var lv_folder: LetterView? = null
    private var tv_folder_dialog: TextView? = null
    private val folderDao = DaoFactory.getDao(Folder::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_folder, null)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {

        statusbar_folder = view.findViewById(R.id.statusbar_folder)
        statusbar_folder!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())

        lv_folder = view.findViewById(R.id.lv_folder)
        tv_folder_dialog = view.findViewById(R.id.tv_folder_dialog)
        backBtn = view.findViewById(R.id.backBtn)
        backBtn!!.setOnClickListener { manager.setCurrentItem() }
        val folders = folderDao.selectAll() as ArrayList<Folder>
        adapter = FolderItemAdapter(folders)
        adapter!!.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_FOLDER_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rv_folder = view.findViewById(R.id.rv_folder)
        rv_folder!!.layoutManager = LinearLayoutManager(context)
        rv_folder!!.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        rv_folder!!.adapter = adapter
        lv_folder!!.setOnLetterChangeListener { s ->
            tv_folder_dialog!!.text = s
            val position: Int
            if (s == "↑") {
                position = 0
            } else if (s == "#") {
                position = adapter!!.itemCount - 1
            } else {
                position = adapter!!.getPositionForSection(s[0])
            }
            rv_folder!!.scrollToPosition(position)
        }
        lv_folder!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tv_folder_dialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tv_folder_dialog!!.visibility = View.VISIBLE
            }
            false
        }
    }
}
