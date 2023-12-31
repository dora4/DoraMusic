package site.doramusic.app.ui.activity

import android.bluetooth.BluetoothHeadset
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lsxiao.apollo.core.Apollo
import dora.arouter.open
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.http.DoraHttp.net
import dora.http.DoraHttp.request
import dora.skin.SkinManager
import dora.util.*
import dora.widget.DoraAlertDialog
import dora.widget.DoraLoadingDialog
import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.callback.OnBackListener
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.ApolloEvent
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.databinding.ActivityMainBinding
import site.doramusic.app.db.Music
import site.doramusic.app.media.MusicScanner
import site.doramusic.app.receiver.EarphoneReceiver
import site.doramusic.app.ui.IBack
import site.doramusic.app.ui.fragment.HomeFragment
import site.doramusic.app.util.PreferencesManager
import java.util.concurrent.Executors

@Route(path = ARoutePath.ACTIVITY_MAIN)
class MainActivity : BaseSkinActivity<ActivityMainBinding>(), IBack, AppConfig {

    private var lastTime: Long = 0
    private var homeFragment: HomeFragment? = null
    private val backListeners: MutableList<OnBackListener> = ArrayList()
    private var earphoneReceiver: EarphoneReceiver? = null
    private var prefsManager: PreferencesManager? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    /**
     * 打开侧边栏。
     */
    fun openDrawer() {
        mBinding.dlMain.openDrawer(GravityCompat.START)
        val headerView = mBinding.nvMain.getHeaderView(0)
        val nicknameView = headerView.findViewById<TextView>(R.id.tv_drawer_header_nickname)
        val dobView = headerView.findViewById<TextView>(R.id.tv_drawer_header_dob)
        val scoreView = headerView.findViewById<TextView>(R.id.tv_drawer_header_score)
        val header = headerView.findViewById<LinearLayout>(R.id.ll_drawer_header)
//        val doraUser = UserManager.currentUser
//        if (doraUser != null) {
//            nicknameView.text = doraUser.username
//            dobView.text = "朵币:${doraUser.dob}"
//            scoreView.text = "积分:${doraUser.score}"
//            header.visibility = View.VISIBLE
//        } else {
//            nicknameView.text = ""
//            dobView.text = ""
//            scoreView.text = ""
//            header.visibility = View.INVISIBLE
//        }
    }

    /**
     * 关闭侧边栏。
     */
    fun closeDrawer() {
        if (mBinding.dlMain.isDrawerOpen(GravityCompat.START)) {
            mBinding.dlMain.closeDrawer(GravityCompat.START)
        }
    }


    /**
     * 请求通知栏权限。
     */
    private fun requestNotificationPermission() {
        XXPermissions.with(this)
            .permission(Permission.NOTIFICATION_SERVICE)
            .request(null)
    }

    /**
     * 注册耳机插拔监听。
     */
    private fun registerEarCupReceiver() {
        earphoneReceiver = EarphoneReceiver()
        val filter = IntentFilter()
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(earphoneReceiver, filter)
    }

    /**
     * 应用皮肤。
     */
    private fun applySkin() {
        val manager = PreferencesManager(this)
        when (manager.getSkinType()) {
            0 -> {
            }
            1 -> {
                SkinManager.changeSkin("cyan")
            }
            2 -> {
                SkinManager.changeSkin("orange")
            }
            3 -> {
                SkinManager.changeSkin("black")
            }
            4 -> {
                SkinManager.changeSkin("green")
            }
            5 -> {
                SkinManager.changeSkin("red")
            }
            6 -> {
                SkinManager.changeSkin("blue")
            }
            7 -> {
                SkinManager.changeSkin("purple")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        closeDrawer()
    }

    private fun initMenu() {
        // 禁用手势滑动打开
        mBinding.dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val headerView = mBinding.nvMain.getHeaderView(0)
        val userAvatarView = headerView.findViewById<ImageView>(R.id.iv_drawer_header_avatar)
        val nicknameView = headerView.findViewById<TextView>(R.id.tv_drawer_header_nickname)
        val signView = headerView.findViewById<TextView>(R.id.tv_drawer_header_sign)
        val versionNameView = headerView.findViewById<TextView>(R.id.tv_drawer_header_version_name)
        val header = headerView.findViewById<LinearLayout>(R.id.ll_drawer_header)
        val dobView = headerView.findViewById<TextView>(R.id.tv_drawer_header_dob)
        val scoreView = header.findViewById<TextView>(R.id.tv_drawer_header_score)
        userAvatarView.setOnClickListener {
//            if (UserManager.currentUser == null) {
//                open(ARoutePath.ACTIVITY_LOGIN)
//            }
        }
        versionNameView.text = "客户端版本:" + getString(R.string.app_version)
        signView.setOnClickListener {
//            val doraUser = UserManager.currentUser
//            if (doraUser != null) {
//                val userId = doraUser.id
//                if (userId != null) {
//                    //用户签到
//                    val service = RetrofitManager.getService(UserService::class.java)
//                    val call = service.sign(userId)
//                    call.enqueue(object : DoraCallback<DoraSign>() {
//                        override fun onSuccess(sign: DoraSign) {
//                            doraUser.score = sign.score
//                            doraUser.dob = sign.dob
//                            UserManager.update(doraUser)
//                            dobView.text = "朵币:${sign.dob}"
//                            scoreView.text = "积分:${sign.score}"
//                            showShortToast("签到成功，连续签到${sign.signNum}天")
//                        }
//
//                        override fun onFailure(code: Int, msg: String) {
//                            showShortToast("签到失败，$msg")
//                        }
//                    })
//                }
//            }
        }
        mBinding.nvMain.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // 扫描歌曲
                R.id.menu_scan_music -> performScanMusic()
                // 更换换肤
                R.id.menu_change_skin -> open(ARoutePath.ACTIVITY_CHOICE_COLOR)
                // 均衡器
                R.id.menu_equalizer -> open(ARoutePath.ACTIVITY_EQUALIZER)
                // 设置
                R.id.menu_settings -> open(ARoutePath.ACTIVITY_SETTINGS)
            }
            true
        }
    }

