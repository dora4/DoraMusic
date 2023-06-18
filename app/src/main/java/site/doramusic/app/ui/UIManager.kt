package site.doramusic.app.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lsxiao.apollo.core.Apollo
import dora.db.table.OrmTable
import site.doramusic.app.R
import site.doramusic.app.base.callback.OnBackListener
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.ui.layout.AlbumUI
import site.doramusic.app.ui.layout.ArtistUI
import site.doramusic.app.ui.layout.FolderUI
import site.doramusic.app.ui.layout.MusicUI
import java.util.*

class UIManager(private val context: Context, val view: View) : AppConfig, OnBackListener {

    private var ui: UIFactory? = null
    private var masterViewPager: ViewPager? = null
    private var slaveViewPager: ViewPager? = null
    private var masterViews: ArrayList<View>? = null
    private var slaveViews: ArrayList<View>? = null
    private val inflater: LayoutInflater? = LayoutInflater.from(context)
    val isLocal: Boolean
        get() = masterViewPager!!.currentItem == 0

    init {
        init()
    }

    fun setCurrentItem() {
        if (slaveViewPager!!.childCount > 0) {
            slaveViewPager!!.setCurrentItem(0, true)
        } else {
            masterViewPager!!.setCurrentItem(0, true)
            //返回首页要刷新界面
            Apollo.emit(ApolloEvent.REFRESH_LOCAL_NUMS)
        }
    }

    private fun findViewById(id: Int): View {
        return view.findViewById(id)
    }

    fun setContentType(type: Int) { // 此处可以根据传递过来的view和type分开来处理
        setContentType(type, null)
    }

    fun setContentType(type: Int, table: OrmTable?) {
        (context as IBack).registerBackListener(this)
        val transView = inflater?.inflate(
                R.layout.view_vp_trans, null)
        when (type) {
            AppConfig.ROUTE_START_FROM_LOCAL -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_LOCAL)
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_FAVORITE)
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_LATEST)
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                ui = FolderUI(context, this)
                val contentView = ui!!.getView()
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                ui = ArtistUI(context, this)
                val contentView = ui!!.getView()
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                ui = AlbumUI(context, this)
                val contentView = ui!!.getView()
                masterViewPager!!.visibility = View.VISIBLE
                masterViews!!.clear()
                masterViewPager!!.removeAllViews()
                masterViews!!.add(transView!!)
                masterViews!!.add(contentView)
                masterViewPager!!.adapter = ViewPagerAdapter(masterViews)
                masterViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_FOLDER_TO_LOCAL -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_FOLDER, table)
                slaveViewPager!!.visibility = View.VISIBLE
                slaveViews!!.clear()
                slaveViewPager!!.removeAllViews()
                slaveViews!!.add(transView!!)
                slaveViews!!.add(contentView)
                slaveViewPager!!.adapter = ViewPagerAdapter(slaveViews)
                slaveViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_ARTIST_TO_LOCAL -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_ARTIST, table)
                slaveViewPager!!.visibility = View.VISIBLE
                slaveViews!!.clear()
                slaveViewPager!!.removeAllViews()
                slaveViews!!.add(transView!!)
                slaveViews!!.add(contentView)
                slaveViewPager!!.adapter = ViewPagerAdapter(slaveViews)
                slaveViewPager!!.setCurrentItem(1, true)
            }
            AppConfig.ROUTE_ALBUM_TO_LOCAL -> {
                ui = MusicUI(context, this)
                val contentView = ui!!.getView(AppConfig.ROUTE_START_FROM_ALBUM, table)
                slaveViewPager!!.visibility = View.VISIBLE
                slaveViews!!.clear()
                slaveViewPager!!.removeAllViews()
                slaveViews!!.add(transView!!)
                slaveViews!!.add(contentView)
                slaveViewPager!!.adapter = ViewPagerAdapter(slaveViews)
                slaveViewPager!!.setCurrentItem(1, true)
            }
        }
    }

    override fun onBack() {
        if (slaveViewPager!!.isShown) {
            slaveViewPager!!.setCurrentItem(0, true)
        } else if (masterViewPager!!.isShown) {
            masterViewPager!!.setCurrentItem(0, true)
        }
    }

    private inner class ViewPagerAdapter(private val pageViews: List<View>?) : PagerAdapter() {
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(pageViews!![position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(pageViews!![position])
            return pageViews[position]
        }

        override fun getCount(): Int {
            return pageViews!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }
    }

    private fun init() {
        masterViewPager = findViewById(R.id.vp_home_master) as ViewPager
        slaveViewPager = findViewById(R.id.vp_home_slave) as ViewPager
        masterViews = ArrayList()
        slaveViews = ArrayList()
        masterViewPager!!.addOnPageChangeListener(OnPageChangeListenerMaster())
        slaveViewPager!!.addOnPageChangeListener(OnPageChangeListenerSlave())
    }

    private inner class OnPageChangeListenerMaster : ViewPager.OnPageChangeListener {
        var onPageScrolled = -1
        // 当滑动状态改变时调用
        override fun onPageScrollStateChanged(state: Int) {
            if (onPageScrolled == 0 && state == 0) {
                (context as MainActivity).unregisterBackListener(this@UIManager)
                masterViewPager!!.removeAllViews()
                masterViewPager!!.visibility = View.INVISIBLE
            }
        }

        // 当当前页面被滑动时调用
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            onPageScrolled = position
        }

        // 当新的页面被选中时调用
        override fun onPageSelected(position: Int) {
        }
    }

    private inner class OnPageChangeListenerSlave : ViewPager.OnPageChangeListener {
        var onPageScrolled = -1
        // 当滑动状态改变时调用
        override fun onPageScrollStateChanged(state: Int) {
            if (onPageScrolled == 0 && state == 0) {
                slaveViewPager!!.removeAllViews()
                slaveViewPager!!.visibility = View.INVISIBLE
            }
        }

        // 当当前页面被滑动时调用
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            onPageScrolled = position
        }

        // 当新的页面被选中时调用
        override fun onPageSelected(position: Int) {}
    }
}