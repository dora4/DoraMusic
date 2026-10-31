package site.doramusic.app.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import dora.util.IntentUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URI
import site.doramusic.app.media.FloatingPlayer
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis

/**
 * 点击音乐文件用朵拉音乐打开，中转的界面，用于启动悬浮播放器。
 */
class ProxyPlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        TrackAnalysis.report(scope, EventType.EVENT_TYPE_PROXY_PLAYBACK)
        if (intent?.action != Intent.ACTION_VIEW) {
            return
        }
        val uri = intent.data ?: return
        if (IntentUtils.hasOverlayPermission(this)) {
            val serviceIntent = Intent(this, FloatingPlayer::class.java).apply {
                putExtra(EXTRA_URI, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startForegroundServiceCompat(serviceIntent)
        } else {
            // TODO: 可缓存uri，待授权完成直接播放
            startActivity(IntentUtils.getRequestOverlayPermissionIntent(packageName))
        }
    }

    /**
     * 兼容 API26 启动前台服务。
     */
    private fun startForegroundServiceCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
