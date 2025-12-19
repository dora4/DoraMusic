package site.doramusic.app

import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.http.retrofit.RetrofitManager
import dora.skin.SkinManager
import dora.pay.DoraFund
import dora.pay.EVMChains
import dora.util.LogUtils
import dora.util.ThreadUtils
import dora.util.ToastUtils
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.APP_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_ORDER_ID
import site.doramusic.app.base.conf.AppConfig.Companion.CONNECT_TIMEOUT
import site.doramusic.app.base.conf.AppConfig.Companion.DB_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.DB_VERSION
import site.doramusic.app.base.conf.AppConfig.Companion.READ_TIMEOUT
import site.doramusic.app.base.conf.AppConfig.Companion.URL_DOMAIN
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.http.service.AdService
import site.doramusic.app.http.service.FileService
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.model.Donation
import site.doramusic.app.model.DownloadTask
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

    private fun init() {
        val startTime = System.currentTimeMillis()
        LogUtils.d("init start time:$startTime")
        initDb()    // 初始化SQLite数据库的表
        initHttp()   // 初始化网络框架
        val endTime = System.currentTimeMillis()
        LogUtils.d("init end time:$endTime,cost ${(endTime - startTime) / 1000.0}s")
    }

    private fun initPay() {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
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
                // dcache高版本自动添加FormatLogInterceptor
//                interceptors().add(FormatLogInterceptor())
                build()
            }
            // 这里可以指定不同节点的API服务
            mappingBaseUrl(FileService::class.java, AppConfig.URL_FILE_SERVER)
            mappingBaseUrl(AdService::class.java, AppConfig.URL_AD_SERVER)
        }
    }

    private fun initDb() {
        Orm.init(this, OrmConfig.Builder()
            .database(DB_NAME)      // 自定义数据库名称
            .version(DB_VERSION)    // 从1开始递增
            // 所管理的表
            .tables(Music::class.java, Artist::class.java,
                Album::class.java, Folder::class.java,
                Donation::class.java, DownloadTask::class.java
            )
            .build())
    }
}
