package site.doramusic.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.table.OrmTable
import dora.firebase.SpmUtils
import dora.util.LogUtils
import dora.util.RxBus
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_FAVORITE
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PAUSE_RESUME
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_NEXT
import site.doramusic.app.base.conf.AppConfig.Companion.ACTION_PREV
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_IS_PLAYING
import site.doramusic.app.db.Music
import site.doramusic.app.event.RefreshFavoriteEvent
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MediaService.Companion.NOTIFICATION_NAME
import site.doramusic.app.media.MediaService.Companion.NOTIFICATION_TITLE
import site.doramusic.app.util.MusicUtils

/**
 * 通知栏控制广播。
 */
class MusicPlayReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PAUSE_RESUME -> {
                if (intent.getBooleanExtra(EXTRA_IS_PLAYING, false)) {
                    MediaManager.pause()
                } else {
                    MediaManager.replay()
                }
                val title = intent.getStringExtra(NOTIFICATION_TITLE) ?: ""
                val name = intent.getStringExtra(NOTIFICATION_NAME) ?: ""
                val music = MediaManager.curMusic
                if (music != null) {
                    val defaultArtwork = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.bottom_bar_cover_bg
                    )
                    try {
                        val bitmap = MusicUtils.getCachedArtwork(
                            context, music.albumId.toLong(),
                            defaultArtwork
                        )
                        MediaManager.updateNotification(bitmap, title, name)
                    } catch (e: UnsupportedOperationException) {
                        MediaManager.updateNotification(defaultArtwork, title, name)
                        LogUtils.e(e.toString())
                        //                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                    }
                }
            }

            ACTION_NEXT -> MediaManager.next()
            ACTION_PREV -> MediaManager.prev()
            ACTION_FAVORITE -> {
                val curMusic = MediaManager.curMusic
                if (curMusic == null) {
                    return
                }
                curMusic.let {
                    if (it.favorite == 1) {
                        SpmUtils.selectContent(context, "通知栏取消收藏歌曲")
                        it.favorite = 0
                    } else {
                        SpmUtils.selectContent(context, "通知栏收藏歌曲")
                        it.favorite = 1
                    }
                    MediaManager.updateFavorite(it.favorite)
                    saveFavorite(it, it.favorite)
                    RxBus.getInstance().post(RefreshFavoriteEvent())
                }
            }
//                ACTION_CANCEL -> {
//                    cancelNotification()
//                    killAllOtherProcess(context)
//                    android.os.Process.killProcess(android.os.Process.myPid())
//                }
        }
    }

    private fun saveFavorite(music: Music, favorite: Int) {
        music.favorite = favorite
        DaoFactory.getDao(Music::class.java).update(WhereBuilder.create().addWhereEqualTo(OrmTable.INDEX_ID, music.id), music)
    }
}