package site.doramusic.app

import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.http.retrofit.RetrofitManager
import dora.trade.DoraTrade
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import dora.util.ToastUtils
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.http.service.AdService
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.media.MediaManager

/**
 * 朵拉音乐APP。
 */
class MusicApp : BaseApplication(), AppConfig {

    companion object {

        /**
         * 全局Application单例。
         */
        lateinit var app: MusicApp
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        MediaManager.cancelNotification()
    }

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
            Web3ModalChainsPresets.ethChains["137"]!!
        )
        DoraTrade.init(this, "Dora Music",
            getString(R.string.app_desc), "http://doramusic.site", chains, object : DoraTrade.PayListener {
                override fun onPayFailure(orderId: String, transactionHash: String) {
                }

                override fun onSendTransactionToBlockchain(
                    orderId: String,
                    transactionHash: String
                ) {
                    ToastUtils.showShort(getString(R.string.donate_successfully, transactionHash))
                }
            })
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
                Album::class.java, Folder::class.java)
            .build())
    }
}
