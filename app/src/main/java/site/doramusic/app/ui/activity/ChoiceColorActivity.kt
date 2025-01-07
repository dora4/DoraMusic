package site.doramusic.app.ui.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import dora.firebase.SpmUtils.spmSelectContent
import dora.skin.SkinManager
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
//import site.doramusic.app.annotation.TimeTrace
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.databinding.ActivityChoiceColorBinding
import site.doramusic.app.ui.adapter.ChoiceColorAdapter
import site.doramusic.app.util.PrefsManager

/**
 * 换肤界面，选择颜色。
 */
@Route(path = ARoutePath.ACTIVITY_CHOICE_COLOR)
class ChoiceColorActivity : BaseSkinActivity<ActivityChoiceColorBinding>() {

    private lateinit var colorDrawable: ColorDrawable
    private lateinit var choiceColorAdapter: ChoiceColorAdapter
    private var colors: MutableList<ColorData> = arrayListOf()
    private lateinit var prefsManager: PrefsManager

    data class ColorData(val backgroundResId: Int, val backgroundColor: Int)

    override fun getLayoutId(): Int {
        return R.layout.activity_choice_color
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityChoiceColorBinding) {
        binding.statusbarChoiceColor.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarChoiceColor, COLOR_THEME)
        binding.titlebarChoiceColor.addMenuButton(R.drawable.ic_save)
        binding.titlebarChoiceColor.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                if (position == 0) {
                    spmSelectContent("换肤")
                    changeSkin()
                }
            }
        })
        prefsManager = PrefsManager(this)
        colors = mutableListOf(
            ColorData(R.drawable.cyan_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_cyan)),
            ColorData(R.drawable.orange_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_orange)),
            ColorData(R.drawable.black_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_black)),
            ColorData(R.drawable.green_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_green)),
            ColorData(R.drawable.red_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_red)),
            ColorData(R.drawable.blue_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_blue)),
            ColorData(R.drawable.purple_bg,
                ContextCompat.getColor(this, R.color.skin_theme_color_purple)))

        choiceColorAdapter = ChoiceColorAdapter()
        choiceColorAdapter.setList(colors)
        binding.rvChoiceColor.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvChoiceColor.itemAnimator = DefaultItemAnimator()
        binding.rvChoiceColor.adapter = choiceColorAdapter
        choiceColorAdapter.selectedPosition = if (prefsManager.getSkinType() == 0) 0 else prefsManager.getSkinType() - 1

        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.ivChoiceColorPreview.background = colorDrawable
        choiceColorAdapter.setOnItemClickListener { _, _, position ->
            val color = colors[position].backgroundColor
            colorDrawable.color = color
            choiceColorAdapter.selectedPosition = position
            choiceColorAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 测试AOP。
     */
//    @TimeTrace
    private fun changeSkin() {
        when (choiceColorAdapter.selectedPosition) {
            0 -> {
                prefsManager.saveSkinType(1)
                SkinManager.changeSkin("cyan")
            }
            1 -> {
                prefsManager.saveSkinType(2)
                SkinManager.changeSkin("orange")
            }
            2 -> {
                prefsManager.saveSkinType(3)
                SkinManager.changeSkin("black")
            }
            3 -> {
                prefsManager.saveSkinType(4)
                SkinManager.changeSkin("green")
            }
            4 -> {
                prefsManager.saveSkinType(5)
                SkinManager.changeSkin("red")
            }
            5 -> {
                prefsManager.saveSkinType(6)
                SkinManager.changeSkin("blue")
            }
            6 -> {
                prefsManager.saveSkinType(7)
                SkinManager.changeSkin("purple")
            }
        }
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarChoiceColor, COLOR_THEME)
        finish()
    }
}
