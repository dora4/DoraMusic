package site.doramusic.app

import android.os.Build
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
import site.doramusic.app.base.conf.AppConfig.Companion.DB_NAME
import site.doramusic.app.base.conf.AppConfig.Companion.DB_VERSION
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.http.service.AdService
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.model.Donation
import site.doramusic.app.model.DownloadTask

/**
 * 朵拉音乐APP。
 */
class MusicApp : BaseApplication(), AppConfig {

    companion object {

        lateinit var app: MusicApp
        var isAppInitialized: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        ThreadUtils.lazyLoad {
            if (!isAppInitialized) {
                init()
                isAppInitialized = true
            }
            true
        }
    }

    private fun init() {
        val startTime = System.currentTimeMillis()
        LogUtils.d("init start time:$startTime")
        initDb()    // 初始化SQLite数据库的表
        initPay()   // 初始化支付SDK
        initHttp()   // 初始化网络框架
        val endTime = System.currentTimeMillis()
        LogUtils.d("init end time:$endTime,cost ${(endTime - startTime) / 1000.0}s")
    }

    private fun initPay() {
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        DoraFund.init(this, APP_NAME,
            getString(R.string.app_desc), "http://doramusic.site",
            arrayOf(EVMChains.POLYGON), skinThemeColor,
            object : DoraFund.PayListener {
                override fun onPayFailure(orderId: String, msg: String) {
                }

                override fun onSendTransactionToBlockchain(
                    orderId: String,
                    transactionHash: String
                ) {
                    val donation = DaoFactory.getDao(Donation::class.java).selectOne(
                        WhereBuilder.create().addWhereEqualTo(COLUMN_ORDER_ID, orderId)
                    )
                    if (donation != null) {
                        donation.transactionHash = transactionHash
                        donation.pending = true
                        DaoFactory.getDao(Donation::class.java).update(donation)
                        // 相信粉丝不会取消
                        ToastUtils.showLong(getString(R.string.donate_successfully, transactionHash))
                    }
                }
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DoraFund.createNotificationChannels(this)
        }
    }

    private fun initHttp() {
        RetrofitManager.initConfig {
            okhttp {
                // 高版本自动添加FormatLogInterceptor
//                interceptors().add(FormatLogInterceptor())
                build()
            }
            // 这里可以指定不同节点的API服务
            mappingBaseUrl(MusicService::class.java, AppConfig.URL_APP_SERVER)
            mappingBaseUrl(AdService::class.java, AppConfig.URL_AD_SERVER)
        }
    }

    private fun initDb() {
        Orm.init(this, OrmConfig.Builder()
            .database(DB_NAME)
            .version(DB_VERSION)
            .tables(Music::class.java, Artist::class.java,
                Album::class.java, Folder::class.java,
                Donation::class.java, DownloadTask::class.java
            )
            .build())
    }
}
