package site.doramusic.app.ui.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import dora.util.StatusBarUtils
import dora.widget.DoraEqualizerView
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityEqualizerBinding
import site.doramusic.app.media.MediaManager
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
import site.doramusic.app.util.PrefsManager
import site.doramusic.app.util.ThemeSelector

/**
 * 均衡器界面。EQ预设：5段，顺序 [低频, 低中频, 中频, 中高频, 高频]
 * 正数 = 增益（滑块向下）；负数 = 衰减（滑块向上）
 * 范围：-12 ~ +12 dB
 * 注意：本控件UI方向和常规EQ相反！上拉=衰减，下拉=增益
 */
@Route(path = ARoutePath.ACTIVITY_EQUALIZER)
class EqualizerActivity : BaseSkinActivity<ActivityEqualizerBinding>(),
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
            freqs[i] = centerFreq
        }
        return freqs
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityEqualizerBinding) {
        binding.statusbarEqualizer.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarEqualizer)
        ThemeSelector.applyViewTheme(binding.titlebarEqualizer)
        prefsManager = PrefsManager(this)
        val equalizerFreq = getEqualizerFreq()
        val size = equalizerFreq.size
        val decibels = IntArray(size)
        val equalizerDecibels = prefsManager.getEqualizerDecibels()
        val values = equalizerDecibels.split(",".toRegex()).toTypedArray()
        if (equalizerDecibels == "" || (values.all { it == "0" } && values.size == size)) {
            // 默认选中第一个
            binding.rgEqualizer.check(R.id.rb_equalizer_off)
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
        binding.rbEqualizerOff.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerCustom.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerPop.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerDance.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerBlues.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerClassical.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerJazz.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerSlowRock.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerElectronic.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerRock.buttonDrawable = BitmapDrawable()
        binding.rbEqualizerCountry.buttonDrawable = BitmapDrawable()

        val skinThemeColor = ThemeSelector.getThemeColor(this)
        binding.evEqualizer.setThemeColor(skinThemeColor)
        val colors = intArrayOf(skinThemeColor, Color.WHITE)
        val state = arrayOf(intArrayOf(android.R.attr.state_checked), IntArray(0))
        val colorStateList = ColorStateList(state, colors)
        binding.rbEqualizerOff.setTextColor(colorStateList)
        binding.rbEqualizerCustom.setTextColor(colorStateList)
        binding.rbEqualizerPop.setTextColor(colorStateList)
        binding.rbEqualizerDance.setTextColor(colorStateList)
        binding.rbEqualizerBlues.setTextColor(colorStateList)
        binding.rbEqualizerClassical.setTextColor(colorStateList)
        binding.rbEqualizerJazz.setTextColor(colorStateList)
        binding.rbEqualizerSlowRock.setTextColor(colorStateList)
        binding.rbEqualizerElectronic.setTextColor(colorStateList)
        binding.rbEqualizerRock.setTextColor(colorStateList)
        binding.rbEqualizerCountry.setTextColor(colorStateList)
        binding.evEqualizer.setDecibels(decibels)
        binding.evEqualizer.setFreqs(equalizerFreq)
        binding.evEqualizer.setOnUpdateDecibelListener(this)

        binding.rgEqualizer.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_equalizer_off   // 关闭
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_CLOSE_EQUALIZER)
                    binding.evEqualizer.setDecibels(EQ_PRESET_OFF)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_OFF)
                }
                R.id.rb_equalizer_custom  // 自定义
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "custom")
                    )
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
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "pop")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_POP)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_POP)
                }
                R.id.rb_equalizer_dance   // 舞曲
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "dance")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_DANCE)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_DANCE)
                }
                R.id.rb_equalizer_blues    // 蓝调
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "blues")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_BLUES)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_BLUES)
                }
                R.id.rb_equalizer_classical // 古典
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "classical")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_CLASSICAL)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_CLASSICAL)
                }
                R.id.rb_equalizer_jazz    // 爵士
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "jazz")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_JAZZ)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_JAZZ)
                }
                R.id.rb_equalizer_slow_rock    // 慢摇
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "slow rock")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_SLOW_ROCK)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_SLOW_ROCK)
                }
                R.id.rb_equalizer_electronic   // 电子
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "electronic")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_ELECTRONIC)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_ELECTRONIC)
                }
                R.id.rb_equalizer_rock   // 摇滚
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "rock")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_ROCK)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_ROCK)
                }
                R.id.rb_equalizer_country // 乡村
                -> {
                    TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_USE_EQUALIZER,
                        mapOf("type" to "country")
                    )
                    binding.evEqualizer.setDecibels(EQ_PRESET_COUNTRY)
                    binding.evEqualizer.setTouchable(false)
                    binding.evEqualizer.resetState()
                    onUpdateDecibel(EQ_PRESET_COUNTRY)
                }
            }
        }
    }

    override fun onUpdateDecibel(decibels: IntArray) {
        prefsManager.saveEqualizerDecibels(decibels)
        MediaManager.setEqualizer(decibels)
    }

    companion object {

        // 关闭均衡器
        val EQ_PRESET_OFF = intArrayOf(0, 0, 0, 0, 0)
        // 流行，低+、高+，中间收，浅V
        val EQ_PRESET_POP = intArrayOf(3, 2, -2, 2, 3)
        // 舞曲，低频增益，底鼓有力
        val EQ_PRESET_DANCE = intArrayOf(6, 3, -1, 2, 1)
        // 蓝调，微微抬低、高，收中频
        val EQ_PRESET_BLUES = intArrayOf(2, -2, -1, 2, 2)
        // 古典，压低低频防浑浊，抬高频泛音
        val EQ_PRESET_CLASSICAL = intArrayOf(-7, -2, 0, 7, 8)
        // 爵士，抬低频贝斯，收高频防刺耳
        val EQ_PRESET_JAZZ = intArrayOf(5, 3, -3, -1, -2)
        // 慢摇，低频增益克制，不破
        val EQ_PRESET_SLOW_ROCK = intArrayOf(4, 3, -2, -3, 1)
        // 电子，低频+中频增益，EDM底鼓、合成器
        val EQ_PRESET_ELECTRONIC = intArrayOf(6, 2, 3, 2, -2)
        // 摇滚，低、中增益，扎实有力
        val EQ_PRESET_ROCK = intArrayOf(5, 3, 2, -1, -3)
        // 乡村，轻微低增、高增，干净明亮
        val EQ_PRESET_COUNTRY = intArrayOf(3, 2, -2, -1, 4)
    }
}
