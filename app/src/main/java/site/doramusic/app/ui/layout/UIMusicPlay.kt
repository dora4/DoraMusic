package site.doramusic.app.ui.layout

import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lsxiao.apollo.core.Apollo
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.db.table.OrmTable
import dora.util.DensityUtils
import dora.util.ScreenUtils
import site.doramusic.app.MusicApp
import site.doramusic.app.R
//import site.doramusic.app.annotation.SingleClick
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Music
import site.doramusic.app.lrc.LyricLine
import site.doramusic.app.lrc.LyricScroller
import site.doramusic.app.lrc.loader.DoraLyricLoader
import site.doramusic.app.lrc.loader.LyricLoader
import site.doramusic.app.media.MediaManager
import site.doramusic.app.media.PlayModeControl
import site.doramusic.app.ui.UIFactory
import site.doramusic.app.ui.UIManager
import site.doramusic.app.ui.adapter.LyricAdapter
import site.doramusic.app.util.MusicTimer
import site.doramusic.app.widget.RotateCoverView
import site.doramusic.app.widget.SlidingView

/**
 * 歌词滚动界面。
 */
class UIMusicPlay(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
        View.OnClickListener, AppConfig, SeekBar.OnSeekBarChangeListener,
        SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener {

    var handler: Handler
    private val curVolume: Int
    private val maxVolume: Int
    private val audioManager: AudioManager by lazy {
        manager.view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val mediaManager: MediaManager by lazy {
        MusicApp.app!!.mediaManager!!
    }
    private val contentView: View = manager.view
    private var btnMusicPlayMode: ImageButton? = null
    private var llMusicPlayVolume: LinearLayout? = null
    private var sbMusicPlayPlayback: SeekBar? = null
    private var sbMusicPlayVolume: SeekBar? = null
    private var tvSlidingMusicName: TextView? = null
    private var tvSlidingArtist: TextView? = null
    private var tvMusicPlayTotalTime: TextView? = null
    private var tvMusicPlayCurTime: TextView? = null
    private var btnMusicPlayPrev: ImageButton? = null
    private var btnMusicPlayNext: ImageButton? = null
    private var btnMusicPlayPlay: ImageButton? = null
    private var btnMusicPlayPause: ImageButton? = null
    private var btnMusicPlayVolume: ImageButton? = null
    private var btnMusicPlayFavorite: ImageButton? = null
    private var statusBarLyric: View? = null
    private var slidingView: SlidingView? = null
    private var curMusic: Music? = null
    private var viewPager: ViewPager? = null
    private var coverLrcContainer: FrameLayout? = null
    private var coverContainer: FrameLayout? = null
    private var rotateCoverView: RotateCoverView? = null
    private var lrcEmptyView: TextView? = null
    private var lrcListView: ListView? = null
    private val lyricAdapter: LyricAdapter by lazy {
        LyricAdapter(manager.view.context)
    }
    private var musicTimer: MusicTimer? = null
    private var playAuto: Boolean = false
    private var seekBarProgress: Int = 0
    private var rvHomeModule: RecyclerView? = null
    private val musicDao: OrmDao<Music> by lazy {
        DaoFactory.getDao(Music::class.java)
    }
    private val lyricScroller: LyricScroller = LyricScroller()
    private val volumeHandler: Handler = Handler()
    private val lyricLoader: LyricLoader
    private val playModeControl: PlayModeControl by lazy {
        PlayModeControl(manager.view.context)
    }

    val isOpened: Boolean
        get() = slidingView!!.isOpened

    internal var r: Runnable = Runnable {
        llMusicPlayVolume!!.visibility = View.INVISIBLE
        llMusicPlayVolume!!.startAnimation(AnimationUtils.loadAnimation(this.manager.view.context, R.anim.anim_fade_out))
    }

    private val lyricListener = object : LyricScroller.LyricListener {

        override fun onLyricLoaded(lyricLines: MutableList<LyricLine>, index: Int) {
            lyricAdapter.setLyric(lyricLines)
            lyricAdapter.setCurrentSentenceIndex(index)
            lyricAdapter.notifyDataSetChanged()
        }

        override fun onLyricSentenceChanged(indexOfCurSentence: Int) {
            lyricAdapter.setCurrentSentenceIndex(indexOfCurSentence)
            lyricAdapter.notifyDataSetChanged()
            lrcListView!!.smoothScrollToPositionFromTop(indexOfCurSentence,
                lrcListView!!.height / 2, 500)
        }
    }

    init {
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        initViews()
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                refreshSeekProgress(
                    mediaManager.position(),
                        mediaManager.duration())
            }
        }
        lyricLoader = DoraLyricLoader(lyricScroller, lyricListener)
    }

    private fun initViews() {
        rvHomeModule = findViewById(R.id.rv_home_module) as RecyclerView
        slidingView = findViewById(R.id.sv_home_drawer) as SlidingView
        statusBarLyric = findViewById(R.id.statusbar_lyric)
        tvSlidingMusicName = findViewById(R.id.tv_sliding_music_name) as TextView
        tvSlidingArtist = findViewById(R.id.tv_sliding_artist) as TextView
        btnMusicPlayPrev = findViewById(R.id.btn_music_play_prev) as ImageButton
        btnMusicPlayNext = findViewById(R.id.btn_music_play_next) as ImageButton
        btnMusicPlayPlay = findViewById(R.id.btn_music_play_play) as ImageButton
        btnMusicPlayPause = findViewById(R.id.btn_music_play_pause) as ImageButton
        btnMusicPlayVolume = findViewById(R.id.btn_music_play_volume) as ImageButton
        btnMusicPlayMode = findViewById(R.id.btn_music_play_mode) as ImageButton
        btnMusicPlayFavorite = findViewById(R.id.btn_music_play_favorite) as ImageButton
        statusBarLyric!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight())
        rotateCoverView = RotateCoverView(manager.view.context)
        viewPager = findViewById(R.id.vp_music_play_cover_lyric) as ViewPager

        lrcEmptyView = TextView(manager.view.context)
        lrcEmptyView!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        lrcEmptyView!!.gravity = Gravity.CENTER
        lrcEmptyView!!.text = "暂无歌词"
        lrcEmptyView!!.setTextColor(Color.WHITE)
        lrcListView = ListView(manager.view.context)
        lrcListView!!.isVerticalScrollBarEnabled = false
        lrcListView!!.adapter = lyricAdapter
        lrcListView!!.emptyView = lrcEmptyView
        lrcListView!!.overScrollMode = AbsListView.OVER_SCROLL_NEVER
        lrcListView!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context,
            android.R.anim.fade_in))
        coverLrcContainer = FrameLayout(manager.view.context)
        coverLrcContainer!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        coverContainer = FrameLayout(manager.view.context)
        coverContainer!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        val dp40 = DensityUtils.dp2px(40f)
        val lp = FrameLayout.LayoutParams(ScreenUtils.getScreenWidth() - dp40,
            ScreenUtils.getScreenWidth() - dp40)
        lp.gravity = Gravity.CENTER
        coverContainer!!.addView(rotateCoverView, lp)
        coverLrcContainer!!.addView(lrcListView)
        coverLrcContainer!!.addView(lrcEmptyView)
        val pageViews: MutableList<View>  = ArrayList()
        pageViews.add(coverContainer!!)
        pageViews.add(coverLrcContainer!!)
        viewPager!!.adapter = MusicPlayPagerAdapter(pageViews)
        slidingView!!.setOnDrawerCloseListener(this)
        slidingView!!.setOnDrawerOpenListener(this)

        btnMusicPlayPrev!!.setOnClickListener(this)
        btnMusicPlayNext!!.setOnClickListener(this)
        btnMusicPlayPlay!!.setOnClickListener(this)
        btnMusicPlayPause!!.setOnClickListener(this)
        btnMusicPlayVolume!!.setOnClickListener(this)
        btnMusicPlayMode!!.setOnClickListener(this)
        btnMusicPlayFavorite!!.setOnClickListener(this)
        sbMusicPlayPlayback = findViewById(R.id.sb_music_play_playback) as SeekBar
        sbMusicPlayVolume = findViewById(R.id.sb_music_play_volume) as SeekBar
        sbMusicPlayVolume!!.max = maxVolume
        sbMusicPlayVolume!!.progress = curVolume
        sbMusicPlayPlayback!!.setOnSeekBarChangeListener(this)
        sbMusicPlayVolume!!.setOnSeekBarChangeListener(this)
        tvMusicPlayCurTime = findViewById(R.id.tv_music_play_cur_time) as TextView
        tvMusicPlayTotalTime = findViewById(R.id.tv_music_play_total_time) as TextView

        llMusicPlayVolume = findViewById(R.id.ll_music_play_volume) as LinearLayout
        sbMusicPlayVolume!!.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> volumeHandler.removeCallbacks(r)
                MotionEvent.ACTION_UP -> {
                    llMusicPlayVolume!!.visibility = View.INVISIBLE
                    llMusicPlayVolume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_out))
                }
            }
            false
        }
    }

    class MusicPlayPagerAdapter(private val pageViews: List<View>?) : PagerAdapter() {

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(pageViews!![position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(pageViews!![position])
            return pageViews[position]
        }

        override fun getCount(): Int {
            return pageViews!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }
    }

    private fun findViewById(id: Int): View {
        return contentView.findViewById(id)
    }

    fun setMusicTimer(timer: MusicTimer) {
        this.musicTimer = timer
    }

    fun open() {
        slidingView!!.visibility = View.VISIBLE
        slidingView!!.animateOpen()
    }

    fun close() {
        slidingView!!.animateClose()
    }

    fun showPlay(flag: Boolean) {
        if (flag) {
            btnMusicPlayPlay!!.visibility = View.VISIBLE
            btnMusicPlayPause!!.visibility = View.GONE
            rotateCoverView!!.pauseRotateAnimation()
        } else {
            btnMusicPlayPlay!!.visibility = View.GONE
            btnMusicPlayPause!!.visibility = View.VISIBLE
            rotateCoverView!!.startRotateAnimation()
        }
    }

    private fun startAnimation(view: View) {
        view.visibility = View.VISIBLE
        val fromX = view.left
        val fromY = view.top

        val animSet = AnimationSet(true)
        // 注：ABSOLUTE表示离当前自己的View绝对的像素单位
        // 使用RELATIVE_TO_SELF和RELATIVE_TO_PARENT时一般用倍数关系 一般用1f 0f
        // 表示相对于自身或父控件几倍的移动
        val transAnim = TranslateAnimation(
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, (-fromX).toFloat(),
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, (-fromY).toFloat())

        val alphaAnim1 = AlphaAnimation(0f, 1f)
        val scaleAnim1 = ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_PARENT.toFloat(), Animation.RELATIVE_TO_PARENT.toFloat())

        val alphaAnim2 = AlphaAnimation(1f, 0f)
        val scaleAnim2 = ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_PARENT.toFloat(), Animation.RELATIVE_TO_PARENT.toFloat())

        transAnim.duration = 600

        scaleAnim1.duration = 600
        alphaAnim1.duration = 600

        scaleAnim2.duration = 800
        alphaAnim2.duration = 800
        scaleAnim2.startOffset = 600
        alphaAnim2.startOffset = 600
        transAnim.startOffset = 600

        animSet.addAnimation(scaleAnim1)
        animSet.addAnimation(alphaAnim1)

        animSet.addAnimation(scaleAnim2)
        animSet.addAnimation(alphaAnim2)
        animSet.addAnimation(transAnim)
        view.startAnimation(animSet)
        view.visibility = View.GONE
    }

    private fun stopAnimation(view: View) {
        view.visibility = View.GONE
        view.clearAnimation()
    }

    private fun refreshFavorite(favorite: Int) {
        if (favorite == 1) {
            btnMusicPlayFavorite!!.setImageResource(R.drawable.ic_favorite_checked)
        } else {
            btnMusicPlayFavorite!!.setImageResource(R.drawable.ic_favorite_unchecked)
        }
        curMusic?.let {
            it.favorite = favorite
            mediaManager!!.setCurMusic(it)
            saveFavorite(it, favorite)
        }
    }

    private fun saveFavorite(music: Music, favorite: Int) {
        music.favorite = favorite
        musicDao.update(WhereBuilder.create().addWhereEqualTo(OrmTable.INDEX_ID, music.id), music)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                   fromUser: Boolean) {
        if (seekBar === sbMusicPlayPlayback) {
            if (!playAuto) {
                seekBarProgress = progress
            }
        } else if (seekBar === sbMusicPlayVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (seekBar === sbMusicPlayPlayback) {
            playAuto = false
            musicTimer!!.stopTimer()
            mediaManager.pause()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (seekBar === sbMusicPlayPlayback) {
            playAuto = true
            mediaManager.seekTo(seekBarProgress)
            refreshSeekProgress(mediaManager.position(), mediaManager.duration())
            mediaManager.replay()
            musicTimer!!.startTimer()
        }
    }

    override fun onDrawerOpened() {
        if (lrcListView != null) {
            lrcListView!!.visibility = View.INVISIBLE
        }
        if (rvHomeModule != null) {
            rvHomeModule!!.visibility = View.INVISIBLE
        }
        playModeControl.refreshButtonStatus(btnMusicPlayMode!!)
        lyricLoader.searchLrc(curMusic)
    }

    override fun onDrawerClosed() {
        if (lrcListView != null) {
            lrcListView!!.visibility = View.VISIBLE
        }
        if (rvHomeModule != null) {
            rvHomeModule!!.visibility = View.VISIBLE
        }
        slidingView!!.visibility = View.GONE
    }

    /**
     * 刷新播放进度。
     */
    fun refreshSeekProgress(curTime: Int, totalTime: Int) {
        var curTime = curTime
        var totalTime = totalTime
        val tempCurTime = curTime
        curTime /= 1000
        totalTime /= 1000
        val curMinute = curTime / 60
        val curSecond = curTime % 60

        val curTimeString = String.format("%02d:%02d", curMinute, curSecond)
        tvMusicPlayCurTime!!.text = curTimeString

        var rate = 0
        if (totalTime != 0) {
            rate = (curTime.toFloat() / totalTime * 100).toInt()
        }
        sbMusicPlayPlayback!!.progress = rate

        lyricScroller.notifyTime(tempCurTime.toLong())
    }

    /**
     * 刷新歌曲播放界面。
     */
    fun refreshUI(curTime: Int, totalTime: Int, music: Music) {

        var totalTime = totalTime

        curMusic = music
        refreshFavorite(music.favorite)
        val tempTotalTime = totalTime

        totalTime /= 1000
        val totalMinute = totalTime / 60
        val totalSecond = totalTime % 60
        val totalTimeString = String.format("%02d:%02d", totalMinute,
                totalSecond)

        tvMusicPlayTotalTime!!.text = totalTimeString

        tvSlidingMusicName!!.text = music.musicName
        tvSlidingArtist!!.text = music.artist
        refreshSeekProgress(curTime, tempTotalTime)
    }

//    @SingleClick
    override fun onClick(v: View) {
        when (v.id) {
            // 上一首
            R.id.btn_music_play_prev -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.prev()
            }
            // 播放
            R.id.btn_music_play_play -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.replay()
            }
            // 下一首
            R.id.btn_music_play_next -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.next()
            }
            // 暂停
            R.id.btn_music_play_pause -> mediaManager!!.pause()
            // 音量
            R.id.btn_music_play_volume -> if (llMusicPlayVolume!!.isShown) {
                volumeHandler.removeCallbacks(r)
                llMusicPlayVolume!!.visibility = View.INVISIBLE
                llMusicPlayVolume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_out))
            } else {
                llMusicPlayVolume!!.visibility = View.VISIBLE
                llMusicPlayVolume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_in))
                volumeHandler.postDelayed(r, 3000)
            }
            // 播放模式
            R.id.btn_music_play_mode -> playModeControl.changePlayMode(btnMusicPlayMode!!)
            // 喜爱
            R.id.btn_music_play_favorite -> {
                if (mediaManager!!.curMusic!!.favorite == 0) {
                    refreshFavorite(1)
                } else {
                    refreshFavorite(0)
                }
                // 此处最好只刷新收藏数目
                Apollo.emit(ApolloEvent.REFRESH_LOCAL_NUMS)
            }
        }
    }

    /**
     * 加载歌词。
     */
    fun loadLyric(music: Music) {
        lyricLoader.searchLrc(music)
    }

    /**
     * 加载转盘封面。
     */
    fun loadRotateCover(bitmap: Bitmap) {
        rotateCoverView!!.setImageBitmap(bitmap)
    }

    /**
     * 设置转盘封面的边框。
     */
    private fun setBitmapBorder(canvas: Canvas) {
        val rect = canvas.getClipBounds()
        val paint = Paint()
        // 设置边框颜色
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        // 设置边框宽度
        paint.strokeWidth = 100F
        canvas.drawRect(rect, paint)
    }

    /**
     * 创建默认的转盘封面。
     */
    fun createDefaultCover() : Bitmap {
        val bmp = BitmapFactory.decodeResource(manager.view.context.resources, R.drawable.cover_rotating_bg)
        val width = bmp.width + DensityUtils.DP50
        val height = bmp.height + DensityUtils.DP100
        // 创建一个空的Bitmap(内存区域),宽度等于第一张图片的宽度，高度等于两张图片高度总和
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // 将bitmap放置到绘制区域,并将要拼接的图片绘制到指定内存区域
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(Rect(0, 0, width, height), paint)
        canvas.drawBitmap(bmp, DensityUtils.DP50.toFloat(), DensityUtils.DP50.toFloat(), null)
        //将canvas传递进去并设置其边框
        setBitmapBorder(canvas)
        return bitmap
    }
}
