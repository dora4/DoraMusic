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
import site.doramusic.app.widget.LetterView
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
import dora.skin.SkinManager
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Folder
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.FolderItemAdapter
import java.util.*

class FolderUI(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager) {

    private var statusbar_folder: View? = null
    private var titlebar: DoraTitleBar? = null
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
        SkinManager.getLoader().setBackgroundColor(statusbar_folder!!, "skin_theme_color")
        lv_folder = view.findViewById(R.id.lv_folder)
        tv_folder_dialog = view.findViewById(R.id.tv_folder_dialog)
        titlebar = view.findViewById(R.id.titlebar_folder)
        titlebar!!.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
                manager.setCurrentItem()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        val folders = folderDao.selectAll() as ArrayList<Folder>
        adapter = FolderItemAdapter(folders)
        adapter!!.setOnItemClickListener { adapter, view, position ->
            manager.setContentType(AppConfig.ROUTE_FOLDER_TO_LOCAL,
                adapter.getItem(position) as OrmTable?
            )
        }
        rv_folder = view.findViewById(R.id.rv_folder)
        rv_folder!!.layoutManager = LinearLayoutManager(manager.view.context)
        rv_folder!!.addItemDecoration(DividerItemDecoration(manager.view.context, RecyclerView.VERTICAL))
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
