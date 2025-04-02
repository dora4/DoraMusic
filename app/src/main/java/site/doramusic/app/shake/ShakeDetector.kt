package site.doramusic.app.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import site.doramusic.app.util.PrefsManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 摇一摇切歌的传感器探测器。
 */
class ShakeDetector(context: Context) : SensorEventListener {

    // 获取传感器管理服务
    private val sensorManager: SensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private var onShakeListener: OnShakeListener? = null
    private val prefsManager: PrefsManager by lazy { PrefsManager(context) }
    private var lowX: Float = 0f
    private var lowY: Float = 0f
    private var lowZ: Float = 0f
    private val isShaking: AtomicBoolean = AtomicBoolean(false)
    private val shakeHandler: Handler by lazy { Handler() }

    companion object {
        private const val FILTERING_VALUE = 0.1f
    }

    private val resetShakeRunnable = Runnable {
        isShaking.set(false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isShaking.get() && prefsManager.getShakeChangeMusic()
            && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            shakeHandler.removeCallbacks(resetShakeRunnable)
            val x = event.values[SensorManager.DATA_X]
            val y = event.values[SensorManager.DATA_Y]
            val z = event.values[SensorManager.DATA_Z]
            lowX = x * FILTERING_VALUE + lowX * (1.0f - FILTERING_VALUE)
            lowY = y * FILTERING_VALUE + lowY * (1.0f - FILTERING_VALUE)
            lowZ = z * FILTERING_VALUE + lowZ * (1.0f - FILTERING_VALUE)
            val highX = x - lowX
            val highY = y - lowY
            val highZ = z - lowZ
            if (highX >= 10 || highY >= 10 || highZ >= 10) {
                if (isShaking.compareAndSet(false, true)) {
                    onShakeListener?.onShake()
                    shakeHandler.postDelayed(resetShakeRunnable, 2000)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 传感器精度改变
    }

    /**
     * 启动摇晃检测--注册监听器。
     */
    fun start() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**
     * 停止摇晃检测--取消监听器。
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * 当摇晃事件发生时，接收通知。
     */
    interface OnShakeListener {
        /**
         * 当手机晃动时被调用。
         */
        fun onShake()
    }

    fun setOnShakeListener(l: OnShakeListener) {
        this.onShakeListener = l
    }
}