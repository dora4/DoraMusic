package site.doramusic.app

import android.os.Build
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.http.retrofit.RetrofitManager
import dora.skin.SkinManager
import dora.trade.DoraTrade
import dora.util.ToastUtils
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
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
    }

//    override fun onTrimMemory(level: Int) {
//        super.onTrimMemory(level)
//        // 根据不同的内存级别，决定是否取消通知
//        if (level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_RUNNING_CRITICAL) {
//            // 内存较低，清除通知
//            MediaManager.cancelNotification()
//        }
//    }

    override fun onCreate() {
        super.onCreate()
        app = this
        init()
    }

    private fun init() {
        initHttp()   // 初始化网络框架
        initDb()    // 初始化SQLite数据库的表
        initPay()   // 初始化支付SDK
    }

    private fun initPay() {
        val chains: Array<Modal.Model.Chain> = arrayOf(
            Web3ModalChainsPresets.ethChains["137"]!!   // Polygon
        )
        val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
        DoraTrade.init(this, "Dora Music",
            getString(R.string.app_desc), "http://doramusic.site", chains, skinThemeColor,
            object : DoraTrade.PayListener {
                override fun onPayFailure(orderId: String, msg: String) {
                }

                override fun onSendTransactionToBlockchain(
                    orderId: String,
                    transactionHash: String
                ) {
                    val donation = DaoFactory.getDao(Donation::class.java).selectOne(
                        WhereBuilder.create().addWhereEqualTo("order_id", orderId)
                    )
                    if (donation != null) {
                        donation.pending = true
                        donation.transactionHash = transactionHash
                        DaoFactory.getDao(Donation::class.java).update(donation)
                        // 相信粉丝不会取消
                        ToastUtils.showLong(getString(R.string.donate_successfully, transactionHash))
                    }
                }
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DoraTrade.createNotificationChannels(this)
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
            .database(AppConfig.DB_NAME)
            .version(AppConfig.DB_VERSION)
            .tables(Music::class.java, Artist::class.java,
                Album::class.java, Folder::class.java,
                Donation::class.java, DownloadTask::class.java
            )
            .build())
    }
}
