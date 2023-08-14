package site.doramusic.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.lsxiao.apollo.core.Apollo
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.media.MediaManager
import site.doramusic.app.ui.layout.BottomBarUI
import site.doramusic.app.ui.layout.MusicPlayUI
import site.doramusic.app.util.MusicTimer
import site.doramusic.app.util.MusicUtils

class MusicPlayReceiver(val mediaManager: MediaManager, val musicTimer: MusicTimer,
        val musicPlayUI: MusicPlayUI, val bottomBarUI: BottomBarUI, val defaultArtwork: Bitmap
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == AppConfig.ACTION_PLAY) {
            val music = mediaManager.curMusic
            val playState = mediaManager.playState
            val pendingProgress = intent.getIntExtra("pending_progress", 0)
            when (playState) {
                AppConfig.MPS_INVALID -> {  // 考虑后面加上如果文件不可播放直接跳到下一首
                    musicTimer.stopTimer()

                    musicPlayUI.refreshUI(0, music!!.duration, music)
                    musicPlayUI.showPlay(true)

                    bottomBarUI.refreshUI(0, music.duration, music)
                    bottomBarUI.setSecondaryProgress(pendingProgress)
                    bottomBarUI.showPlay(true)
                }
                AppConfig.MPS_PAUSE -> {    //  刷新播放列表当前播放的条目
                    Apollo.emit(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
                    musicTimer.stopTimer()

                    musicPlayUI.refreshUI(
                        mediaManager.position(), music!!.duration,
                        music
                    )
                    musicPlayUI.showPlay(true)

                    bottomBarUI.refreshUI(
                        mediaManager.position(), music.duration,
                        music
                    )
                    bottomBarUI.setSecondaryProgress(pendingProgress)
                    bottomBarUI.showPlay(true)

                    if (music.albumId != -1) {
                        try {
                            val bitmap = MusicUtils.getCachedArtwork(
                                context,
                                music.albumId.toLong(), defaultArtwork
                            )
                            if (bitmap != null) {
                                mediaManager.updateNotification(
                                    bitmap, music.musicName,
                                    music.artist
                                )
                            }
                        } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                        }
                    } else {
                        mediaManager.updateNotification(
                            defaultArtwork,
                            music.musicName,
                            music.artist
                        )
                    }
                }
                AppConfig.MPS_PLAYING -> {  //刷新播放列表当前播放的条目
                    Apollo.emit(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
                    musicTimer.startTimer()

                    musicPlayUI.refreshUI(
                        mediaManager.position(), music!!.duration,
                        music
                    )
                    musicPlayUI.showPlay(false)
                    // 读取歌词
                    musicPlayUI.loadLyric(music)

                    bottomBarUI.refreshUI(
                        mediaManager.position(), music.duration,
                        music
                    )
                    bottomBarUI.setSecondaryProgress(pendingProgress)
                    bottomBarUI.showPlay(false)
                    try {
                        val bitmap = MusicUtils.getCachedArtwork(
                            context,
                            music.albumId.toLong(), defaultArtwork
                        )
                        if (music.albumId != -1) {
                            if (bitmap != null) {
                                mediaManager.updateNotification(
                                    bitmap, music.musicName,
                                    music.artist
                                )
                                musicPlayUI.loadRotateCover(bitmap)
                            } else {
                                musicPlayUI.loadRotateCover(musicPlayUI.createDefaultCover())
                            }
                        } else {
                            musicPlayUI.loadRotateCover(bitmap)
                            mediaManager.updateNotification(
                                defaultArtwork,
                                music.musicName,
                                music.artist
                            )
                        }
                    } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                    }
                }
                AppConfig.MPS_PREPARE -> {
                    musicTimer.stopTimer()

                    musicPlayUI.refreshUI(0, music!!.duration, music)
                    musicPlayUI.showPlay(true)

                    bottomBarUI.setSecondaryProgress(pendingProgress)
                    bottomBarUI.refreshUI(0, music.duration, music)
                    bottomBarUI.showPlay(true)
                    try {
                        //暂停状态也要刷新Cover
                        val bitmap = MusicUtils.getCachedArtwork(
                            context,
                            music.albumId.toLong(), defaultArtwork
                        )
                        if (music.albumId != -1) {
                            if (bitmap != null) {
                                musicPlayUI.loadRotateCover(bitmap)
                            } else {
                                musicPlayUI.loadRotateCover(musicPlayUI.createDefaultCover())
                            }
                        } else {
                            musicPlayUI.loadRotateCover(bitmap)
                        }
                    } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                    }


                }
            }
        }
    }
}