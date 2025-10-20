package site.doramusic.app.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.walletconnect.web3.modal.client.Web3Modal
import dora.arouter.open
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.http.retrofit.RetrofitManager
import dora.skin.SkinManager
import dora.pay.DoraFund
import dora.util.IntentUtils
import dora.util.NetUtils
import dora.util.PermissionHelper
import dora.util.RxBus
import dora.util.StatusBarUtils
import dora.util.ToastUtils
import dora.widget.DoraAlertDialog
import dora.widget.DoraLoadingDialog
import site.doramusic.app.BuildConfig
import site.doramusic.app.R
import site.doramusic.app.base.callback.OnBackListener
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.DORA_FUND_ACCESS_KEY
import site.doramusic.app.base.conf.AppConfig.Companion.DORA_FUND_SECRET_KEY
import site.doramusic.app.databinding.ActivityMainBinding
import site.doramusic.app.db.Music
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.media.MusicScanner
import site.doramusic.app.ui.IBackNavigator
import site.doramusic.app.ui.fragment.HomeFragment
import site.doramusic.app.ui.layout.IMenuDrawer
import site.doramusic.app.util.IPFSUtils
import site.doramusic.app.util.PrefsManager
import java.io.File

/**
 * 主界面。
 */
@Route(path = ARoutePath.ACTIVITY_MAIN)
class MainActivity : BaseSkinActivity<ActivityMainBinding>(), IMenuDrawer, IBackNavigator, AppConfig {

    private var lastTime: Long = 0
    private lateinit var homeFragment: HomeFragment
    private val backListeners: MutableList<OnBackListener> = ArrayList()
    private lateinit var prefsManager: PrefsManager
    private var addressView: TextView? = null
    private lateinit var helper: PermissionHelper

