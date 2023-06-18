package site.doramusic.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import site.doramusic.app.R
import site.doramusic.app.media.SimpleAudioPlayer

/**
 * 低电提醒。
 */
class BatteryLowReceiver : BroadcastReceiver() {

    private lateinit var player: SimpleAudioPlayer

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        // 手机电量低
        if (action == "android.intent.action.BATTERY_LOW") {
            player = SimpleAudioPlayer(context)
            // 播放电量低的音效
            player.playByRawId(R.raw.battery)
        }
    }
}

