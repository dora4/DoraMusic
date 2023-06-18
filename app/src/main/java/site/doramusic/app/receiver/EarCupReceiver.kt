package site.doramusic.app.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler

import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.media.SimpleAudioPlayer

/**
 * 耳机拨出监听。
 */
class EarCupReceiver : BroadcastReceiver() {

    private lateinit var player: SimpleAudioPlayer

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            changeSpeakerphoneOn(context, true)
            // 只监听拔出耳机使用这个意图
            // 耳机拔出时，暂停音乐播放
            Handler().postDelayed({
                player = SimpleAudioPlayer(context)
                player.playByRawId(R.raw.ear_cup)
            }, 1000)
            pauseMusic()
        } else if (Intent.ACTION_HEADSET_PLUG == action) {
            //            if (intent.hasExtra("state")) {
            //                int state = intent.getIntExtra("state", -1);
            //                if (state == 1) {
            //                    //插入耳机
            //                } else if (state == 0) {
            //                    //拔出耳机
            //                    pauseMusic();
            //                }
            //            }
        } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED == action) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.A2DP) ||
                    BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET) ||
                    BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEALTH) ||
                    BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.GATT)) {
                changeSpeakerphoneOn(context, true)
                //蓝牙耳机失去连接
                Handler().postDelayed({
                    player = SimpleAudioPlayer(context)
                    player.playByRawId(R.raw.bluetooth)
                }, 1000)
                pauseMusic()
            } else if (BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET) ||
                    BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET) ||
                    BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEALTH) ||
                    BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.GATT)) {
                //蓝牙耳机已连接
            }
        }
    }

    private fun pauseMusic() {
        MusicApp.instance!!.mediaManager!!.pause()
    }

    /**
     * 切换播放模式。
     *
     * @param connected
     */
    private fun changeSpeakerphoneOn(context: Context, connected: Boolean) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.isSpeakerphoneOn = connected
    }
}

