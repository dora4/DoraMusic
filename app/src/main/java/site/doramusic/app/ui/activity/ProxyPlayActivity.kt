package site.doramusic.app.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dora.util.IntentUtils
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URI
import site.doramusic.app.media.FloatingPlayer

/**
 * 点击音乐文件用朵拉音乐打开，中转的界面，用于启动悬浮播放器。
 */
class ProxyPlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish() // 不显示界面，直接关闭
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data ?: return
            if (IntentUtils.hasOverlayPermission(this)) {
                val serviceIntent = Intent(this, FloatingPlayer::class.java).apply {
                    putExtra(EXTRA_URI, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // 把URI权限交给Service
                grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                startService(serviceIntent)
            } else {
                startActivity(IntentUtils.getRequestOverlayPermissionIntent(packageName))
            }
        }
    }
}