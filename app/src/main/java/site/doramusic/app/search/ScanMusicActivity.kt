package site.doramusic.app.search

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import dora.BaseActivity
import dora.util.StatusBarUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityScanMusicBinding
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_SCAN_MUSIC)
class ScanMusicActivity : BaseActivity<ActivityScanMusicBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_scan_music
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun getEmbeddedFragmentContainerId(): Int {
        return R.id.container
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityScanMusicBinding) {
        binding.statusbarScanMusic.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        ThemeSelector.applyViewTheme(binding.statusbarScanMusic)
        ThemeSelector.applyViewTheme(binding.titlebarScanMusic)
        addFragment(ScanMusicFragment())
    }
}