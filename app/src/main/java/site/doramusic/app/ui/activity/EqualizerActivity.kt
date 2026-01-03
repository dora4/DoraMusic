package site.doramusic.app.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import com.alibaba.android.arouter.facade.annotation.Route
import dora.skin.SkinManager
import dora.skin.base.BaseSkinBindingActivity
import dora.util.StatusBarUtils
import dora.widget.DoraEqualizerView
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.databinding.ActivityEqualizerBinding
import site.doramusic.app.media.MediaManager
import site.doramusic.app.util.PrefsManager

/**
 * 均衡器界面。
 */
@Route(path = ARoutePath.ACTIVITY_EQUALIZER)
class EqualizerActivity : BaseSkinBindingActivity<ActivityEqualizerBinding>(),
        DoraEqualizerView.OnUpdateDecibelListener {

    private lateinit var prefsManager: PrefsManager

    override fun getLayoutId(): Int {
        return R.layout.activity_equalizer
    }

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    /**
     * 获取均衡器支持的频率。
     *
     * @return
     */
    private fun getEqualizerFreq(): IntArray {
        val equalizer = Equalizer(0, 0)
        val bands = equalizer.numberOfBands
        val freqs = IntArray(bands.toInt())
        for (i in 0 until bands) {
            val centerFreq = equalizer.getCenterFreq(i.toShort()) / 1000
            freqs[i.toInt()] = centerFreq
        }
        return freqs
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityEqualizerBinding) {
        binding.statusbarEqualizer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        SkinManager.getLoader().setBackgroundColor(binding.statusbarEqualizer, COLOR_THEME)
        prefsManager = PrefsManager(this)
        val equalizerFreq = getEqualizerFreq()
        val size = equalizerFreq.size
        val decibels = IntArray(size)
        val equalizerDecibels = prefsManager.getEqualizerDecibels()
        val values = equalizerDecibels.split(",".toRegex()).toTypedArray()
        if (equalizerDecibels == "" || (values.all { it == "0" } && values.size == size)) {
            // 默认选中第一个
            binding.rgEqualizer.check(R.id.rb_equalizer_close)
            binding.evEqualizer.setDecibels(intArrayOf(0, 0, 0, 0, 0))
            onUpdateDecibel(intArrayOf(0, 0, 0, 0, 0))
            binding.evEqualizer.setTouchable(false)
            binding.evEqualizer.resetState()
        } else {
            for (i in values.indices) {
                decibels[i] = Integer.valueOf(values[i])
            }
            // 选中自定义的tab
            binding.rgEqualizer.check(R.id.rb_equalizer_custom)
            binding.evEqualizer.setDecibels(decibels)
            onUpdateDecibel(decibels)
            binding.evEqualizer.setTouchable(true)
            binding.evEqualizer.resetState()
        }
        binding.rbEqualizerClose.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerCustom.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerPop.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerDance.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerBlue.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerClassic.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerJazz.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerSlow.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerSlots.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerShake.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerCountry.buttonDrawable = BitmapDrawable()

        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        binding.evEqualizer.setThemeColor(skinThemeColor)
        val colors = intArrayOf(skinThemeColor, Color.WHITE)
        val state = arrayOf(intArrayOf(android.R.attr.state_checked), IntArray(0))
        val colorStateList = ColorStateList(state, colors)
        binding.rbEqualizerClose.setTextColor(colorStateList)
        binding.rbEqualizerCustom.setTextColor(colorStateList)
        binding.rbEqualizerPop.setTextColor(colorStateList)
        binding.rbEqualizerDance.setTextColor(colorStateList)
        binding.rbEqualizerBlue.setTextColor(colorStateList)
        binding.rbEqualizerClassic.setTextColor(colorStateList)
        binding.rbEqualizerJazz.setTextColor(colorStateList)
        binding.rbEqualizerSlow.setTextColor(colorStateList)
        binding.rbEqualizerSlots.setTextColor(colorStateList)
        binding.rbEqualizerShake.setTextColor(colorStateList)
        binding.rbEqualizerCountry.setTextColor(colorStateList)
        binding.evEqualizer.setDecibels(decibels)
        binding.evEqualizer.setFreqs(equalizerFreq)
        binding.evEqualizer.setOnUpdateDecibelListener(this)

        binding.rgEqualizer.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_equalizer_close   // 关闭
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(0, 0, 0, 0, 0))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(0, 0, 0, 0, 0))
                }
                R.id.rb_equalizer_custom  // 自定义
                -> {
                    val equalizerDecibelsSP = prefsManager.getEqualizerDecibels()
                    val splitDecibels = equalizerDecibelsSP.split(",".toRegex()).toTypedArray()
                    val result = IntArray(splitDecibels.size)
                    for (i in splitDecibels.indices) {
                        result[i] = Integer.valueOf(splitDecibels[i])
                    }
                    binding.evEqualizer.setDecibels(result)
                    binding.evEqualizer.setTouchable(true)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(result)
                }
                R.id.rb_equalizer_pop // 流行
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(-6, 6, -6, 6, -6))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-6, 6, -6, 6, -6))
                }
                R.id.rb_equalizer_dance   // 舞曲
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(-4, 5, 0, -4, -5))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-4, 5, 0, -4, -5))
                }
                R.id.rb_equalizer_blue    // 蓝调
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(2, -2, -1, 2, 2))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(2, -2, -1, 2, 2))
                }
                R.id.rb_equalizer_classic // 古典
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(-7, -2, 0, 7, 8))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-7, -2, 0, 7, 8))
                }
                R.id.rb_equalizer_jazz    // 爵士
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(5, 3, -3, -1, -2))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(5, 3, -3, -1, -2))
                }
                R.id.rb_equalizer_slow    // 慢摇
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(7, 6, -2, -4, 0))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(7, 6, -2, -4, 0))
                }
                R.id.rb_equalizer_slots   // 电子
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(-8, -1, 5, 0, -3))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-8, -1, 5, 0, -3))
                }
                R.id.rb_equalizer_shake   // 摇滚
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(-7, -2, 4, -1, -4))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(-7, -2, 4, -1, -4))
                }
                R.id.rb_equalizer_country // 乡村
                -> {
                    binding.evEqualizer.setDecibels(intArrayOf(7, 6, -3, -4, 5))
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(intArrayOf(7, 6, -3, -4, 5))
                }
            }
        }
    }

    override fun onUpdateDecibel(decibels: IntArray) {
        prefsManager.saveEqualizerDecibels(decibels)
        MediaManager.setEqualizer(decibels)
    }
}
