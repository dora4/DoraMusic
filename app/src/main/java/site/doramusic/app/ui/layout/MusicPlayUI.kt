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
import com.lwh.jackknife.av.util.MusicTimer
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.util.DensityUtils
import dora.widget.DoraRotateCoverView
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.annotation.SingleClick
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
import site.doramusic.app.widget.SlidingView

/**
 * 歌词滚动界面。
 */
class MusicPlayUI(drawer: ILyricDrawer, manager: UIManager) : UIFactory(drawer, manager),
        View.OnClickListener, AppConfig, SeekBar.OnSeekBarChangeListener,
        SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener {

    var handler: Handler
    private val curVolume: Int
    private val maxVolume: Int
    private val audioManager: AudioManager
    private val mediaManager: MediaManager?
    private val contentView: View
    private var btn_music_play_mode: ImageButton? = null
    private var ll_music_play_volume: LinearLayout? = null
    private var sb_music_play_playback: SeekBar? = null
    private var sb_music_play_volume: SeekBar? = null
    private var iv_sliding_favorite_flying: ImageView? = null
    private var tv_sliding_music_name: TextView? = null
    private var tv_sliding_artist: TextView? = null
    private var tv_music_play_total_time: TextView? = null
    private var tv_music_play_cur_time: TextView? = null
    private var btn_music_play_prev: ImageButton? = null
    private var btn_music_play_next: ImageButton? = null
    private var btn_music_play_play: ImageButton? = null
    private var btn_music_play_pause: ImageButton? = null
    private var btn_music_play_volume: ImageButton? = null
    private var btn_music_play_favorite: ImageButton? = null
    private var statusbar_lyric: View? = null
    private var sv_home_drawer: SlidingView? = null
    private var curMusic: Music? = null
    private var vp_music_play_cover_lyric: ViewPager? = null
    private var coverLrcContainer: FrameLayout? = null
    private var rotateCoverView: DoraRotateCoverView? = null
    private var lrcEmptyView: TextView? = null
    private var lrcListView: ListView? = null
    private lateinit var lyricAdapter: LyricAdapter
    private var musicTimer: MusicTimer? = null
    private var playAuto: Boolean = false
    private var seekBarProgress: Int = 0
    private var rv_home_module: RecyclerView? = null
    private val musicDao: OrmDao<Music>
    private val lyricScroller: LyricScroller
    private val volumeHandler: Handler
    private val lyricLoader: LyricLoader
    private val playModeControl: PlayModeControl

    val isOpened: Boolean
        get() = sv_home_drawer!!.isOpened

    internal var r: Runnable = Runnable {
        ll_music_play_volume!!.visibility = View.INVISIBLE
        ll_music_play_volume!!.startAnimation(AnimationUtils.loadAnimation(this.manager.view.context, R.anim.anim_fade_out))
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
        this.mediaManager = MusicApp.instance!!.mediaManager!!
        this.contentView = manager.view
        this.lyricAdapter = LyricAdapter(manager.view.context)
        this.lyricScroller = LyricScroller()
        this.musicDao = DaoFactory.getDao(Music::class.java)
        this.playModeControl = PlayModeControl(manager.view.context)
        volumeHandler = Handler()
        audioManager = manager.view.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        initViews()
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                refreshSeekProgress(mediaManager!!.position(),
                        mediaManager.duration())
            }
        }
        lyricLoader = DoraLyricLoader(lyricScroller, lyricListener)
    }

    private fun initViews() {
        rv_home_module = findViewById(R.id.rv_home_module) as RecyclerView
        sv_home_drawer = findViewById(R.id.sv_home_drawer) as SlidingView
        statusbar_lyric = findViewById(R.id.statusbar_lyric)
        tv_sliding_music_name = findViewById(R.id.tv_sliding_music_name) as TextView
        tv_sliding_artist = findViewById(R.id.tv_sliding_artist) as TextView
        btn_music_play_prev = findViewById(R.id.btn_music_play_prev) as ImageButton
        btn_music_play_next = findViewById(R.id.btn_music_play_next) as ImageButton
        btn_music_play_play = findViewById(R.id.btn_music_play_play) as ImageButton
        btn_music_play_pause = findViewById(R.id.btn_music_play_pause) as ImageButton
        btn_music_play_volume = findViewById(R.id.btn_music_play_volume) as ImageButton
        btn_music_play_mode = findViewById(R.id.btn_music_play_mode) as ImageButton
        btn_music_play_favorite = findViewById(R.id.btn_music_play_favorite) as ImageButton
        iv_sliding_favorite_flying = findViewById(R.id.iv_sliding_favorite_flying) as ImageView
        statusbar_lyric!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            getStatusBarHeight())
        rotateCoverView = DoraRotateCoverView(manager.view.context)
        rotateCoverView!!.scaleType = ImageView.ScaleType.CENTER_CROP
        rotateCoverView!!.scaleX = 0.8f
        rotateCoverView!!.scaleY = 0.8f
        vp_music_play_cover_lyric = findViewById(R.id.vp_music_play_cover_lyric) as ViewPager

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
        coverLrcContainer!!.addView(lrcListView)
        coverLrcContainer!!.addView(lrcEmptyView)
        var pageViews: MutableList<View>  = ArrayList()
        pageViews.add(rotateCoverView!!)
        pageViews.add(coverLrcContainer!!)
        vp_music_play_cover_lyric!!.adapter = MusicPlayPagerAdapter(pageViews)
        sv_home_drawer!!.setOnDrawerCloseListener(this)
        sv_home_drawer!!.setOnDrawerOpenListener(this)

        btn_music_play_prev!!.setOnClickListener(this)
        btn_music_play_next!!.setOnClickListener(this)
        btn_music_play_play!!.setOnClickListener(this)
        btn_music_play_pause!!.setOnClickListener(this)
        btn_music_play_volume!!.setOnClickListener(this)
        btn_music_play_mode!!.setOnClickListener(this)
        btn_music_play_favorite!!.setOnClickListener(this)
        sb_music_play_playback = findViewById(R.id.sb_music_play_playback) as SeekBar
        sb_music_play_volume = findViewById(R.id.sb_music_play_volume) as SeekBar
        sb_music_play_volume!!.max = maxVolume
        sb_music_play_volume!!.progress = curVolume
        sb_music_play_playback!!.setOnSeekBarChangeListener(this)
        sb_music_play_volume!!.setOnSeekBarChangeListener(this)
        tv_music_play_cur_time = findViewById(R.id.tv_music_play_cur_time) as TextView
        tv_music_play_total_time = findViewById(R.id.tv_music_play_total_time) as TextView

        ll_music_play_volume = findViewById(R.id.ll_music_play_volume) as LinearLayout
        sb_music_play_volume!!.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> volumeHandler.removeCallbacks(r)
                MotionEvent.ACTION_UP -> {
                    ll_music_play_volume!!.visibility = View.INVISIBLE
                    ll_music_play_volume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_out))
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
        sv_home_drawer!!.visibility = View.VISIBLE
        sv_home_drawer!!.animateOpen()
    }

    fun close() {
        sv_home_drawer!!.animateClose()
    }

    fun showPlay(flag: Boolean) {
        if (flag) {
            btn_music_play_play!!.visibility = View.VISIBLE
            btn_music_play_pause!!.visibility = View.GONE
            rotateCoverView!!.pause()
        } else {
            btn_music_play_play!!.visibility = View.GONE
            btn_music_play_pause!!.visibility = View.VISIBLE
            rotateCoverView!!.start(R.drawable.default_cover_rotate)
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
            btn_music_play_favorite!!.setImageResource(R.drawable.ic_favorite_checked)
        } else {
            btn_music_play_favorite!!.setImageResource(R.drawable.ic_favorite_unchecked)
        }
        curMusic!!.favorite = favorite
        mediaManager!!.setCurMusic(curMusic!!)
        saveFavorite(curMusic!!, favorite)
    }

    private fun saveFavorite(music: Music, favorite: Int) {
        music.favorite = favorite
        musicDao.update(WhereBuilder.create().addWhereEqualTo("_id", music.id), music)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                   fromUser: Boolean) {
        if (seekBar === sb_music_play_playback) {
            if (!playAuto) {
                seekBarProgress = progress
            }
        } else if (seekBar === sb_music_play_volume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (seekBar === sb_music_play_playback) {
            playAuto = false
            musicTimer!!.stopTimer()
            mediaManager!!.pause()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (seekBar === sb_music_play_playback) {
            playAuto = true
            mediaManager!!.seekTo(seekBarProgress)
            refreshSeekProgress(mediaManager.position(), mediaManager.duration())
            mediaManager.replay()
            musicTimer!!.startTimer()
        }
    }

    override fun onDrawerOpened() {
        if (lrcListView != null) {
            lrcListView!!.visibility = View.INVISIBLE
        }
        if (rv_home_module != null) {
            rv_home_module!!.visibility = View.INVISIBLE
        }
        playModeControl.refreshButtonStatus(btn_music_play_mode!!)
        lyricLoader.searchLrc(curMusic)
    }

    override fun onDrawerClosed() {
        if (lrcListView != null) {
            lrcListView!!.visibility = View.VISIBLE
        }
        if (rv_home_module != null) {
            rv_home_module!!.visibility = View.VISIBLE
        }
        sv_home_drawer!!.visibility = View.GONE
        stopAnimation(iv_sliding_favorite_flying!!)
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
        tv_music_play_cur_time!!.text = curTimeString

        var rate = 0
        if (totalTime != 0) {
            rate = (curTime.toFloat() / totalTime * 100).toInt()
        }
        sb_music_play_playback!!.progress = rate

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

        tv_music_play_total_time!!.text = totalTimeString

        tv_sliding_music_name!!.text = music.musicName
        tv_sliding_artist!!.text = music.artist
        refreshSeekProgress(curTime, tempTotalTime)
    }

    @SingleClick
    override fun onClick(v: View) {
        when (v.id) {
            //上一首
            R.id.btn_music_play_prev -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.prev()
            }
            //播放
            R.id.btn_music_play_play -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.replay()
            }
            //下一首
            R.id.btn_music_play_next -> {
                if (curMusic == null) {
                    return
                }
                mediaManager!!.next()
            }
            //暂停
            R.id.btn_music_play_pause -> mediaManager!!.pause()
            //音量
            R.id.btn_music_play_volume -> if (ll_music_play_volume!!.isShown) {
                volumeHandler.removeCallbacks(r)
                ll_music_play_volume!!.visibility = View.INVISIBLE
                ll_music_play_volume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_out))
            } else {
                ll_music_play_volume!!.visibility = View.VISIBLE
                ll_music_play_volume!!.startAnimation(AnimationUtils.loadAnimation(manager.view.context, R.anim.anim_fade_in))
                volumeHandler.postDelayed(r, 3000)
            }
            //播放模式
            R.id.btn_music_play_mode -> playModeControl.changePlayMode(btn_music_play_mode!!)
            //喜爱
            R.id.btn_music_play_favorite -> {
                if (mediaManager!!.curMusic!!.favorite == 0) {
//                    startAnimation(iv_sliding_favorite_flying!!)
                    refreshFavorite(1)
                } else {
                    refreshFavorite(0)
                }
                //此处最好只刷新收藏数目
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
        rotateCoverView!!.setImageBitmap(createDefaultCover())
    }

    /**
     * 设置转盘封面的边框。
     */
    private fun setBitmapBorder(canvas: Canvas) {
        var rect = canvas.getClipBounds()
        var paint = Paint()
        //设置边框颜色
        paint.setColor(Color.WHITE)
        paint.setStyle(Paint.Style.STROKE)
        //设置边框宽度
        paint.setStrokeWidth(100F)
        canvas.drawRect(rect, paint)
    }

    /**
     * 创建默认的转盘封面。
     */
    fun createDefaultCover() : Bitmap {
        var bmp = BitmapFactory.decodeResource(manager.view.context.resources, R.drawable.default_cover_rotate)
        var dp50 = DensityUtils.dp2px(manager.view.context, 50f).toFloat()
        var dp100 = DensityUtils.dp2px(manager.view.context, 100f)
        var width = bmp.width + dp100
        var height = bmp.height + dp100
        //创建一个空的Bitmap(内存区域),宽度等于第一张图片的宽度，高度等于两张图片高度总和
        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //将bitmap放置到绘制区域,并将要拼接的图片绘制到指定内存区域
        var canvas = Canvas(bitmap)
        var paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(Rect(0, 0, width, height), paint)
        canvas.drawBitmap(bmp, dp50, dp50, null)
        //将canvas传递进去并设置其边框
//        setBitmapBorder(canvas)
        return bitmap
    }
}
