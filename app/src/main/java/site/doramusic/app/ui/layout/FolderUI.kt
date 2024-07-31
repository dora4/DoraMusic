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
import site.doramusic.app.db.Folder
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.FolderItemAdapter
import site.doramusic.app.widget.LetterView
import java.util.*

class FolderUI(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager) {

    private var statusBarFolder: View? = null
    private var titlebar: DoraTitleBar? = null
    private var adapter: FolderItemAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var lvFolder: LetterView? = null
    private var tvFolderDialog: TextView? = null
    private val folderDao = DaoFactory.getDao(Folder::class.java)

    override fun getView(from: Int, obj: OrmTable?): View {
        val view = inflater.inflate(R.layout.view_ui_folder, null)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {

        statusBarFolder = view.findViewById(R.id.statusbar_folder)
        statusBarFolder!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(statusBarFolder!!, "skin_theme_color")
        lvFolder = view.findViewById(R.id.lv_folder)
        tvFolderDialog = view.findViewById(R.id.tv_folder_dialog)
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
        recyclerView = view.findViewById(R.id.rv_folder)
        recyclerView!!.layoutManager = LinearLayoutManager(manager.view.context)
        recyclerView!!.addItemDecoration(DividerItemDecoration(manager.view.context, RecyclerView.VERTICAL))
        recyclerView!!.adapter = adapter
        lvFolder!!.setOnLetterChangeListener(object : LetterView.OnLetterChangeListener {
            override fun onChanged(letter: String) {
                tvFolderDialog!!.text = letter
                val position: Int
                if (letter == "↑") {
                    position = 0
                } else if (letter == "#") {
                    position = adapter!!.itemCount - 1
                } else {
                    position = adapter!!.getPositionForSection(letter[0])
                }
                recyclerView!!.scrollToPosition(position)
            }

        })
        lvFolder!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> tvFolderDialog!!.visibility = View.GONE
                MotionEvent.ACTION_DOWN -> tvFolderDialog!!.visibility = View.VISIBLE
            }
            false
        }
    }
}
