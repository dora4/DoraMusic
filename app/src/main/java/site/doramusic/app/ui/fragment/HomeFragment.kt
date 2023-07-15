package site.doramusic.app.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.lsxiao.apollo.core.Apollo
import com.lsxiao.apollo.core.annotations.Receive
import com.lwh.jackknife.av.util.MusicTimer
import com.lwh.jackknife.av.util.MusicUtils
import dora.BaseFragment
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.util.*
import dora.widget.DoraTitleBar
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.databinding.FragmentHomeBinding
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.media.IMediaService
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MusicControl
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.ui.adapter.HomeAdapter
import site.doramusic.app.ui.layout.BottomBarUI
import site.doramusic.app.ui.layout.ILyricDrawer
import site.doramusic.app.ui.layout.MusicPlayUI
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppConfig,
    MusicControl.OnConnectCompletionListener, ILyricDrawer {

    private var uiManager: UIManager? = null
    private var bottomBarUI: BottomBarUI? = null
    private var musicPlayUI: MusicPlayUI? = null
    private var mediaManager: MediaManager? = null
    private var musicTimer: MusicTimer? = null
    private var musicPlayReceiver: MusicPlayReceiver? = null
    private var defaultArtwork: Bitmap? = null
    private var musicDao: OrmDao<Music>? = null
    private var artistDao: OrmDao<Artist>? = null
    private var albumDao: OrmDao<Album>? = null
    private var folderDao: OrmDao<Folder>? = null
    private val adapter = HomeAdapter()

    val isHome: Boolean
        get() = uiManager!!.isLocal && !musicPlayUI!!.isOpened

    val isSlidingDrawerOpened: Boolean
        get() = musicPlayUI!!.isOpened

    data class HomeItem @JvmOverloads constructor(
        @DrawableRes val iconRes: Int, val name: String,
        val musicNum: Long = 0
    ) {

        fun getMusicNum(): String {
            return musicNum.toString()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        musicDao = DaoFactory.getDao(Music::class.java)
        artistDao = DaoFactory.getDao(Artist::class.java)
        albumDao = DaoFactory.getDao(Album::class.java)
        folderDao = DaoFactory.getDao(Folder::class.java)
        mediaManager = MusicApp.instance!!.mediaManager
        mediaManager!!.connectService()
        mediaManager!!.setOnCompletionListener(this)
        defaultArtwork = BitmapFactory.decodeResource(
            resources,
            R.drawable.bottom_bar_cover_bg
        )
        musicPlayReceiver = MusicPlayReceiver()

        val filter = IntentFilter(AppConfig.ACTION_PLAY)
        requireActivity().registerReceiver(musicPlayReceiver, filter)
    }

    private fun getHomeItems(): List<HomeItem> {
        val musicCount = musicDao!!.count()
        val artistCount = artistDao!!.count()
        val albumCount = albumDao!!.count()
        val folderCount = folderDao!!.count()
        val favoriteBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereEqualTo(Music.COLUMN_FAVORITE, 1)
        )
        val favoriteCount = musicDao!!.count(favoriteBuild)
        val latestBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0)
        )
        val latestCount =
            ViewUtils.clamp(musicDao!!.count(latestBuild).toFloat(), 100f, 0f).toLong()
        val homeItems = ArrayList<HomeItem>()
        homeItems.add(HomeItem(R.drawable.ic_local_music, "我的歌曲", musicCount))
        homeItems.add(HomeItem(R.drawable.ic_local_artist, "歌手", artistCount))
        homeItems.add(HomeItem(R.drawable.ic_local_album, "专辑", albumCount))
        homeItems.add(HomeItem(R.drawable.ic_local_folder, "文件夹", folderCount))
        homeItems.add(HomeItem(R.drawable.ic_local_favorite, "我的收藏", favoriteCount))
        homeItems.add(HomeItem(R.drawable.ic_local_latest, "最近播放", latestCount))
        return homeItems
    }

    inner class MusicPlayReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == AppConfig.ACTION_PLAY) {
                val music = mediaManager!!.curMusic
                val playState = mediaManager!!.playState
                val pendingProgress = intent.getIntExtra("pending_progress", 0)
                when (playState) {
                    AppConfig.MPS_INVALID -> {  // 考虑后面加上如果文件不可播放直接跳到下一首
                        musicTimer!!.stopTimer()

                        musicPlayUI!!.refreshUI(0, music!!.duration, music)
                        musicPlayUI!!.showPlay(true)

                        bottomBarUI!!.refreshUI(0, music.duration, music)
                        bottomBarUI!!.setSecondaryProgress(pendingProgress)
                        bottomBarUI!!.showPlay(true)
                    }
                    AppConfig.MPS_PAUSE -> {    //  刷新播放列表当前播放的条目
                        Apollo.emit(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
                        musicTimer!!.stopTimer()

                        musicPlayUI!!.refreshUI(
                            mediaManager!!.position(), music!!.duration,
                            music
                        )
                        musicPlayUI!!.showPlay(true)

                        bottomBarUI!!.refreshUI(
                            mediaManager!!.position(), music.duration,
                            music
                        )
                        bottomBarUI!!.setSecondaryProgress(pendingProgress)
                        bottomBarUI!!.showPlay(true)

                        if (music.albumId != -1) {
                            try {
                                val bitmap = MusicUtils.getCachedArtwork(
                                    context,
                                    music.albumId.toLong(), defaultArtwork
                                )
                                if (bitmap != null) {
                                    mediaManager!!.updateNotification(
                                        bitmap, music.musicName,
                                        music.artist
                                    )
                                }
                            } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                            }
                        } else {
                            mediaManager!!.updateNotification(
                                defaultArtwork!!,
                                music.musicName,
                                music.artist
                            )
                        }
                    }
                    AppConfig.MPS_PLAYING -> {  //刷新播放列表当前播放的条目
                        Apollo.emit(ApolloEvent.REFRESH_MUSIC_PLAY_LIST)
                        musicTimer!!.startTimer()

                        musicPlayUI!!.refreshUI(
                            mediaManager!!.position(), music!!.duration,
                            music
                        )
                        musicPlayUI!!.showPlay(false)
                        // 读取歌词
                        musicPlayUI!!.loadLyric(music)

                        bottomBarUI!!.refreshUI(
                            mediaManager!!.position(), music.duration,
                            music
                        )
                        bottomBarUI!!.setSecondaryProgress(pendingProgress)
                        bottomBarUI!!.showPlay(false)
                        try {
                            val bitmap = MusicUtils.getCachedArtwork(
                                context,
                                music.albumId.toLong(), defaultArtwork
                            )
                            if (music.albumId != -1) {
                                if (bitmap != null) {
                                    mediaManager!!.updateNotification(
                                        bitmap, music.musicName,
                                        music.artist
                                    )
                                    musicPlayUI!!.loadRotateCover(bitmap)
                                } else {
                                    musicPlayUI!!.loadRotateCover(musicPlayUI!!.createDefaultCover())
                                }
                            } else {
                                musicPlayUI!!.loadRotateCover(bitmap)
                                mediaManager!!.updateNotification(
                                    defaultArtwork!!,
                                    music.musicName,
                                    music.artist
                                )
                            }
                        } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                        }
                    }
                    AppConfig.MPS_PREPARE -> {
                        musicTimer!!.stopTimer()

                        musicPlayUI!!.refreshUI(0, music!!.duration, music)
                        musicPlayUI!!.showPlay(true)

                        bottomBarUI!!.setSecondaryProgress(pendingProgress)
                        bottomBarUI!!.refreshUI(0, music.duration, music)
                        bottomBarUI!!.showPlay(true)
                        try {
                            //暂停状态也要刷新Cover
                            val bitmap = MusicUtils.getCachedArtwork(
                                context,
                                music.albumId.toLong(), defaultArtwork
                            )
                            if (music.albumId != -1) {
                                if (bitmap != null) {
                                    musicPlayUI!!.loadRotateCover(bitmap)
                                } else {
                                    musicPlayUI!!.loadRotateCover(musicPlayUI!!.createDefaultCover())
                                }
                            } else {
                                musicPlayUI!!.loadRotateCover(bitmap)
                            }
                        } catch (e: UnsupportedOperationException) {
//                java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
                        }


                    }
                }
            }
        }
    }

    @Receive(ApolloEvent.REFRESH_LOCAL_NUMS)
    fun onMessageEvent() {
        val homeItems = getHomeItems()
        adapter.setList(homeItems)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicPlayReceiver != null) {
            requireActivity().unregisterReceiver(musicPlayReceiver)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mBinding.statusbarHome.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        StatusBarUtils.setLightDarkStatusBar(activity, true, false)
        val drawable = BitmapDrawable()
        drawable.setBounds(
            0, 0, ScreenUtils.getScreenWidth(context),
            DensityUtils.dp2px(context, 26f).toInt()
        )
        mBinding.titlebarHome.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
                (context as MainActivity).openDrawer()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        mBinding.rvHomeModule.adapter = adapter
        mBinding.rvHomeModule.setBackgroundResource(R.drawable.shape_home_module)
        mBinding.rvHomeModule.itemAnimator = DefaultItemAnimator()
        mBinding.rvHomeModule.layoutManager = GridLayoutManager(context, 3)
        adapter.setOnItemClickListener { adapter, view, position ->
            var from = -1
            when (position) {
                0// 我的音乐
                -> from = AppConfig.ROUTE_START_FROM_LOCAL
                1// 歌手
                -> from = AppConfig.ROUTE_START_FROM_ARTIST
                2// 专辑
                -> from = AppConfig.ROUTE_START_FROM_ALBUM
                3// 文件夹
                -> from = AppConfig.ROUTE_START_FROM_FOLDER
                4// 我的最爱
                -> from = AppConfig.ROUTE_START_FROM_FAVORITE
                5 // 最近播放
                -> from = AppConfig.ROUTE_START_FROM_LATEST
            }
            uiManager!!.setContentType(from)
        }
        adapter.setList(getHomeItems())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiManager = context?.let { UIManager(this, view) }
        bottomBarUI = BottomBarUI(this, uiManager!!)
        musicPlayUI = MusicPlayUI(this, uiManager!!)
        musicTimer = MusicTimer(bottomBarUI!!.handler, musicPlayUI!!.handler)
        musicPlayUI!!.setMusicTimer(musicTimer!!)
    }

    override fun onConnectCompletion(service: IMediaService) {
        bottomBarUI!!.initData()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun showDrawer() {
        openSlidingDrawer()
    }

    fun openSlidingDrawer() {
        if (!musicPlayUI!!.isOpened) {
            musicPlayUI!!.open()
        }
    }

    /**
     * 关闭侧边栏。
     */
    fun closeSlidingDrawer() {
        if (musicPlayUI!!.isOpened) {
            musicPlayUI!!.close()
        }
    }

    override fun closeDrawer() {
        closeSlidingDrawer()
    }
}
