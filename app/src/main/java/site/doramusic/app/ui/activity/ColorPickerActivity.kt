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
import dora.util.RxBus
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
//import site.doramusic.app.annotation.TimeTrace
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.databinding.ActivityColorPickerBinding
import site.doramusic.app.event.ChangeSkinEvent
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.ui.adapter.ColorAdapter
import site.doramusic.app.util.PrefsManager

/**
 * 换肤界面，选择颜色。
 */
@Route(path = ARoutePath.ACTIVITY_COLOR_PICKER)
class ColorPickerActivity : BaseSkinActivity<ActivityColorPickerBinding>() {

    private lateinit var colorDrawable: ColorDrawable
    private lateinit var colorAdapter: ColorAdapter
    private var colors: MutableList<ColorData> = arrayListOf()
    private lateinit var prefsManager: PrefsManager

    data class ColorData(val backgroundResId: Int, val backgroundColor: Int)

    override fun getLayoutId(): Int {
        return R.layout.activity_color_picker
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityColorPickerBinding) {
        binding.statusbarColorPicker.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarColorPicker, COLOR_THEME)
        binding.titlebarColorPicker.addMenuButton(R.drawable.ic_save)
        binding.titlebarColorPicker.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
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
            ColorData(R.drawable.shape_color_cyan,
                ContextCompat.getColor(this, R.color.skin_theme_color_cyan)),
            ColorData(R.drawable.shape_color_orange,
                ContextCompat.getColor(this, R.color.skin_theme_color_orange)),
            ColorData(R.drawable.shape_color_black,
                ContextCompat.getColor(this, R.color.skin_theme_color_black)),
            ColorData(R.drawable.shape_color_green,
                ContextCompat.getColor(this, R.color.skin_theme_color_green)),
            ColorData(R.drawable.shape_color_red,
                ContextCompat.getColor(this, R.color.skin_theme_color_red)),
            ColorData(R.drawable.shape_color_blue,
                ContextCompat.getColor(this, R.color.skin_theme_color_blue)),
            ColorData(R.drawable.shape_color_purple,
                ContextCompat.getColor(this, R.color.skin_theme_color_purple)),
            ColorData(R.drawable.shape_color_yellow,
                ContextCompat.getColor(this, R.color.skin_theme_color_yellow)),
            ColorData(R.drawable.shape_color_pink,
                ContextCompat.getColor(this, R.color.skin_theme_color_pink)),
            ColorData(R.drawable.shape_color_gold,
                ContextCompat.getColor(this, R.color.skin_theme_color_gold))
            )

        colorAdapter = ColorAdapter()
        colorAdapter.setList(colors)
        binding.rvColorPicker.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvColorPicker.itemAnimator = DefaultItemAnimator()
        binding.rvColorPicker.adapter = colorAdapter
        colorAdapter.selectedPosition = if (prefsManager.getSkinType() == 0) 0 else prefsManager.getSkinType() - 1
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        colorDrawable = ColorDrawable(skinThemeColor)
        binding.ivColorPickerPreview.background = colorDrawable
        colorAdapter.setOnItemClickListener { _, _, position ->
            val color = colors[position].backgroundColor
            colorDrawable.color = color
            colorAdapter.selectedPosition = position
            colorAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 更换皮肤。
     */
//    @TimeTrace
    private fun changeSkin() {
        when (colorAdapter.selectedPosition) {
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
            7 -> {
                prefsManager.saveSkinType(8)
                SkinManager.changeSkin("yellow")
            }
            8 -> {
                prefsManager.saveSkinType(9)
                SkinManager.changeSkin("pink")
            }
            9 -> {
                prefsManager.saveSkinType(10)
                SkinManager.changeSkin("gold")
            }
        }
        RxBus.getInstance().post(RefreshHomeItemEvent())
        RxBus.getInstance().post(ChangeSkinEvent())
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarColorPicker, COLOR_THEME)
        finish()
    }
}
