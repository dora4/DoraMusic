package site.doramusic.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import dora.db.table.OrmTable
import dora.util.RxBus
import site.doramusic.app.R
import site.doramusic.app.base.callback.OnBackListener
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.event.RefreshNumEvent
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.ui.layout.ILyricDrawer
import site.doramusic.app.ui.layout.UIViewAlbum
import site.doramusic.app.ui.layout.UIViewArtist
import site.doramusic.app.ui.layout.UIViewFolder
import site.doramusic.app.ui.layout.UIViewMusic

class UIManager(private var drawer: ILyricDrawer,
                // 外部要用，不能private
                val view: View) : AppConfig, OnBackListener {

    private lateinit var factory: UIFactory
    private lateinit var mainViewPager: ViewPager
    private lateinit var secondaryViewPager: ViewPager
    private var mainViews: ArrayList<View> = arrayListOf()
    private var secondaryViews: ArrayList<View> = arrayListOf()
    val inflater: LayoutInflater = LayoutInflater.from(view.context)
    val isLocal: Boolean
        get() = mainViewPager.currentItem == 0

    init {
        init()
    }

    companion object {
        const val PAGE_MAIN = 0
        const val PAGE_SECONDARY = 1
    }

    fun setCurrentItem() {
        if (secondaryViewPager.childCount > 0) {
            secondaryViewPager.setCurrentItem(0, true)
        } else {
            mainViewPager.setCurrentItem(0, true)
            // 返回首页要刷新界面
            RxBus.getInstance().post(RefreshNumEvent())
        }
    }

    private fun findViewById(id: Int): View {
        return view.findViewById(id)
    }

    fun setContentType(type: Int) { // 此处可以根据传递过来的view和type分开来处理
        setContentType(type, null)
    }

    fun setContentType(type: Int, table: OrmTable?) {
        (view.context as IBack).registerBackListener(this)
        val transView = inflater.inflate(
                R.layout.view_vp_trans, null) as LinearLayout
        when (type) {
            AppConfig.ROUTE_START_FROM_LOCAL -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_LOCAL)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_FAVORITE)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_LATEST)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                factory = UIViewFolder(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_FOLDER)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                factory = UIViewArtist(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_ARTIST)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                factory = UIViewAlbum(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_ALBUM)
                mainViewPager.visibility = View.VISIBLE
                mainViews.clear()
                mainViewPager.removeAllViews()
                mainViews.add(transView)
                mainViews.add(contentView)
                mainViewPager.adapter = ViewPagerAdapter(mainViews)
                mainViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_FOLDER_TO_LOCAL -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_FOLDER, table)
                secondaryViewPager.visibility = View.VISIBLE
                secondaryViews.clear()
                secondaryViewPager.removeAllViews()
                secondaryViews.add(transView)
                secondaryViews.add(contentView)
                secondaryViewPager.adapter = ViewPagerAdapter(secondaryViews)
                secondaryViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_ARTIST_TO_LOCAL -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_ARTIST, table)
                secondaryViewPager.visibility = View.VISIBLE
                secondaryViews.clear()
                secondaryViewPager.removeAllViews()
                secondaryViews.add(transView)
                secondaryViews.add(contentView)
                secondaryViewPager.adapter = ViewPagerAdapter(secondaryViews)
                secondaryViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
            AppConfig.ROUTE_ALBUM_TO_LOCAL -> {
                factory = UIViewMusic(drawer, this)
                val contentView = factory.getView(AppConfig.ROUTE_START_FROM_ALBUM, table)
                secondaryViewPager.visibility = View.VISIBLE
                secondaryViews.clear()
                secondaryViewPager.removeAllViews()
                secondaryViews.add(transView)
                secondaryViews.add(contentView)
                secondaryViewPager.adapter = ViewPagerAdapter(secondaryViews)
                secondaryViewPager.setCurrentItem(PAGE_SECONDARY, true)
            }
        }
    }

    override fun onBack() {
        if (secondaryViewPager.isShown) {
            secondaryViewPager.setCurrentItem(PAGE_MAIN, true)
        } else if (mainViewPager.isShown) {
            mainViewPager.setCurrentItem(PAGE_MAIN, true)
        }
    }

    private inner class ViewPagerAdapter(private val pageViews: List<View>) : PagerAdapter() {
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(pageViews[position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(pageViews[position])
            return pageViews[position]
        }

        override fun getCount(): Int {
            return pageViews.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }
    }

    private fun init() {
        mainViewPager = findViewById(R.id.vp_home_master) as ViewPager
        secondaryViewPager = findViewById(R.id.vp_home_slave) as ViewPager
        mainViews = ArrayList()
        secondaryViews = ArrayList()
        mainViewPager.addOnPageChangeListener(OnPageChangeListenerMain())
        secondaryViewPager.addOnPageChangeListener(OnPageChangeListenerSecondary())
    }

    private inner class OnPageChangeListenerMain : ViewPager.OnPageChangeListener {
        var onPageScrolled = -1
        // 当滑动状态改变时调用
        override fun onPageScrollStateChanged(state: Int) {
            if (onPageScrolled == 0 && state == 0) {
                (view.context as MainActivity).unregisterBackListener(this@UIManager)
                mainViewPager.removeAllViews()
                mainViewPager.visibility = View.INVISIBLE
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

    private inner class OnPageChangeListenerSecondary : ViewPager.OnPageChangeListener {
        var onPageScrolled = -1
        // 当滑动状态改变时调用
        override fun onPageScrollStateChanged(state: Int) {
            if (onPageScrolled == 0 && state == 0) {
                secondaryViewPager.removeAllViews()
                secondaryViewPager.visibility = View.INVISIBLE
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