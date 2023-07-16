package site.doramusic.app.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import dora.skin.SkinManager
import dora.skin.base.BaseSkinActivity
import dora.util.StatusBarUtils
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.databinding.ActivityEqualizerBinding
import site.doramusic.app.util.PreferencesManager
import site.doramusic.app.widget.EqualizerView

@Route(path = ARoutePath.ACTIVITY_EQUALIZER)
class EqualizerActivity : BaseSkinActivity<ActivityEqualizerBinding>(),
        EqualizerView.OnUpdateDecibelListener {

    private var prefsManager: PreferencesManager? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_equalizer
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }
    override fun initData(savedInstanceState: Bundle?) {
        mBinding.statusbarEqualizer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(mBinding.statusbarEqualizer, "skin_theme_color")
        prefsManager = PreferencesManager(this)
        val equalizerFreq = MusicApp.instance!!.mediaManager!!.equalizerFreq
        val decibels = IntArray(equalizerFreq!!.size)
        if (prefsManager!!.getEqualizerDecibels() != "") {
            val values = prefsManager!!.getEqualizerDecibels().split(",".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in decibels.indices) {
                decibels[i] = Integer.valueOf(values[i])
            }
        }
        mBinding.rbEqualizerClose.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerCustom.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerPop.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerDance.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerBlue.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerClassic.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerJazz.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerSlow.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerSlots.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerShake.buttonDrawable = BitmapDrawable()
        mBinding.rbEqualizerCountry.buttonDrawable = BitmapDrawable()

        val skinThemeColor = SkinManager.getLoader().getColor("skin_theme_color")
        val colors = intArrayOf(skinThemeColor, Color.WHITE)
        val state = arrayOf(intArrayOf(android.R.attr.state_checked), IntArray(0))
        val colorStateList = ColorStateList(state, colors)
        mBinding.rbEqualizerClose.setTextColor(colorStateList)
        mBinding.rbEqualizerCustom.setTextColor(colorStateList)
        mBinding.rbEqualizerPop.setTextColor(colorStateList)
        mBinding.rbEqualizerDance.setTextColor(colorStateList)
        mBinding.rbEqualizerBlue.setTextColor(colorStateList)
        mBinding.rbEqualizerClassic.setTextColor(colorStateList)
        mBinding.rbEqualizerJazz.setTextColor(colorStateList)
        mBinding.rbEqualizerSlow.setTextColor(colorStateList)
        mBinding.rbEqualizerSlots.setTextColor(colorStateList)
        mBinding.rbEqualizerShake.setTextColor(colorStateList)
        mBinding.rbEqualizerCountry.setTextColor(colorStateList)
        mBinding.evEqualizer.setDecibels(decibels)
        mBinding.evEqualizer.freqs = equalizerFreq
        mBinding.evEqualizer.setOnUpdateDecibelListener(this)

        //默认选中第一个
        mBinding.evEqualizer.setDecibels(intArrayOf(0, 0, 0, 0, 0))
        onUpdateDecibel(intArrayOf(0, 0, 0, 0, 0))
        mBinding.evEqualizer.setTouchable(false)
        mBinding.evEqualizer.resetState()

        mBinding.rgEqualizer.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_equalizer_close   //关闭
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(0, 0, 0, 0, 0))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(0, 0, 0, 0, 0))
                }
                R.id.rb_equalizer_custom  //自定义
                -> {
                    val equalizerDecibels = prefsManager!!.getEqualizerDecibels()
                    val splitDecibels = equalizerDecibels.split(",".toRegex()).dropLastWhile {
                        it.isEmpty()
                    }.toTypedArray()
                    val result = IntArray(splitDecibels.size)
                    val reverseResult = IntArray(splitDecibels.size)
                    for (i in splitDecibels.indices) {
                        reverseResult[i] = -Integer.valueOf(splitDecibels[i])
                        result[i] = Integer.valueOf(splitDecibels[i])
                    }
                    mBinding.evEqualizer.setDecibels(reverseResult)
                    mBinding.evEqualizer.setTouchable(true)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(reverseResult)
                }
                R.id.rb_equalizer_pop //流行
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(6, -6, 6, -6, 6))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(6, -6, 6, -6, 6))
                }
                R.id.rb_equalizer_dance   //舞曲
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(4, -5, 0, 4, 5))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(4, -5, 0, 4, 5))
                }
                R.id.rb_equalizer_blue    //蓝调
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(-2, 2, 1, -2, -2))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-2, 2, 1, -2, -2))
                }
                R.id.rb_equalizer_classic //古典
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(7, 2, 0, -7, -8))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(7, 2, 0, -7, -8))
                }
                R.id.rb_equalizer_jazz    //爵士
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(-5, -3, 3, 1, 2))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-5, -3, 3, 1, 2))
                }
                R.id.rb_equalizer_slow    //慢摇
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(-7, -6, 2, 4, 0))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-7, -6, 2, 4, 0))
                }
                R.id.rb_equalizer_slots   //电子
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(8, 1, -5, 0, 3))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(8, 1, -5, 0, 3))
                }
                R.id.rb_equalizer_shake   //摇滚
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(7, 2, -4, 1, 4))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(7, 2, -4, 1, 4))
                }
                R.id.rb_equalizer_country //乡村
                -> {
                    mBinding.evEqualizer.setDecibels(intArrayOf(-7, -6, 3, 4, -5))
                    mBinding.evEqualizer.setTouchable(false)
                    mBinding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-7, -6, 3, 4, -5))
                }
            }
        }
    }

    override fun onUpdateDecibel(decibels: IntArray) {
        prefsManager!!.saveEqualizerDecibels(decibels)
        MusicApp.instance!!.mediaManager!!.setEqualizer(decibels)
    }
}
