package site.doramusic.app

import android.content.Intent
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.dorachat.auth.ARouterPath
import com.dorachat.auth.AuthInterceptor
import com.dorachat.auth.AuthService
import com.dorachat.auth.DoraChatConfig
import com.dorachat.auth.DoraChatSDK
import com.dorachat.auth.DoraUserInfo
import com.dorachat.auth.SignInEvent
import com.dorachat.auth.SignOutEvent
import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.http.retrofit.RetrofitManager
import dora.pay.DoraFund
import dora.pay.EVMChains
import dora.util.LogUtils
import dora.util.RxBus
import dora.util.ThreadUtils
import dora.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import site.doramusic.app.chat.ChatService
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.APP_NAME
import site.doramusic.app.conf.AppConfig.Companion.COLUMN_ORDER_ID
import site.doramusic.app.conf.AppConfig.Companion.CONNECT_TIMEOUT
import site.doramusic.app.conf.AppConfig.Companion.DB_NAME
import site.doramusic.app.conf.AppConfig.Companion.DB_VERSION
import site.doramusic.app.conf.AppConfig.Companion.READ_TIMEOUT
import site.doramusic.app.conf.AppConfig.Companion.URL_DOMAIN
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.feedback.FeedbackService
import site.doramusic.app.http.service.AdService
import site.doramusic.app.http.service.FileService
import site.doramusic.app.model.Donation
import site.doramusic.app.model.DownloadTask
import site.doramusic.app.sysmsg.SysMsgService
import site.doramusic.app.upgrade.ApkService
import site.doramusic.app.util.ThemeSelector
import java.util.concurrent.TimeUnit

/**
 * 朵拉音乐APP。
 */
class MusicApp : BaseApplication(), AppConfig {

    companion object {

        lateinit var app: MusicApp
        var isAppInitialized = false
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        init()
        // 懒加载
        ThreadUtils.lazyLoad {
            // 耗时操作延迟加载，不影响启动速度，代价是调用之前要先检测是否初始化完成
            val startTime = System.currentTimeMillis()
            LogUtils.d("initPay start time:$startTime")
            initPay()
            val endTime = System.currentTimeMillis()
            LogUtils.d("initPay end time:$endTime,cost ${(endTime - startTime) / 1000.0}s")
            isAppInitialized = true
        }
    }

    private fun handleSignIn() {
        ARouter.getInstance().build(ARoutePath.ACTIVITY_MAIN)
            .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    private fun handleSignOut() {
        ARouter.getInstance().build(ARouterPath.ACTIVITY_SIGN_IN)
            .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    private fun init() {
        val startTime = System.currentTimeMillis()
        LogUtils.d("init start time:$startTime")
        initDb()    // 初始化SQLite数据库的表
        initAuth()  // 初始化Dora Chat认证SDK
        initHttp()   // 初始化网络框架
        RxBus.getInstance()
            .toObservable(SignInEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleSignIn()
            }
        RxBus.getInstance()
            .toObservable(SignOutEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleSignOut()
            }
        val endTime = System.currentTimeMillis()
        LogUtils.d("init end time:$endTime,cost ${(endTime - startTime) / 1000.0}s")
    }

    private fun initAuth() {
        val config = DoraChatConfig.Builder(
            apiBaseUrl = AppConfig.URL_AUTH_SERVER,
            partitionId = "doramusic",
            appName = "Dora Music",
            themeColor = ContextCompat.getColor(this, R.color.colorPrimary)
        )
            .enableLog(true)
            .autoRefreshToken(true)
            .build()
        DoraChatSDK.init(this, config)
    }

    private fun initPay() {
        val skinThemeColor = ThemeSelector.getThemeColor(applicationContext)
        DoraFund.init(this, APP_NAME,
            getString(R.string.app_desc), URL_DOMAIN,
            arrayOf(EVMChains.POLYGON), skinThemeColor,
            object : DoraFund.PayListener {
                override fun onPayFailure(orderId: String, msg: String) {
                }

                override fun onSendTransactionToBlockchain(
                    orderId: String,
                    transactionHash: String
                ) {
                    // 交易完成签名并广播给区块链即认为已捐赠，实际还要等区块确认，此时如果手动和被动取消都
                    // 不会转出代币。手动取消，发送一笔0数量的转账给自己地址。被动取消，设置极低gas永远都不
                    // 可能被确认，然后卸载钱包软件。
                    val donation = DaoFactory.getDao(Donation::class.java).selectOne(
                        WhereBuilder.create().addWhereEqualTo(COLUMN_ORDER_ID, orderId)
                    )
                    if (donation != null) {
                        donation.transactionHash = transactionHash
                        donation.pending = true
                        DaoFactory.getDao(Donation::class.java).updateAsync(donation)
                        ToastUtils.showLong(getString(R.string.donate_successfully, transactionHash))
                    }
                }
            })
        DoraFund.createNotificationChannels(this)
    }

    private fun initHttp() {
        RetrofitManager.initConfig {
            okhttp {
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                addInterceptor(AuthInterceptor())
                build()
            }
            // 支持Observable请求
            rxJava(true)
            // 支持Flow请求
            flow(true)
            // 认证
            mappingBaseUrl(AuthService::class.java, AppConfig.URL_AUTH_SERVER)
            // 聊天室
            mappingBaseUrl(ChatService::class.java, AppConfig.URL_CHAT_SERVER)
            // 建议反馈
            mappingBaseUrl(FeedbackService::class.java, AppConfig.URL_FEEDBACK_SERVER)
            // 官方产品簇
            mappingBaseUrl(AdService::class.java, AppConfig.URL_AD_SERVER)
            // 应用更新
            mappingBaseUrl(ApkService::class.java, AppConfig.URL_APK_SERVER)
            // 系统消息
            mappingBaseUrl(SysMsgService::class.java, AppConfig.URL_SYS_MSG_SERVER)
            // 文件
            mappingBaseUrl(FileService::class.java, AppConfig.URL_FILE_SERVER)
        }
    }

    private fun initDb() {
        Orm.init(this, OrmConfig.Builder()
            .database(DB_NAME)      // 自定义数据库名称
            .version(DB_VERSION)    // 从1开始递增
            // 所管理的表
            .tables(Music::class.java, Artist::class.java,
                Album::class.java, Folder::class.java,
                Donation::class.java, DownloadTask::class.java,
                DoraUserInfo::class.java
            )
            .build())
    }
}
