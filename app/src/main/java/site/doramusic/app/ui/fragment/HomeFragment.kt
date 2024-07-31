package site.doramusic.app.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.lsxiao.apollo.core.annotations.Receive
import com.youth.banner.adapter.BannerAdapter
import dora.BaseFragment
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.http.retrofit.RetrofitManager
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
import site.doramusic.app.http.DoraHomeBanner
import site.doramusic.app.http.service.CommonService
import site.doramusic.app.media.IMediaService
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.MusicControl
import site.doramusic.app.receiver.MusicPlayReceiver
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.activity.BrowserActivity
import site.doramusic.app.ui.activity.MainActivity
import site.doramusic.app.ui.adapter.HomeAdapter
import site.doramusic.app.ui.layout.UIBottomBar
import site.doramusic.app.ui.layout.ILyricDrawer
import site.doramusic.app.ui.layout.UIMusicPlay
import site.doramusic.app.util.MusicTimer
import java.util.*

class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppConfig,
    MusicControl.OnConnectCompletionListener, ILyricDrawer {

    private lateinit var uiManager: UIManager
    private lateinit var bottomBar: UIBottomBar
    private lateinit var musicPlay: UIMusicPlay
    private lateinit var mediaManager: MediaManager
    private lateinit var musicTimer: MusicTimer
    private lateinit var musicPlayReceiver: MusicPlayReceiver
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initData(savedInstanceState: Bundle?, binding: FragmentHomeBinding) {
        musicDao = DaoFactory.getDao(Music::class.java)
        artistDao = DaoFactory.getDao(Artist::class.java)
        albumDao = DaoFactory.getDao(Album::class.java)
        folderDao = DaoFactory.getDao(Folder::class.java)
        mediaManager = MusicApp.instance!!.mediaManager!!
        mediaManager.connectService()
        mediaManager.setOnCompletionListener(this)
        defaultArtwork = BitmapFactory.decodeResource(
            resources,
            R.drawable.bottom_bar_cover_bg
        )

        uiManager = UIManager(this, binding.root)
        bottomBar = UIBottomBar(this, uiManager)
        musicPlay = UIMusicPlay(this, uiManager)
        musicTimer = MusicTimer(bottomBar.handler, musicPlay.handler)
        musicPlay.setMusicTimer(musicTimer)
        musicPlayReceiver = MusicPlayReceiver(mediaManager,
            musicTimer, musicPlay, bottomBar, defaultArtwork
        )
        val filter = IntentFilter(AppConfig.ACTION_PLAY)
        activity?.registerReceiver(musicPlayReceiver, filter, Context.RECEIVER_EXPORTED)
        net {
            val bannerCheckResult = result {
                RetrofitManager.getService(CommonService::class.java).checkHomeBanners("doramusic")
            }
            if (bannerCheckResult != null && bannerCheckResult.data == true) {
                binding.banner.visibility = View.VISIBLE
                val bannerResult = result {
                    RetrofitManager.getService(CommonService::class.java).getHomeBanners()
                }
                val result = arrayListOf<String>()
                val banners: MutableList<DoraHomeBanner>? = bannerResult!!.data
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
                    intent.putExtra("title", "Dora Music")
                    intent.putExtra("url", banners?.get(position)?.detailUrl)
                    startActivity(intent)
                }
                binding.banner.setAdapter(imageAdapter)
            }
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
                (context as MainActivity).openDrawer()
            }

            override fun onIconMenuClick(position: Int, icon: AppCompatImageView) {
            }
        })
        binding.rvHomeModule.adapter = adapter
        binding.rvHomeModule.setBackgroundResource(R.drawable.shape_home_module)
        binding.rvHomeModule.itemAnimator = DefaultItemAnimator()
        binding.rvHomeModule.layoutManager = GridLayoutManager(context, 3)
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
    }

    private fun getHomeItems(): List<HomeItem> {
        val musicCount = musicDao.count()
        val artistCount = artistDao.count()
        val albumCount = albumDao.count()
        val folderCount = folderDao.count()
        val favoriteBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereEqualTo(Music.COLUMN_FAVORITE, 1)
        )
        val favoriteCount = musicDao.count(favoriteBuild)
        val latestBuild = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereGreaterThan(Music.COLUMN_LAST_PLAY_TIME, 0)
        )
        val latestCount =
            ViewUtils.clamp(musicDao.count(latestBuild).toFloat(), 100f, 0f).toLong()
        val homeItems = ArrayList<HomeItem>()
        homeItems.add(HomeItem(R.drawable.ic_local_music, "我的歌曲", musicCount))
        homeItems.add(HomeItem(R.drawable.ic_local_artist, "歌手", artistCount))
        homeItems.add(HomeItem(R.drawable.ic_local_album, "专辑", albumCount))
        homeItems.add(HomeItem(R.drawable.ic_local_folder, "文件夹", folderCount))
        homeItems.add(HomeItem(R.drawable.ic_local_favorite, "我的收藏", favoriteCount))
        homeItems.add(HomeItem(R.drawable.ic_local_latest, "最近播放", latestCount))
        return homeItems
    }

    @Receive(ApolloEvent.REFRESH_LOCAL_NUMS)
    fun onRefreshLocalMusic() {
        val homeItems = getHomeItems()
        adapter.setList(homeItems)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(musicPlayReceiver)
    }

    class ImageAdapter(banners: List<String>) : BannerAdapter<String, ImageAdapter.BannerViewHolder>(banners) {
        //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
        override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context)
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            //        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return BannerViewHolder(imageView)
        }

        override fun onBindView(holder: BannerViewHolder, data: String, position: Int, size: Int) {

            //图片加载自己实现
            Glide.with(holder.itemView)
                .load(data)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .into(holder.imageView)
        }

        inner class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(
            imageView
        )
    }

    override fun onConnectCompletion(service: IMediaService) {
        bottomBar.initData()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun showDrawer() {
        openSlidingDrawer()
    }

    fun openSlidingDrawer() {
        if (!musicPlay.isOpened) {
            musicPlay.open()
        }
    }

    /**
     * 关闭侧边栏。
     */
    fun closeSlidingDrawer() {
        if (musicPlay.isOpened) {
            musicPlay.close()
        }
    }

    override fun closeDrawer() {
        closeSlidingDrawer()
    }
}
