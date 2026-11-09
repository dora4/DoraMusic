package site.doramusic.app.score

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import dora.widget.DoraTabBar
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding

class ViewPager2TabStrategy(
    private val activity: PlayerBgSelectActivity
) : TabRenderStrategy {

    private var tabBar: DoraTabBar? = null
    private var viewPager: ViewPager2? = null
    private var pagerAdapter: BackgroundTabPagerAdapter? = null

    override fun initTab(
        binding: ActivityPlayerBgSelectBinding,
        titles: List<String>,
        fragments: List<BackgroundListFragment>
    ) {
        tabBar = binding.tabBar
        viewPager = binding.viewPager2
        binding.viewPager2?.visibility = View.VISIBLE
        binding.flFragmentContainer?.visibility = View.GONE
        pagerAdapter = BackgroundTabPagerAdapter(activity, fragments)
        viewPager?.adapter = pagerAdapter
        tabBar?.setOnTabSelectListener(object : DoraTabBar.OnTabSelectListener {

            override fun onTabSelected(position: Int) {
                viewPager?.setCurrentItem(position, true)
            }

            override fun onTabReselected(position: Int) {}
        })
        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                tabBar?.setCurrentTab(position)
            }
        })
    }

    override fun onConfigurationChanged() {
        // viewpager2 自己处理
    }

    override fun release() {
        viewPager?.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){})
        viewPager = null
        tabBar = null
        pagerAdapter = null
    }
}