    /**
     * 扫描歌曲。
     */
    private fun performScanMusic() {
        mBinding.dlMain.closeDrawer(GravityCompat.START)
        val builder = QueryBuilder.create().where(
            WhereBuilder.create()
                .addWhereEqualTo(Music.COLUMN_FAVORITE, 1))
        val favoriteCount = DaoFactory.getDao(Music::class.java).count(builder)
        if (favoriteCount > 0) { //有收藏的歌曲
            DoraAlertDialog(this).show("您有收藏的歌曲，扫描将会强制清空收藏的歌曲，是否继续？") {
                themeColorResId(R.color.colorPrimary)
                positiveListener { scanMusic() }
            }
        } else {
            scanMusic()
        }
    }

    /**
     * 扫描歌曲。
     */
    fun scanMusic() {
        net {
            val dialog = DoraLoadingDialog(this).show("正在扫描...") {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            request {
                Executors.newCachedThreadPool().submit {
                    try {
                        val playlist = MusicScanner.scan(this@MainActivity) as MutableList<Music>
                        MusicApp.instance!!.mediaManager!!.refreshPlaylist(playlist)
                    } finally {
                        it.releaseLock(null)
                    }
                }
            }
            homeFragment!!.onRefreshLocalMusic()
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (earphoneReceiver != null) {
            unregisterReceiver(earphoneReceiver)
        }
    }

    override fun onBackPressed() {
        if (homeFragment != null) {
            if (homeFragment!!.isHome) {
                if (mBinding.dlMain.isDrawerOpen(GravityCompat.START)) {
                    mBinding.dlMain.closeDrawer(GravityCompat.START)
                } else {
                    val currTime = System.currentTimeMillis()
                    if (currTime - lastTime > 2000) {
                        showShortToast("再按一次返回到桌面")
                        lastTime = currTime
                    } else {
                        moveTaskToBack(false)
                    }
                }
            } else {
                if (homeFragment!!.isSlidingDrawerOpened) {
                    homeFragment!!.closeSlidingDrawer()
                } else {
                    if (backListeners.size > 0) {
                        for (listener in backListeners) {
                            listener.onBack()
                        }
                    }
                    //这种方式返回首页也要刷新，另一种刷新是在UIManager#setCurrentItem()
                    if (homeFragment!!.isHome) {
                        Apollo.emit(ApolloEvent.REFRESH_LOCAL_NUMS)
                    }
                }
            }
        } else {
            showShortToast("后台回收内存")
        }
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityMainBinding) {
        StatusBarUtils.setStatusBarWithDrawerLayout(this, mBinding.dlMain, ContextCompat.getColor(this, R.color.colorPrimary), 255)
        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fl_main, homeFragment!!).commit()
        prefsManager = PreferencesManager(this)
        initMenu()
        if (savedInstanceState != null) {
            LogUtils.i("后台启动")
        } else {
            //提供皮肤
            applySkin()
            //注册耳机监听器
            registerEarCupReceiver()
            //请求通知栏权限
//            requestNotificationPermission()
//            if (prefsManager!!.getHotFix()) {
//                //拉取热修复补丁
//                fetchPatch()
//            }
        }
    }

    override fun registerBackListener(listener: OnBackListener) {
        if (!backListeners.contains(listener)) {
            backListeners.add(listener)
        }
    }

    override fun unregisterBackListener(listener: OnBackListener) {
        if (!backListeners.contains(listener)) {
            backListeners.add(listener)
        }
    }
}