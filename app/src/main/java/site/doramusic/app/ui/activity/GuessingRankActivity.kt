package site.doramusic.app.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import dora.util.FragmentUtils
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityGuessingRankBinding
import site.doramusic.app.ui.fragment.GuessingRankBetFragment
import site.doramusic.app.ui.fragment.GuessingRankProfitFragment
import site.doramusic.app.ui.fragment.GuessingRankWinFragment
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_GUESSING_RANK)
class GuessingRankActivity : BaseSkinActivity<ActivityGuessingRankBinding>() {

    private var winFragment: GuessingRankWinFragment? = null
    private var profitFragment: GuessingRankProfitFragment? = null
    private var betFragment: GuessingRankBetFragment? = null

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    private fun initFragments() {
        if (winFragment == null) {
            winFragment = GuessingRankWinFragment()
            FragmentUtils.add(supportFragmentManager, winFragment!!, R.id.fl_container)
        }
        if (profitFragment == null) {
            profitFragment = GuessingRankProfitFragment()
            FragmentUtils.add(supportFragmentManager, profitFragment!!, R.id.fl_container)
        }
        if (betFragment == null) {
            betFragment = GuessingRankBetFragment()
            FragmentUtils.add(supportFragmentManager, betFragment!!, R.id.fl_container)
        }
        showWinFragment()
    }

    private fun showWinFragment() {
        hideFragment()
        if (winFragment == null) {
            winFragment = GuessingRankWinFragment()
            FragmentUtils.add(supportFragmentManager, winFragment!!, R.id.fl_container)
        }
        FragmentUtils.show(winFragment!!)
    }

    private fun showProfitFragment() {
        hideFragment()
        if (profitFragment == null) {
            profitFragment = GuessingRankProfitFragment()
            FragmentUtils.add(supportFragmentManager, profitFragment!!, R.id.fl_container)
        }
        FragmentUtils.show(profitFragment!!)
    }

    private fun showBetFragment() {
        hideFragment()
        if (betFragment == null) {
            betFragment = GuessingRankBetFragment()
            FragmentUtils.add(supportFragmentManager, betFragment!!, R.id.fl_container)
        }
        FragmentUtils.show(betFragment!!)
    }

    private fun hideFragment() {
        if (winFragment != null) {
            FragmentUtils.hide(winFragment!!)
        }
        if (profitFragment != null) {
            FragmentUtils.hide(profitFragment!!)
        }
        if (betFragment != null) {
            FragmentUtils.hide(betFragment!!)
        }
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityGuessingRankBinding) {
        binding.statusbarGuessingRank.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarGuessingRank)
        ThemeSelector.applyViewTheme(binding.titlebarGuessingRank)
        initFragments()
        binding.tabLayout.apply {
            addTab(newTab().setText(R.string.win_rate_ranking))
            addTab(newTab().setText(R.string.profit_loss_ranking))
            addTab(newTab().setText(R.string.bet_ranking))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> showWinFragment()
                        1 -> showProfitFragment()
                        2 -> showBetFragment()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })
            // 禁止长按提示
            post {
                val tabStrip = getChildAt(0) as ViewGroup
                for (i in 0 until tabStrip.childCount) {
                    val tabView = tabStrip.getChildAt(i)
                    tabView.setOnLongClickListener {
                        true
                    }
                    tabView.isLongClickable = false
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_guessing_rank
    }
}
