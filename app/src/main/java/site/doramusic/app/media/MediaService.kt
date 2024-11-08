package site.doramusic.app.media

import android.app.*
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.*
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_CANCEL
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_NEXT
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PAUSE_RESUME
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PREV
import site.doramusic.app.db.Music
import site.doramusic.app.shake.ShakeDetector
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.PreferencesManager

/**
 * 这个服务单独开启一个进程，用来在Android系统后台播放音乐。
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
    private var controlBroadcast: ControlBroadcast? = null
    private var prefsManager: PreferencesManager? = null
    private var simplePlayer: SimpleAudioPlayer? = null
    private var remoteViews: RemoteViews? = null
    private lateinit var detector: ShakeDetector

    override fun onBind(intent: Intent): IBinder? {
        binder = MediaServiceImpl()
        return binder
    }

    override fun onShake() {
        //先播放摇一摇切歌的音效
        simplePlayer?.playByRawId(R.raw.shaking)
        //再切到下一首播放
        Handler().postDelayed({ mc.next() }, 2000)
    }

    inner class ControlBroadcast : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PAUSE_RESUME -> {
                    if (mc.isPlaying) {
                        mc.pause()
                    } else {
                        mc.replay()
                    }
                    val title = intent.getStringExtra(NOTIFICATION_TITLE) ?: ""
                    val name = intent.getStringExtra(NOTIFICATION_NAME) ?: ""
                    val music = mc.curMusic
                    val defaultArtwork = BitmapFactory.decodeResource(this@MediaService.resources,
                            R.drawable.bottom_bar_cover_bg)
                    val bitmap = MusicUtils.getCachedArtwork(this@MediaService, music.albumId.toLong(),
                            defaultArtwork)
                    updateNotification(bitmap, title, name)
                }
                ACTION_NEXT -> mc.next()
                ACTION_PREV -> mc.prev()
                ACTION_CANCEL -> {
                    cancelNotification()
                    killAllOtherProcess(context)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun killAllOtherProcess(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessList = am.runningAppProcesses ?: return
        for (ai in appProcessList) {
            if (ai.uid == android.os.Process.myUid() && ai.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(ai.pid)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        controlBroadcast = ControlBroadcast()
        val filter = IntentFilter()
        filter.addAction(ACTION_PAUSE_RESUME)
        filter.addAction(ACTION_PREV)
        filter.addAction(ACTION_NEXT)
        filter.addAction(ACTION_CANCEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(controlBroadcast, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(controlBroadcast, filter)
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        prefsManager = PreferencesManager(this)
        simplePlayer = SimpleAudioPlayer(this)
        mc = MusicControl(this)
        detector = ShakeDetector(this)
        detector.setOnShakeListener(this)
        detector.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(controlBroadcast)
        mc.exit()
        simplePlayer?.exit()
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
        override fun playByUrl(music: Music, url: String) {
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
        override fun setCurMusic(music: Music) {
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
            return mc.curMusic.songId
        }

        @Throws(RemoteException::class)
        override fun loadCurMusic(music: Music): Boolean {
            return mc.loadCurMusic(music)
        }

        @Throws(RemoteException::class)
        override fun getCurMusic(): Music {
            return mc.curMusic
        }

        @Throws(RemoteException::class)
        override fun getPlaylist(): List<Music> {
            return mc.playlist
        }

        @Throws(RemoteException::class)
        override fun updateNotification(bitmap: Bitmap, title: String, name: String) {
            this@MediaService.updateNotification(bitmap, title, name)
        }

        @Throws(RemoteException::class)
        override fun cancelNotification() {
            this@MediaService.cancelNotification()
        }
    }

    private fun updateNotification(bitmap: Bitmap?, title: String, name: String) {
        val channelId = "site.doramusic.app"
        val channelName = "DoraMusic"
        val notificationChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(false)
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pi = PendingIntent.getActivity(this,
            0, intent, PendingIntent.FLAG_IMMUTABLE)
        remoteViews = RemoteViews(packageName, R.layout.view_notification)
        val notification: Notification = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this).build()
        } else {
            Notification.Builder(this).setChannelId(channelId).build()
        }
        notification.icon = R.mipmap.ic_launcher
        notification.tickerText = title
        notification.contentIntent = pi
        notification.contentView = remoteViews
        notification.flags = Notification.FLAG_NO_CLEAR

        if (bitmap != null) {
            remoteViews?.setImageViewBitmap(R.id.iv_nc_album, bitmap)
        } else {
            remoteViews?.setImageViewResource(R.id.iv_nc_album, R.drawable.bottom_bar_cover_bg)
        }
        remoteViews?.setTextViewText(R.id.tv_nc_title, title)
        remoteViews?.setTextViewText(R.id.tv_nc_text, name)
        if (mc.isPlaying) {
            remoteViews?.setImageViewResource(R.id.iv_nc_pause_resume, R.drawable.ic_notification_pause)
        } else {
            remoteViews?.setImageViewResource(R.id.iv_nc_pause_resume, R.drawable.ic_notification_play)
        }

        val pauseResumeIntent = Intent(ACTION_PAUSE_RESUME)
        pauseResumeIntent.component = ComponentName(packageName, ControlBroadcast::class.java.name)
        pauseResumeIntent.putExtra(NOTIFICATION_TITLE, title)
        pauseResumeIntent.putExtra(NOTIFICATION_NAME, name)

        val pauseResumePIntent = createPendingIntent(this, 1, pauseResumeIntent)
        remoteViews?.setOnClickPendingIntent(R.id.iv_nc_pause_resume, pauseResumePIntent)

        val prevIntent = Intent(ACTION_PREV)
        prevIntent.component = ComponentName(packageName, ControlBroadcast::class.java.name)
        val prevPIntent = createPendingIntent(this, 2, prevIntent)
        remoteViews?.setOnClickPendingIntent(R.id.iv_nc_previous, prevPIntent)

        val nextIntent = Intent(ACTION_NEXT)
        nextIntent.component = ComponentName(packageName, ControlBroadcast::class.java.name)
        val nextPIntent = createPendingIntent(this, 3, nextIntent)
        remoteViews?.setOnClickPendingIntent(R.id.iv_nc_next, nextPIntent)

        val cancelIntent = Intent(ACTION_CANCEL)
        cancelIntent.component = ComponentName(packageName, ControlBroadcast::class.java.name)
        val cancelPIntent = createPendingIntent(this, 4, cancelIntent)
        remoteViews?.setOnClickPendingIntent(R.id.iv_nc_cancel, cancelPIntent)

        // 显示通知
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 适配Android12的Caused by java.lang.IllegalArgumentException
     * site.doramusic.app.alpha: Targeting S+ (version 31 and above) requires that one of
     * FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent. Strongly consider
     * using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the PendingIntent
     * being mutable, e.g. if it needs to be used with inline replies or bubbles.
     */
    private fun createPendingIntent(context: Context, requestCode: Int, intent: Intent) : PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flag)
        return pendingIntent
    }

    private fun cancelNotification() {
        stopForeground(true)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 0x1
        private const val NOTIFICATION_TITLE = "notification_title"
        private const val NOTIFICATION_NAME = "notification_name"
    }
}
