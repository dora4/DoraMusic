package site.doramusic.app.ui.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityColorPickerBinding
import site.doramusic.app.event.ChangeSkinEvent
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.ui.adapter.ColorAdapter
import site.doramusic.app.util.PrefsManager
import androidx.core.graphics.drawable.toDrawable
import dora.widget.colorpicker.listener.OnColorSelectListener
import site.doramusic.app.util.ThemeSelector

/**
 * 换肤界面，选择颜色。
 */
@Route(path = ARoutePath.ACTIVITY_COLOR_PICKER)
class ColorPickerActivity : BaseSkinActivity<ActivityColorPickerBinding>() {

    private lateinit var colorDrawable: ColorDrawable
    private lateinit var colorAdapter: ColorAdapter
    private var colors: MutableList<ColorData> = arrayListOf()
    private lateinit var prefsManager: PrefsManager
    private lateinit var customColor: ColorData
    private var selectColor: Int = Color.BLACK

    data class ColorData(val backgroundDrawable: Drawable? = null, val backgroundColor: Int)

    override fun getLayoutId(): Int {
        return R.layout.activity_color_picker
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityColorPickerBinding) {
        binding.statusbarColorPicker.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarColorPicker)
        ThemeSelector.applyViewTheme(binding.titlebarColorPicker)
        binding.titlebarColorPicker.addMenuButton(R.drawable.ic_save)
        binding.titlebarColorPicker.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                if (position == 0) {
                    spmSelectContent("换肤")
                    if (colorAdapter.selectedPosition == 0) {
                        prefsManager.saveSkinColor(selectColor)
                    }
                    changeSkin()
                }
            }
        })
        prefsManager = PrefsManager(this)
        selectColor = ContextCompat.getColor(this, R.color.skin_theme_color_cyan)
        if (prefsManager.getSkinType() == 0) {
            selectColor = prefsManager.getSkinColor()
        }
        customColor = ColorData(getDrawable(R.drawable.shape_color_default),
            ContextCompat.getColor(this, R.color.skin_theme_color_cyan))
        customColor.backgroundDrawable?.setTint(selectColor)
        colors = mutableListOf(
            customColor,
            ColorData(getDrawable(R.drawable.shape_color_cyan),
                ContextCompat.getColor(this, R.color.skin_theme_color_cyan)),
            ColorData(getDrawable(R.drawable.shape_color_orange),
                ContextCompat.getColor(this, R.color.skin_theme_color_orange)),
            ColorData(getDrawable(R.drawable.shape_color_black),
                ContextCompat.getColor(this, R.color.skin_theme_color_black)),
            ColorData(getDrawable(R.drawable.shape_color_green),
                ContextCompat.getColor(this, R.color.skin_theme_color_green)),
            ColorData(getDrawable(R.drawable.shape_color_red),
                ContextCompat.getColor(this, R.color.skin_theme_color_red)),
            ColorData(getDrawable(R.drawable.shape_color_blue),
                ContextCompat.getColor(this, R.color.skin_theme_color_blue)),
            ColorData(getDrawable(R.drawable.shape_color_purple),
                ContextCompat.getColor(this, R.color.skin_theme_color_purple)),
            ColorData(getDrawable(R.drawable.shape_color_yellow),
                ContextCompat.getColor(this, R.color.skin_theme_color_yellow)),
            ColorData(getDrawable(R.drawable.shape_color_pink),
                ContextCompat.getColor(this, R.color.skin_theme_color_pink)),
            ColorData(getDrawable(R.drawable.shape_color_gold),
                ContextCompat.getColor(this, R.color.skin_theme_color_gold))
            )

        colorAdapter = ColorAdapter()
        colorAdapter.setList(colors)
        binding.rvColorPicker.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvColorPicker.itemAnimator = DefaultItemAnimator()
        binding.rvColorPicker.adapter = colorAdapter
        colorAdapter.selectedPosition = prefsManager.getSkinType()
        val skinThemeColor = ThemeSelector.getThemeColor(this)
        colorDrawable = skinThemeColor.toDrawable()
        colorAdapter.setOnItemClickListener { _, _, position ->
            if (position > 0) { // 预设颜色
                val color = colors[position].backgroundColor
                colorDrawable.color = color
                // 预览
                mBinding.statusbarColorPicker.setBackgroundColor(color)
                mBinding.titlebarColorPicker.setBackgroundColor(color)
            } else {    // 自定义颜色
                // 预览
                mBinding.statusbarColorPicker.setBackgroundColor(selectColor)
                mBinding.titlebarColorPicker.setBackgroundColor(selectColor)
            }
            colorAdapter.selectedPosition = position
            colorAdapter.notifyDataSetChanged()
        }
        binding.colorRingView.setOnColorSelectListener(object : OnColorSelectListener {
            override fun onColorSelecting(color: Int) {
            }

            override fun onColorSelected(color: Int) {
                selectColor = color
                customColor.backgroundDrawable?.setTint(color)
                colorAdapter.setData(0, customColor)
                colorAdapter.notifyDataSetChanged()
                // 如果当前就是选中的自定义颜色，预览
                if (colorAdapter.selectedPosition == 0) {
                    mBinding.statusbarColorPicker.setBackgroundColor(selectColor)
                    mBinding.titlebarColorPicker.setBackgroundColor(selectColor)
                }
            }
        })
    }

    /**
     * 更换皮肤。
     */
//    @TimeTrace
    private fun changeSkin() {
        when (colorAdapter.selectedPosition) {
            0 -> {
                prefsManager.saveSkinType(0)
                SkinManager.changeSkin("custom")
            }
            1 -> {
                prefsManager.saveSkinType(1)
                SkinManager.changeSkin("cyan")
            }
            2 -> {
                prefsManager.saveSkinType(2)
                SkinManager.changeSkin("orange")
            }
            3 -> {
                prefsManager.saveSkinType(3)
                SkinManager.changeSkin("black")
            }
            4 -> {
                prefsManager.saveSkinType(4)
                SkinManager.changeSkin("green")
            }
            5 -> {
                prefsManager.saveSkinType(5)
                SkinManager.changeSkin("red")
            }
            6 -> {
                prefsManager.saveSkinType(6)
                SkinManager.changeSkin("blue")
            }
            7 -> {
                prefsManager.saveSkinType(7)
                SkinManager.changeSkin("purple")
            }
            8 -> {
                prefsManager.saveSkinType(8)
                SkinManager.changeSkin("yellow")
            }
            9 -> {
                prefsManager.saveSkinType(9)
                SkinManager.changeSkin("pink")
            }
            10 -> {
                prefsManager.saveSkinType(10)
                SkinManager.changeSkin("gold")
            }
        }
        RxBus.getInstance().post(RefreshHomeItemEvent())
        RxBus.getInstance().post(ChangeSkinEvent())
        ThemeSelector.applyViewTheme(mBinding.statusbarColorPicker)
        ThemeSelector.applyViewTheme(mBinding.titlebarColorPicker)
        finish()
    }
}
