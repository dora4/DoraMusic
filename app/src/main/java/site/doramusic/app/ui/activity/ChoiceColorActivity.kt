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
import com.lwh.jackknife.xskin.SkinLoader
import com.lwh.jackknife.xskin.SkinManager
import com.lwh.jackknife.xskin.util.PrefsUtils
import dora.util.DensityUtils
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import site.doramusic.app.R
import site.doramusic.app.annotation.TimeTrace
import site.doramusic.app.base.BaseSkinActivity
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.databinding.ActivityChoiceColorBinding
import site.doramusic.app.ui.adapter.ChoiceColorAdapter
import site.doramusic.app.util.PreferencesManager

/**
 * 换肤界面，选择颜色。
 */
@Route(path = ARoutePath.ACTIVITY_CHOICE_COLOR)
class ChoiceColorActivity : BaseSkinActivity<ActivityChoiceColorBinding>() {

    private lateinit var colorDrawable: ColorDrawable
    private var choiceColorAdapter: ChoiceColorAdapter? = null
    private var colorDatas: MutableList<ColorData>? = null
    private lateinit var prefsManager: PreferencesManager

    data class ColorData(val backgroundResId: Int, val backgroundColor: Int)

    override fun getLayoutId(): Int {
        return R.layout.activity_choice_color
    }


    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?) {
        mBinding.statusbarChoiceColor.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        mBinding.statusbarChoiceColor.background = ContextCompat.getDrawable(this, SkinLoader.getInstance().getColorRes("skin_theme_color_"+ PrefsUtils(this).suffix))
        val imageView = AppCompatImageView(this)
        val dp24 = DensityUtils.dp2px(24f)
        imageView.layoutParams = RelativeLayout.LayoutParams(dp24, dp24)
        imageView.setImageResource(R.drawable.ic_save)
        mBinding.titlebarChoiceColor.addMenuButton(imageView)

        mBinding.titlebarChoiceColor.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
                if (position == 0) {
                    changeSkin()
                }
            }
        })
        prefsManager = PreferencesManager(this)
        colorDatas = mutableListOf(
            ColorData(R.drawable.block_cyan,
                resources.getColor(R.color.skin_theme_color_cyan)),
            ColorData(R.drawable.block_orange,
                resources.getColor(R.color.skin_theme_color_orange)),
            ColorData(R.drawable.block_black,
                resources.getColor(R.color.skin_theme_color_black)),
            ColorData(R.drawable.block_green,
                resources.getColor(R.color.skin_theme_color_green)),
            ColorData(R.drawable.block_red,
                resources.getColor(R.color.skin_theme_color_red)),
            ColorData(R.drawable.block_blue,
                resources.getColor(R.color.skin_theme_color_blue)),
            ColorData(R.drawable.block_purple,
                resources.getColor(R.color.skin_theme_color_purple)))

        choiceColorAdapter = ChoiceColorAdapter()
        choiceColorAdapter!!.setList(colorDatas!!)
        mBinding.rvChoiceColor.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
//        mBinding.rvChoiceColor.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))
        mBinding.rvChoiceColor.itemAnimator = DefaultItemAnimator()
        mBinding.rvChoiceColor.adapter = choiceColorAdapter
        choiceColorAdapter!!.selectedPosition = if (prefsManager.getSkinType() == 0) 0 else prefsManager.getSkinType() - 1

        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
        mBinding.ivChoiceColorPreview.background = colorDrawable
        choiceColorAdapter!!.setOnItemClickListener { adapter, view, position ->
            val color = colorDatas!![position].backgroundColor
            colorDrawable.color = color
            choiceColorAdapter!!.selectedPosition = position
            choiceColorAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 测试AOP。
     */
    @TimeTrace
    private fun changeSkin() {
        when (choiceColorAdapter!!.selectedPosition) {
            0 -> {
                prefsManager.saveSkinType(1)
                SkinManager.getInstance().changeSkin("cyan")
            }
            1 -> {
                prefsManager.saveSkinType(2)
                SkinManager.getInstance().changeSkin("orange")
            }
            2 -> {
                prefsManager.saveSkinType(3)
                SkinManager.getInstance().changeSkin("black")
            }
            3 -> {
                prefsManager.saveSkinType(4)
                SkinManager.getInstance().changeSkin("green")
            }
            4 -> {
                prefsManager.saveSkinType(5)
                SkinManager.getInstance().changeSkin("red")
            }
            5 -> {
                prefsManager.saveSkinType(6)
                SkinManager.getInstance().changeSkin("blue")
            }
            6 -> {
                prefsManager.saveSkinType(7)
                SkinManager.getInstance().changeSkin("purple")
            }
        }
        mBinding.statusbarChoiceColor.background = ContextCompat.getDrawable(this, SkinLoader.getInstance().getColorRes("skin_theme_color_"+ PrefsUtils(this).suffix))
        finish()
    }
}
