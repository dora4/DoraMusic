package site.doramusic.app.media

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dora.util.LogUtils
import dora.util.ProcessUtils
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_FAVORITE
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_NEXT
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PAUSE_RESUME
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PREV
import site.doramusic.app.base.conf.AppConfig.Companion.APP_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.APP_PACKAGE_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_IS_PLAYING
import site.doramusic.app.db.Music
import site.doramusic.app.receiver.MusicPlayReceiver
import site.doramusic.app.shake.ShakeDetector
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.util.PrefsManager

/**
 * 通知兼容要点：
 * Android 8.0+ (O)	必须注册 NotificationChannel	✅
 * Android 9+ (P)	前台服务必须立即调用 startForeground()	✅
 * Android 12+ (S)	PendingIntent 必须声明 FLAG_IMMUTABLE 或 FLAG_MUTABLE	✅
 * Android 13+ (T)	建议设置 setForegroundServiceBehavior()
 * Android 14+ (U)	无新要求，但 IMPORTANCE_LOW 更适合持续型前台通知
 */
class MediaService : Service(), ShakeDetector.OnShakeListener {

    /**
     * 音乐播放流程控制。
     */
    private lateinit var mc: MusicControl
    private var binder: IBinder? = null
    /**
     * 更新通知栏。
     */
    private var notificationManager: NotificationManager? = null
    private var prefsManager: PrefsManager? = null
    private var simplePlayer: SimpleAudioPlayer? = null
    private var remoteViews: RemoteViews? = null
    private lateinit var detector: ShakeDetector
    private var handler: Handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent): IBinder? {
        binder = MediaServiceImpl()
        return binder
    }

    private val shakingRunnable: Runnable = Runnable {
        // 2.再切到下一首播放
        mc.next()
    }

    override fun onShake() {
        handler.removeCallbacks(shakingRunnable)
        // 1.先播放摇一摇切歌的音效
        simplePlayer?.playByRawId(R.raw.shaking)
        handler.postDelayed(shakingRunnable, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        prefsManager = PrefsManager(this)
        simplePlayer = SimpleAudioPlayer(this)
        mc = MusicControl(this)
        detector = ShakeDetector(this)
        detector.setOnShakeListener(this)
        detector.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.e("媒体服务被销毁")
        mc.exit()
        simplePlayer?.exit()
        // 服务被回收，直接退出所有进程
        ProcessUtils.killAllProcesses()
    }

    private inner class MediaServiceImpl : IMediaService.Stub() {

        @Throws(RemoteException::class)
        override fun play(pos: Int): Boolean {
            return mc.play(pos)
        }

        @Throws(RemoteException::class)
        override fun playById(id: Int): Boolean {
            return mc.playById(id)
        }

        @Throws(RemoteException::class)
        override fun playByPath(path: String) {
            mc.play(path)
        }

        @Throws(RemoteException::class)
        override fun playByUrl(music: Music?, url: String) {
            mc.playByUrl(music, url)
        }

        @Throws(RemoteException::class)
        override fun replay(): Boolean {
            return mc.replay()
        }

        @Throws(RemoteException::class)
        override fun pause(): Boolean {
            return mc.pause()
        }

        @Throws(RemoteException::class)
        override fun prev(): Boolean {
            return mc.prev()
        }

        @Throws(RemoteException::class)
        override fun next(): Boolean {
            return mc.next()
        }

        @Throws(RemoteException::class)
        override fun stop() {
            mc.stop()
        }

        @Throws(RemoteException::class)
        override fun duration(): Int {
            return mc.duration()
        }

        @Throws(RemoteException::class)
        override fun setCurMusic(music: Music?) {
            return mc.setCurMusic(music)
        }

        @Throws(RemoteException::class)
        override fun position(): Int {
            return mc.position()
        }

        @Throws(RemoteException::class)
        override fun pendingProgress(): Int {
            return mc.pendingProgress()
        }

        @Throws(RemoteException::class)
        override fun seekTo(progress: Int): Boolean {
            return mc.seekTo(progress)
        }

        @Throws(RemoteException::class)
        override fun refreshPlaylist(playlist: List<Music>) {
            mc.refreshPlaylist(playlist)
        }

        @Throws(RemoteException::class)
        override fun setBassBoost(strength: Int) {
            mc.setBassBoost(strength)
        }

        @Throws(RemoteException::class)
        override fun setEqualizer(bandLevels: IntArray) {
            mc.setEqualizer(bandLevels)
        }

        @Throws(RemoteException::class)
        override fun getEqualizerFreq(): IntArray {
            return mc.equalizerFreq
        }

        @Throws(RemoteException::class)
        override fun getPlayState(): Int {
            return mc.playState
        }

        @Throws(RemoteException::class)
        override fun getPlayMode(): Int {
            return mc.playMode
        }

        @Throws(RemoteException::class)
        override fun setPlayMode(mode: Int) {
            mc.playMode = mode
        }

        @Throws(RemoteException::class)
        override fun getCurMusicId(): Int {
            // 获取的是歌曲的id，而非数据库主键id
            if (mc.curMusic == null) {
                return -1
            }
            return mc.curMusic.songId
        }

        @Throws(RemoteException::class)
        override fun loadCurMusic(music: Music?): Boolean {
            return mc.loadCurMusic(music)
        }

        @Throws(RemoteException::class)
        override fun getCurMusic(): Music? {
            return mc.curMusic
        }

        @Throws(RemoteException::class)
        override fun getPlaylist(): List<Music> {
            return mc.playlist
        }

        @Throws(RemoteException::class)
        override fun updateNotification(bitmap: Bitmap?, title: String, name: String) {
            this@MediaService.updateNotification(bitmap, title, name)
        }

        @Throws(RemoteException::class)
        override fun updateFavorite(favorite: Int) {
            this@MediaService.updateFavorite(favorite)
        }

        @Throws(RemoteException::class)
        override fun cancelNotification() {
            this@MediaService.cancelNotification()
        }
    }

    private fun updateFavorite(favorite: Int) {
        remoteViews?.apply {
            setImageViewResource(
                R.id.iv_nc_favorite,
                if (favorite == 1) R.drawable.ic_favorite_checked else R.drawable.ic_favorite_unchecked
            )
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 🔄 重新刷新通知
        val notification = NotificationCompat.Builder(this, APP_PACKAGE_NAME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pi)
            .setOngoing(true)
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ForegroundServiceType", "RemoteViewLayout")
    private fun updateNotification(bitmap: Bitmap? = null, title: String, name: String) {
        val channelId = APP_PACKAGE_NAME
        val channelName = APP_NAME
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知频道
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                enableLights(false)
                setSound(null, null)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 创建 RemoteViews
        remoteViews = RemoteViews(packageName, R.layout.view_notification).apply {
            if (bitmap != null) {
                setImageViewBitmap(R.id.iv_nc_album, bitmap)
            } else {
                setImageViewResource(R.id.iv_nc_album, R.drawable.bottom_bar_cover_bg)
            }
            setTextViewText(R.id.tv_nc_title, title)
            setTextViewText(R.id.tv_nc_text, name)
            setImageViewResource(
                R.id.iv_nc_pause_resume,
                if (mc.isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
            )
            setImageViewResource(
                R.id.iv_nc_favorite,
                if (mc.curMusic?.favorite == 1) R.drawable.ic_favorite_checked else R.drawable.ic_favorite_unchecked
            )
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseResumeIntent = Intent(ACTION_PAUSE_RESUME).apply {
            component = ComponentName(packageName, MusicPlayReceiver::class.java.name)
            putExtra(NOTIFICATION_TITLE, title)
            putExtra(NOTIFICATION_NAME, name)
            putExtra(EXTRA_IS_PLAYING, mc.isPlaying)
        }
        val pauseResumePIntent = createPendingIntent(this, 1, pauseResumeIntent)
        remoteViews?.setOnClickPendingIntent(R.id.fl_nc_pause_resume, pauseResumePIntent)

        val prevIntent = Intent(ACTION_PREV).apply {
            component = ComponentName(packageName, MusicPlayReceiver::class.java.name)
        }
        val prevPIntent = createPendingIntent(this, 2, prevIntent)
        remoteViews?.setOnClickPendingIntent(R.id.fl_nc_previous, prevPIntent)

        val nextIntent = Intent(ACTION_NEXT).apply {
            component = ComponentName(packageName, MusicPlayReceiver::class.java.name)
        }
        val nextPIntent = createPendingIntent(this, 3, nextIntent)
        remoteViews?.setOnClickPendingIntent(R.id.fl_nc_next, nextPIntent)

        val favoriteIntent = Intent(ACTION_FAVORITE).apply {
            component = ComponentName(packageName, MusicPlayReceiver::class.java.name)
        }
        val favoritePIntent = createPendingIntent(this, 4, favoriteIntent)
        remoteViews?.setOnClickPendingIntent(R.id.fl_nc_favorite, favoritePIntent)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pi)
            .setOngoing(true)
            .setTicker(title)
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * 适配Android12的Caused by java.lang.IllegalArgumentException
     * site.doramusic.app.alpha: Targeting S+ (version 31 and above) requires that one of
     * FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent. Strongly consider
     * using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the PendingIntent
     * being mutable, e.g. if it needs to be used with inline replies or bubbles.
     */
    private fun createPendingIntent(context: Context, requestCode: Int, intent: Intent) : PendingIntent {
        // Android 12+ 需要 FLAG_MUTABLE 才能传递 Extras，否则 Intent 可能为空。
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flag)
    }

    private fun cancelNotification() {
        stopForeground(true)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 0x1
        const val NOTIFICATION_TITLE = "notification_title"
        const val NOTIFICATION_NAME = "notification_name"
    }
}
