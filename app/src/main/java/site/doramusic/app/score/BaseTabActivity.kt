package site.doramusic.app.score

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import dora.BaseActivity
import dora.widget.DoraTabBar
import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding

/**
 * Tab页面基类，支持切换两种渲染模式
 * MODE_VIEWPAGER2：原生ViewPager2 + DoraTabBar（你现在的实现）
 * MODE_DVIEW_TAB：dview TabLayoutActivity 封装模式（内部封装Tab逻辑）
 */
abstract class BaseTabActivity : BaseActivity<ActivityPlayerBgSelectBinding>() {

    companion object {
        const val MODE_VIEWPAGER2 = 0
        const val MODE_DVIEW_TAB = 1
    }

    /**
     * 子类重写这个字段，切换tab展示模式
     */
    protected open val tabShowMode: Int = MODE_VIEWPAGER2

    /**
     * 子类提供Tab标题
     */
    protected abstract fun getTabTitles(): List<String>

    /**
     * 子类提供Fragment列表
     */
    protected abstract fun getTabFragments(): List<BackgroundListFragment>

    private var pagerAdapter: BackgroundTabPagerAdapter? = null

    override fun initData(savedInstanceState: Bundle?, binding: ActivityPlayerBgSelectBinding) {
        when (tabShowMode) {
            MODE_VIEWPAGER2 -> initViewPager2Mode(binding)
            MODE_DVIEW_TAB -> initDviewTabMode(binding)
        }
    }

    /**
     * 原有逻辑：ViewPager2 + DoraTabBar
     */
    private fun initViewPager2Mode(binding: ActivityPlayerBgSelectBinding) {
        val fragments = getTabFragments()
        val titles = getTabTitles()

        pagerAdapter = BackgroundTabPagerAdapter(this, fragments)
        binding.viewPager2?.adapter = pagerAdapter

        titles.forEach {
            binding.tabBar?.addTextTab(it)
        }

        binding.tabBar?.setOnTabSelectListener(object : DoraTabBar.OnTabSelectListener {
            override fun onTabSelected(position: Int) {
                binding.viewPager2?.setCurrentItem(position, true)
            }
            override fun onTabReselected(position: Int) {}
        })

        binding.viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabBar?.setCurrentTab(position)
            }
        })
    }

    /**
     * dview TabLayoutActivity 模式
     * dview内部已经封装TabLayout + FragmentPagerAdapter，不再使用ViewPager2
     * 这里只做占位，dview的TabActivity内部会接管tab渲染，
     * 如果你直接继承 dview 的 TabLayoutActivity，这一段可以删掉，改成继承它
     */
    private fun initDviewTabMode(binding: ActivityPlayerBgSelectBinding) {
        // dview TabLayoutActivity 标准用法：
        // 它内部自带 TabLayout + FragmentManager，不需要 ViewPager2
        // 子类只需要覆写 getTabs() 返回 TabBean(title, fragment)
        // 所以如果走这个模式，Activity 直接继承 dview TabLayoutActivity，而不是我们自己的BaseTabActivity
        // 下面是适配说明，后面给你两套方案：【方案1：自己封装双模式基类】【方案2：直接切换继承dview TabLayoutActivity】
    }
}