    private val selectMusicLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                // 复制文件到缓存目录
                val file = copyUriToFile(uri, "selected_music")
                uploadMusic(file)
            }
        }

    /**
     * 将 content:// Uri 转为 File
     */
    private fun copyUriToFile(uri: Uri, fileName: String): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val outFile = File(cacheDir, fileName)
        inputStream.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }

    /**
     * 触发选择文件
     */
    private fun selectMusicFile() {
        selectMusicLauncher.launch(arrayOf(
            "audio/mpeg",   // mp3
            "audio/flac"    // flac
        ))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    companion object {
        const val REQUEST_VPN_PERMISSION = 1
        const val REQUEST_WALLET_AUTHORIZATION = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VPN_PERMISSION) {
                DoraFund.connectVPN(this, DORA_FUND_ACCESS_KEY,
                    DORA_FUND_SECRET_KEY)
            } else if (requestCode == REQUEST_WALLET_AUTHORIZATION) {
                addressView?.text = DoraFund.getCurrentAddress()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper = PermissionHelper.with(this).prepare(
            PermissionHelper.Permission.READ_MEDIA_AUDIO,
            PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE,
            PermissionHelper.Permission.POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!helper.hasPermission(this, PermissionHelper.Permission.POST_NOTIFICATIONS)) {
                helper.permissions(PermissionHelper.Permission.POST_NOTIFICATIONS).request(null)
            }
        }
        if (prefsManager.getColdLaunchAutoConnectVPN() && NetUtils.checkNetworkAvailable(this)) {
            if (!PermissionHelper.hasStoragePermission(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(IntentUtils.getRequestStoragePermissionIntent(packageName))
                } else {
                    helper.permissions(PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE).request {
                        requestVPNPermission()
                    }
                }
            } else {
                requestVPNPermission()
            }
        }
    }

    private fun requestVPNPermission() {
        val intent = VpnService.prepare(this@MainActivity)
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_PERMISSION)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this@MainActivity.onActivityResult(
                    REQUEST_VPN_PERMISSION,
                    Activity.RESULT_OK,
                    null
                )
            }
        }
    }

    /**
     * 打开侧边栏。
     */
    override fun openDrawer() {
        mBinding.dlMain.openDrawer(GravityCompat.START)
    }

    /**
     * 关闭侧边栏。
     */
    override fun closeDrawer() {
        if (mBinding.dlMain.isDrawerOpen(GravityCompat.START)) {
            mBinding.dlMain.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * 应用皮肤。
     */
    private fun applySkin() {
        val manager = PrefsManager(this)
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
            8 -> {
                SkinManager.changeSkin("yellow")
            }
            9 -> {
                SkinManager.changeSkin("pink")
            }
            10 -> {
                SkinManager.changeSkin("gold")
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
        mBinding.dlMain.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {
                // 滑动中
            }

            override fun onDrawerOpened(drawerView: android.view.View) {
                // 打开状态
                mBinding.dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                // 关闭后，重新锁定不允许滑动打开
                mBinding.dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 状态变化
            }
        })
        val headerView = mBinding.nvMain.getHeaderView(0)
        val avatarView = headerView.findViewById<AppCompatImageView>(R.id.iv_drawer_header_avatar)
        addressView = headerView.findViewById<TextView>(R.id.tv_drawer_header_nickname)
        val versionNameView = headerView.findViewById<TextView>(R.id.tv_drawer_header_version_name)
        versionNameView.text = BuildConfig.APP_VERSION
        if (DoraFund.isWalletConnected()) {
            addressView!!.text = DoraFund.getCurrentAddress()
        }
        avatarView.setOnClickListener {
            // 钱包授权登录
            if (!DoraFund.isWalletConnected()) {
                closeDrawer()
                DoraFund.connectWallet(this, REQUEST_WALLET_AUTHORIZATION)
            } else {
                val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
                DoraAlertDialog(this).show(getString(R.string.are_you_sure_disconnect_wallet)) {
                    themeColor(skinThemeColor)
                    positiveListener {
                        DoraFund.disconnectWallet()
                        addressView?.text = ""
                    }
                }
            }
        }
        mBinding.nvMain.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
//                // 上传歌曲
//                R.id.menu_upload_music -> selectMusicFile()
                // 扫描歌曲
                R.id.menu_scan_music -> performScanMusic()
                // 更换换肤
                R.id.menu_change_skin -> open(ARoutePath.ACTIVITY_COLOR_PICKER)
                // 均衡器
                R.id.menu_equalizer -> open(ARoutePath.ACTIVITY_EQUALIZER)
                // 设置
                R.id.menu_settings -> open(ARoutePath.ACTIVITY_SETTINGS)
            }
            // 不选中
            mBinding.dlMain.closeDrawers()
            true
        }
    }

    /**
     * 上传歌曲。
     */
    private fun uploadMusic(file: File) {
        if (!DoraFund.isWalletConnected()) {
            showShortToast(getString(R.string.evm_login_first))
            return
        }
        val erc20Address = Web3Modal.getAccount()?.address!!
        // 上传歌曲到去中心化存储
        IPFSUtils.uploadToWeb3Storage(file, {
            net {
                val ok = result {
                    RetrofitManager.getService(MusicService::class.java).saveMusicInfo(erc20Address, it)
                }?.result as Boolean
                if (ok) {
                    showShortToast(getString(R.string.uploaded_successfully))
                }
            }
        }, {
            showShortToast(it)
        })
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
            val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
            DoraAlertDialog(this).show(getString(R.string.scan_prompt)) {
                themeColor(skinThemeColor)
                positiveListener { scanMusic() }
            }
        } else {
            scanMusic()
        }
    }

    /**
     * 扫描歌曲。
     */
    @SuppressLint("CheckResult")
    private fun scanMusic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // 通过设置打开
            startActivity(IntentUtils.getRequestStoragePermissionIntent(packageName))
        } else {
            // Android 13 细分文件存储权限
            helper.permissions(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    PermissionHelper.Permission.READ_MEDIA_AUDIO
                else
                    PermissionHelper.Permission.READ_EXTERNAL_STORAGE
            ).request {
                if (it) {
                    val dialog = DoraLoadingDialog(this).show(getString(R.string.scaning)) {
                        setCancelable(false)
                        setCanceledOnTouchOutside(false)
                    }
                    // 扫描音乐，返回列表
                    MusicScanner
                        .scan(this@MainActivity)
                        .doFinally {
                            dialog.dismiss()
                        }
                        .subscribe({ list ->
                            if (list.isNotEmpty()) {
                                ToastUtils.showShort(
                                    String.format(
                                        getString(R.string.music_scan_successfully),
                                        list.size
                                    )
                                )
                            } else {
                                ToastUtils.showShort(getString(R.string.no_songs_scanned))
                            }
                            homeFragment.onRefreshLocalMusic()
                        }, { error ->
                            showShortToast(error.toString())
                        })
                }
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityMainBinding) {
        StatusBarUtils.setStatusBarWithDrawerLayout(this, binding.dlMain,
            ContextCompat.getColor(this, R.color.colorPrimary), 255)
        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fl_main, homeFragment).commit()
        prefsManager = PrefsManager(this)
        initMenu()
        // 应用皮肤
        applySkin()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (homeFragment.isHome) {
                    if (mBinding.dlMain.isDrawerOpen(GravityCompat.START)) {
                        closeDrawer()
                    } else {
                        val currTime = System.currentTimeMillis()
                        if (currTime - lastTime > 2000) {
                            showShortToast(getString(R.string.press_again_to_back))
                            lastTime = currTime
                        } else {
                            moveTaskToBack(false)
                        }
                    }
                } else {
                    if (homeFragment.isSlidingDrawerOpened) {
                        homeFragment.hideDrawer()
                    } else {
                        if (backListeners.size > 0) {
                            for (listener in backListeners) {
                                listener.onBack()
                            }
                        }
                        // 这种方式返回首页也要刷新，另一种刷新是在UIManager#setCurrentItem()
                        if (homeFragment.isHome) {
                            RxBus.getInstance().post(RefreshHomeItemEvent())
                        }
                    }
                }
            }
        })
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