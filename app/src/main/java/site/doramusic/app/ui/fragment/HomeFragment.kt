package site.doramusic.app.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter
import dora.BaseFragment
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.firebase.SpmUtils.spmAdImpression
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.http.retrofit.RetrofitManager
import dora.skin.SkinManager
import dora.trade.DoraTrade
import dora.util.*
import dora.widget.DoraFlipperView
import dora.widget.DoraTitleBar
import io.reactivex.android.schedulers.AndroidSchedulers
import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.APP_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_TITLE
import site.doramusic.app.base.conf.AppConfig.Companion.EXTRA_URL
import site.doramusic.app.base.conf.AppConfig.Companion.MUSIC_MENU_GRID_COLUMN_NUM
import site.doramusic.app.base.conf.AppConfig.Companion.MAX_RECENT_MUSIC_NUM
import site.doramusic.app.base.conf.AppConfig.Companion.PRODUCT_NAME
import site.doramusic.app.databinding.FragmentHomeBinding
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.event.ChangeSkinEvent
import site.doramusic.app.event.PlayMusicEvent
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.http.DoraBannerAd
import site.doramusic.app.http.service.AdService
import site.doramusic.app.media.IMediaService
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MusicControl
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.activity.BrowserActivity
import site.doramusic.app.ui.adapter.HomeAdapter
import site.doramusic.app.ui.layout.ILyricDrawer
import site.doramusic.app.ui.layout.IMenuDrawer
import site.doramusic.app.ui.layout.UIBottomBar
import site.doramusic.app.ui.layout.UIMusicPlay
import site.doramusic.app.util.MusicTimer
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.PrefsManager
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppConfig,
    MusicControl.OnConnectCompletionListener, ILyricDrawer {

    private lateinit var uiManager: UIManager
    private lateinit var bottomBar: UIBottomBar
    private lateinit var musicPlay: UIMusicPlay
    private lateinit var musicTimer: MusicTimer
    private lateinit var defaultArtwork: Bitmap
    private lateinit var musicDao: OrmDao<Music>
    private lateinit var artistDao: OrmDao<Artist>
    private lateinit var albumDao: OrmDao<Album>
    private lateinit var folderDao: OrmDao<Folder>
    private val adapter = HomeAdapter()

    private val songMap = mapOf(
        "是你"         to "https://www.youtube.com/watch?v=aM0EBp9OaAM",
        "爱错"         to "https://www.youtube.com/watch?v=AQLuz0wamT8",
        "谁"           to "https://www.youtube.com/watch?v=8z-C8dikNjA",
        "离别开出花"   to "https://www.youtube.com/watch?v=ZYt5Cg4Qqbs",
        "阿衣莫"       to "https://www.youtube.com/watch?v=O1kXtPGjOzw",
        "精卫"         to "https://www.youtube.com/watch?v=YtFQZkGZtLc",
        "不谓侠"       to "https://www.youtube.com/watch?v=DgC942kpOsM",
        "春庭雪"       to "https://www.youtube.com/watch?v=newAggUqhts",
        "卜卦"         to "https://www.youtube.com/watch?v=EWGdVNUVYbE",
        "海市蜃楼"     to "https://www.youtube.com/watch?v=yB8HmL3WSK8",
        "探故知"       to "https://www.youtube.com/watch?v=5ELID57kRPg",
        "难却"         to "https://www.youtube.com/watch?v=KonvHhu3LZU",
        "莫问归期"     to "https://www.youtube.com/watch?v=j1WifUe_fjQ",
        "无情画"       to "https://www.youtube.com/watch?v=LXFKhiAkmso",
        "辞九门回忆"   to "https://www.youtube.com/watch?v=bQ-SVxu-_DI",
        "飞鸟和蝉"     to "https://www.youtube.com/watch?v=-VjwtAYHzBk",
        "如愿"         to "https://www.youtube.com/watch?v=IOb_IX3u2ag",
        "过火"         to "https://www.youtube.com/watch?v=Hj8P88ZtrwM",
        "吻得太逼真"   to "https://www.youtube.com/watch?v=xY37BazFzYE",
        "记事本"       to "https://www.youtube.com/watch?v=9eGwqB68Ngk",
        "下一个天亮"   to "https://www.youtube.com/watch?v=tun3WrH2b3Q",
        "春不晚"       to "https://www.youtube.com/watch?v=uYGN77Cww-w",
        "半点心"       to "https://www.youtube.com/watch?v=sIucMXINXaI",
        "大天蓬"       to "https://www.youtube.com/watch?v=7-_4NcjDlBs",
        "你的万水千山" to "https://www.youtube.com/watch?v=BZQkb7KpOf0",
        "相思遥"       to "https://www.youtube.com/watch?v=ToyWa0Of1ns",
        "典狱司"       to "https://www.youtube.com/watch?v=3H7YHwep2hk",
        "鸳鸯戏"       to "https://www.youtube.com/watch?v=D038bYY7h-U",
        "野孩子"       to "https://www.youtube.com/watch?v=KYZW55KJrK0",
        "赤伶"         to "https://www.youtube.com/watch?v=HOBOBgmzuGo"
    )

    val isHome: Boolean
        get() = uiManager.isLocal && !musicPlay.isOpened

    val isSlidingDrawerOpened: Boolean
        get() = musicPlay.isOpened

    data class HomeItem @JvmOverloads constructor(
        @DrawableRes val iconRes: Int, val name: String,
        val musicNum: Long = 0
    ) {

        fun getMusicNum(): String {
            return musicNum.toString()
        }
    }

    override fun initData(savedInstanceState: Bundle?, binding: FragmentHomeBinding) {
        musicDao = DaoFactory.getDao(Music::class.java)
        artistDao = DaoFactory.getDao(Artist::class.java)
        albumDao = DaoFactory.getDao(Album::class.java)
        folderDao = DaoFactory.getDao(Folder::class.java)
        MediaManager.connectService(requireContext())
        MediaManager.setOnCompletionListener(this)
        defaultArtwork = BitmapFactory.decodeResource(
            resources,
            R.drawable.bottom_bar_cover_bg
        )

        uiManager = UIManager(this, binding.root)
        bottomBar = UIBottomBar(this, uiManager)
        musicPlay = UIMusicPlay(this, uiManager)
        musicTimer = MusicTimer(bottomBar.handler, musicPlay.handler)
        musicPlay.setMusicTimer(musicTimer)

        loadAds(binding)
        binding.statusbarHome.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        StatusBarUtils.setLightDarkStatusBar(activity, true, false)
        val drawable = BitmapDrawable()
        drawable.setBounds(
            0, 0, ScreenUtils.getScreenWidth(context),
            DensityUtils.DP26
        )
        binding.titlebarHome.setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {
            override fun onIconBackClick(icon: AppCompatImageView) {
                (context as IMenuDrawer).openDrawer()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })

        val titles = songMap.keys.toList()
        // 随机抽取10条，保证不重复
        titles.shuffled()
            .take(10)
            .forEach { title ->
                binding.fpHomeRecommendMusics.addText(getString(R.string.recommend_music, title))
            }
        binding.fpHomeRecommendMusics.setFlipperListener(object : DoraFlipperView.FlipperListener {

            override fun onFlipFinish() {
                // 加载完隐藏
                binding.fpHomeRecommendMusics.visibility = View.GONE
            }

            override fun onFlipStart() {
            }

            override fun onItemClick(text: String) {
                val url = songMap[text]
                ARouter.getInstance()
                    .build(ARoutePath.ACTIVITY_BROWSER)
                    .withString(EXTRA_TITLE, text)
                    .withString(EXTRA_URL, url)
                    .navigation()
            }
        })
        binding.rvHomeModule.adapter = adapter
        binding.rvHomeModule.setBackgroundResource(R.drawable.shape_home_module)
        binding.rvHomeModule.itemAnimator = DefaultItemAnimator()
        binding.rvHomeModule.layoutManager = GridLayoutManager(context, MUSIC_MENU_GRID_COLUMN_NUM)
        adapter.setOnItemClickListener { _, _, position ->
            var from = -1
            when (position) {
                0 // 我的音乐
                    -> from = AppConfig.ROUTE_START_FROM_LOCAL

                1 // 歌手
                    -> from = AppConfig.ROUTE_START_FROM_ARTIST

                2 // 专辑
                    -> from = AppConfig.ROUTE_START_FROM_ALBUM

                3 // 文件夹
                    -> from = AppConfig.ROUTE_START_FROM_FOLDER

                4 // 我的最爱
                    -> from = AppConfig.ROUTE_START_FROM_FAVORITE

                5 // 最近播放
                    -> from = AppConfig.ROUTE_START_FROM_LATEST
            }
            uiManager.setContentType(from)
        }
        adapter.setList(getHomeItems())
        addDisposable(RxBus.getInstance()
            .toObservable(RefreshHomeItemEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onRefreshLocalMusic()
            })
        addDisposable(RxBus.getInstance()
            .toObservable(ChangeSkinEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                bottomBar.updateProgressColor()
                val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
                DoraTrade.setThemeColor(skinThemeColor)
            })
        addDisposable(RxBus.getInstance()
            .toObservable(PlayMusicEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onPlayMusic(it.playState, it.pendingProgress)
            })
    }

    override fun isAutoDispose(): Boolean {
        return true
    }

    private fun loadAds(binding: FragmentHomeBinding) {
        net {
            val adEnable = result {
                RetrofitManager.getService(AdService::class.java).isShowBannerAds(PRODUCT_NAME)
            }?.data
            if (adEnable == true) {
                // 广告印象
                spmAdImpression("official")
                binding.banner.visibility = View.VISIBLE

                // dcache-3.0.4新写法，支持api、result、rxApi和rxResult
//                val bannerAds = result(AdService::class) { getBannerAds() }?.data

                val bannerAds = result {
                    // dcache-3.0.1新写法
//                    DoraHttp[AdService::class].getBannerAds()
                    // 旧写法
                    RetrofitManager.getService(AdService::class.java).getBannerAds()
                }?.data
                val result = arrayListOf<String>()
                val banners: MutableList<DoraBannerAd>? = bannerAds
                if (banners != null) {
                    if (banners.size > 0) {
                        for (banner in banners) {
                            banner.imgUrl?.let { result.add(it) }
                        }
                    }
                }
                val imageAdapter = ImageAdapter(result)
                imageAdapter.setOnBannerListener { _, position ->
                    val intent = Intent(activity, BrowserActivity::class.java)
                    intent.putExtra(EXTRA_TITLE, APP_NAME)
                    intent.putExtra(EXTRA_URL, banners?.get(position)?.detailUrl)
                    startActivity(intent)
                }
                binding.banner.setAdapter(imageAdapter)
            }
        }
    }

    private fun getHomeItems(): List<HomeItem> {
        val musicCount = musicDao.count()
        val artistCount = artistDao.count()
        val albumCount = albumDao.count()
        val folderCount = folderDao.count()
        val favoriteBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereEqualTo(Music.COLUMN_FAVORITE, Music.IS_FAVORITE)
        )
        val favoriteCount = musicDao.count(favoriteBuild)
        val latestBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0)
        )
        val latestCount =
            ViewUtils.clamp(
                musicDao.count(latestBuild).toFloat(),
                MAX_RECENT_MUSIC_NUM.toFloat(),
                0f
            ).toLong()
        val homeItems = ArrayList<HomeItem>()
        homeItems.add(HomeItem(R.drawable.ic_local_music_transparent, getString(R.string.my_music), musicCount))
        homeItems.add(HomeItem(R.drawable.ic_local_artist_transparent, getString(R.string.artist), artistCount))
        homeItems.add(HomeItem(R.drawable.ic_local_album_transparent, getString(R.string.album), albumCount))
        homeItems.add(HomeItem(R.drawable.ic_local_folder_transparent, getString(R.string.folder), folderCount))
        homeItems.add(
            HomeItem(
                R.drawable.ic_local_favorite_transparent,
                getString(R.string.my_favorite), favoriteCount
            )
        )
        homeItems.add(
            HomeItem(
                R.drawable.ic_local_latest_transparent,
                getString(R.string.latest_play), latestCount
            )
        )
        return homeItems
    }

    fun onRefreshLocalMusic() {
        val homeItems = getHomeItems()
        adapter.setList(homeItems)
    }

    private fun onPlayMusic(playState: Int, pendingProgress: Int) {
        val music = MediaManager.curMusic ?: run {
            LogUtils.d("当前无歌曲")
            return
        }

        when (playState) {
            AppConfig.MPS_INVALID, AppConfig.MPS_NO_FILE -> {
                musicTimer.stopTimer()
                refreshUI(0, music, true, pendingProgress)
                updateNotification(music)
            }

            AppConfig.MPS_PREPARE -> {
                musicTimer.stopTimer()
                refreshUI(0, music, true, pendingProgress)
                updateNotification(music)

                loadArtwork(music)
            }

            AppConfig.MPS_PAUSE -> {
                musicTimer.startTimer()
                refreshUI(MediaManager.position(), music, true, pendingProgress)
                musicPlay.loadLyric(music)

                updateNotification(music)
                loadArtwork(music)
            }

            AppConfig.MPS_PLAYING -> {
                musicTimer.startTimer()
                refreshUI(MediaManager.position(), music, false, pendingProgress)
                musicPlay.loadLyric(music)

                updateNotification(music)
                loadArtwork(music)
            }
        }
    }

    private fun refreshUI(progress: Int, music: Music, showPlay: Boolean, pendingProgress: Int) {
        musicPlay.refreshUI(progress, music.duration, music)
        musicPlay.showPlay(showPlay)

        bottomBar.refreshUI(progress, music.duration, music)
        bottomBar.setSecondaryProgress(pendingProgress)
        bottomBar.showPlay(showPlay)
    }

    private fun updateNotification(music: Music) {
        try {
            val bitmap =
                MusicUtils.getCachedArtwork(context, music.albumId.toLong(), defaultArtwork)
            MediaManager.updateNotification(bitmap, music.musicName, music.artist)
        } catch (e: UnsupportedOperationException) {
            MediaManager.updateNotification(defaultArtwork, music.musicName, music.artist)
            LogUtils.e(e.toString())
            //                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
        }
    }

    private fun loadArtwork(music: Music) {
        try {
            val bitmap =
                MusicUtils.getCachedArtwork(context, music.albumId.toLong(), defaultArtwork)
            if (music.albumId != -1) {
                musicPlay.loadRotateCover(bitmap ?: musicPlay.createDefaultCover())
            } else {
                musicPlay.loadRotateCover(defaultArtwork)
            }
        } catch (e: UnsupportedOperationException) {
            musicPlay.loadRotateCover(defaultArtwork)
            LogUtils.e(e.toString())
            //                     java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
        }
    }

    class ImageAdapter(banners: List<String>) :
        BannerAdapter<String, ImageAdapter.BannerViewHolder>(banners) {
        // 创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
        override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context)
            // 注意，必须设置为match_parent，这个是viewpager2强制要求的
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            //        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return BannerViewHolder(imageView)
        }

        override fun onBindView(holder: BannerViewHolder, data: String, position: Int, size: Int) {
            // 图片加载自己实现
            Glide.with(holder.itemView)
                .load(data)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .into(holder.imageView)
        }

        inner class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(
            imageView
        )
    }

    /**
     * 获取均衡器支持的频率。
     *
     * @return
     */
    private fun getEqualizerFreq(): IntArray {
        val equalizer = Equalizer(0, 0)
        val bands = equalizer.numberOfBands
        val freqs = IntArray(bands.toInt())
        for (i in 0 until bands) {
            val centerFreq = equalizer.getCenterFreq(i.toShort()) / 1000
            freqs[i] = centerFreq
        }
        return freqs
    }

    private fun applyEqualizer() {
        val prefsManager = PrefsManager(requireContext())
        val equalizerFreq = getEqualizerFreq()
        val size = equalizerFreq.size
        val decibels = IntArray(size)
        val equalizerDecibels = prefsManager.getEqualizerDecibels()
        if (equalizerDecibels.isNotEmpty()) {
            val values = equalizerDecibels.split(",".toRegex()).toTypedArray()
            for (i in values.indices) {
                decibels[i] = Integer.valueOf(values[i])
            }
            MediaManager.setEqualizer(decibels)
        }
    }

    override fun onConnectCompletion(service: IMediaService) {
        applyEqualizer()
        bottomBar.initData()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun showDrawer() {
        if (!musicPlay.isOpened) {
            musicPlay.open()
        }
    }

    override fun hideDrawer() {
        if (musicPlay.isOpened) {
            musicPlay.close()
        }
    }
}
