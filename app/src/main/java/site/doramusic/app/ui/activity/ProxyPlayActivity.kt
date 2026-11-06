package site.doramusic.app.ui.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import dora.util.IntentUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_URI
import site.doramusic.app.media.FloatingPlayer
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
import java.nio.charset.StandardCharsets
import androidx.core.net.toUri
import site.doramusic.app.R

/**
 * 点击音乐文件/NFC音乐卡片的中转的界面，用于启动悬浮播放器。NFC卡片写卡协议：doramusic://play?url=xxx。
 */
class ProxyPlayActivity : Activity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private var hasNfcSupport = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNfc()
        handleIntent(intent)
    }

    private fun initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        hasNfcSupport = nfcAdapter != null
        if (!hasNfcSupport) {
            return
        }
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (hasNfcSupport && nfcAdapter?.isEnabled == true) {
            val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                addDataScheme("doramusic")
            }
            val intentFilters = arrayOf(ndefFilter)
            val techLists = arrayOf(arrayOf(Ndef::class.java.name))
            nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        TrackAnalysis.report(scope, EventType.EVENT_TYPE_PROXY_PLAYBACK)
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data ?: return
                startPlayByUri(uri)
                finish()
            }
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                if (!hasNfcSupport) {
                    Toast.makeText(this, getString(R.string.not_support_nfc), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                if (nfcAdapter?.isEnabled == false) {
                    Toast.makeText(this, getString(R.string.please_turn_on_nfc), Toast.LENGTH_SHORT).show()
                    val nfcSetting = Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                    startActivity(nfcSetting)
                    finish()
                    return
                }
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.let { readNdefTag(it) }
                finish()
            }
            else -> {
                finish()
            }
        }
    }
    private fun readNdefTag(tag: Tag) {
        val ndef = Ndef.get(tag) ?: run {
            Toast.makeText(this@ProxyPlayActivity,
                getString(R.string.not_an_ndef_tag), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            ndef.connect()
            val msg = ndef.ndefMessage ?: return
            ndef.close()
            msg.records.forEach { record ->
                if (record.tnf == NdefRecord.TNF_WELL_KNOWN
                    && record.type.contentEquals(NdefRecord.RTD_URI)
                ) {
                    val payload = record.payload
                    val prefix = payload[0].toInt()
                    val body = String(payload.copyOfRange(1, payload.size), StandardCharsets.UTF_8)
                    val fullUri = when (prefix) {
                        0x01 -> "http://www.$body"
                        0x02 -> "https://www.$body"
                        0x03 -> "http://$body"
                        0x04 -> "https://$body"
                        else -> body
                    }
                    // 同时支持doramusic://和http/https
                    if (fullUri.startsWith("doramusic://")
                        || fullUri.startsWith("http://")
                        || fullUri.startsWith("https://")) {
                        val uri = fullUri.toUri()
                        startPlayByUri(uri)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.failed_to_read_tag), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPlayByUri(uri: Uri) {
        if (IntentUtils.hasOverlayPermission(this)) {
            val serviceIntent = Intent(this, FloatingPlayer::class.java).apply {
                putExtra(EXTRA_URI, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startForegroundServiceCompat(serviceIntent)
        } else {
            startActivity(IntentUtils.getRequestOverlayPermissionIntent(packageName))
        }
    }

    private fun startForegroundServiceCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}