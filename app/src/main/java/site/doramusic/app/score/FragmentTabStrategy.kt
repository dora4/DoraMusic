package site.doramusic.app.score

import android.view.View
import android.widget.FrameLayout
import dora.util.FragmentUtils
import dora.widget.DoraTabBar
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding

class FragmentTabStrategy(
    private val activity: PlayerBgSelectActivity
) : TabRenderStrategy {

    private var tabBar: DoraTabBar? = null
    private var container: FrameLayout? = null
    private var fragments: List<BackgroundListFragment> = emptyList()
    private var currentPos = -1

    override fun initTab(
        binding: ActivityPlayerBgSelectBinding,
        titles: List<String>,
        fragments: List<BackgroundListFragment>
    ) {
        this.fragments = fragments
        tabBar = binding.tabBar
        container = binding.flFragmentContainer
        binding.viewPager2?.visibility = View.GONE
        binding.flFragmentContainer?.visibility = View.VISIBLE
        tabBar?.setOnTabSelectListener(object : DoraTabBar.OnTabSelectListener {

            override fun onTabSelected(position: Int) {
                switchFragment(position)
            }

            override fun onTabReselected(position: Int) {}
        })
        // 默认展示第0页
        switchFragment(0)
    }

    private fun switchFragment(pos: Int) {
        if (pos == currentPos) return
        currentPos = pos
        // 先全部隐藏
        fragments.forEach {
            FragmentUtils.hide(it)
        }
        val target = fragments[pos]
        container?.id?.let { containerId ->
            if (!target.isAdded) {
                FragmentUtils.add(activity.supportFragmentManager, target, containerId)
            }
            FragmentUtils.show(target)
        }
    }

    override fun onConfigurationChanged() {
        fragments.forEach { it.adapter.notifyDataSetChanged() }
    }

    override fun release() {
        tabBar = null
        container = null
    }
}