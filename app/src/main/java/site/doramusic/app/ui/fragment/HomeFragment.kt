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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter
import dora.BaseFragment
import dora.arouter.open
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.firebase.SpmUtils.spmAdImpression
import dora.firebase.SpmUtils.spmSelectContent
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.skin.SkinManager
import dora.pay.DoraFund
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
import site.doramusic.app.base.conf.AppConfig.Companion.SONG_MAP
import site.doramusic.app.databinding.FragmentHomeBinding
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.event.ChangeSkinEvent
import site.doramusic.app.event.PlayMusicEvent
import site.doramusic.app.event.RefreshFavoriteEvent
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.http.service.AdService
import site.doramusic.app.media.IMediaService
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MusicControl
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.activity.BrowserActivity
import site.doramusic.app.ui.adapter.HomeAdapter
import site.doramusic.app.ui.layout.IPlayerLyricDrawer
import site.doramusic.app.ui.layout.IMenuDrawer
import site.doramusic.app.ui.layout.UIBottomBar
import site.doramusic.app.ui.layout.UIMusicPlay
import site.doramusic.app.util.MusicTimer
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.PrefsManager
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppConfig,
    MusicControl.OnConnectCompletionListener, IPlayerLyricDrawer {

    private lateinit var prefsManager: PrefsManager
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
        prefsManager = PrefsManager(requireContext())
        uiManager = UIManager(this, binding.root)
        bottomBar = UIBottomBar(this, uiManager)
        musicPlay = UIMusicPlay(this, uiManager)
        musicTimer = MusicTimer(bottomBar.handler, musicPlay.handler)
        musicPlay.setMusicTimer(musicTimer)
        if (!prefsManager.isBannerClosed()) {
            loadAds(binding)
        }
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
        if (NetUtils.checkNetworkAvailable(requireContext())) {
            // 有网才加载推荐歌曲
            binding.fpHomeRecommendMusics.visibility = View.VISIBLE
            val titles = SONG_MAP.keys.toList()
            // 随机抽取10条，保证不重复
            titles.shuffled()
                .take(10)
                .forEach { title ->
                    binding.fpHomeRecommendMusics.addText(title)
                }
            binding.fpHomeRecommendMusics.setFlipperListener(object : DoraFlipperView.FlipperListener {

                override fun onFlipFinish() {
                    // 加载完隐藏
                    binding.fpHomeRecommendMusics.visibility = View.GONE
                }

                override fun onFlipStart() {
                }

                override fun onItemClick(text: String) {
                    val url = SONG_MAP[text]
                    spmSelectContent("查看推荐歌曲队列的内容")
                    open(ARoutePath.ACTIVITY_BROWSER) {
                        withString(EXTRA_TITLE, getString(R.string.app_name))
                        withString(EXTRA_URL, url)
                    }
                }
            })
        }
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
            .toObservable(RefreshFavoriteEvent::class.java)
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
                DoraFund.setThemeColor(skinThemeColor)
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
            val adEnable = result(AdService::class) { isShowBannerAds(PRODUCT_NAME) }?.data
            if (adEnable == true) {
                // 广告印象
                spmAdImpression("official")
                binding.banner.visibility = View.VISIBLE
                val banners = result(AdService::class) { getBannerAds(PRODUCT_NAME) }?.data
                val result = arrayListOf<String>()
                if (banners != null) {
                    if (banners.isNotEmpty()) {
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
        val favoriteMusicQuery = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereEqualTo(Music.COLUMN_FAVORITE, Music.IS_FAVORITE)
        )
        val favoriteCount = musicDao.count(favoriteMusicQuery)
        val latestMusicQuery = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0)
        )
        val latestCount =
            ViewUtils.clamp(
                musicDao.count(latestMusicQuery).toFloat(),
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
            // java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
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
            // java.lang.UnsupportedOperationException: Unknown or unsupported URL: content://media/external/audio/albumart/-840129354
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
            // imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return BannerViewHolder(imageView)
        }

        override fun onBindView(holder: BannerViewHolder, data: String, position: Int, size: Int) {
            Glide.with(holder.itemView)
                .load(data)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imageView)
        }

        class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(
            imageView
        )
    }

    /**
     * 获取均衡器支持的频率。
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
            spmSelectContent("打开播放控制器界面")
            musicPlay.open()
        }
    }

    override fun hideDrawer() {
        if (musicPlay.isOpened) {
            spmSelectContent("关闭播放控制器界面")
            musicPlay.close()
        }
    }
}
