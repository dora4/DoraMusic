package site.doramusic.app

import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.http.retrofit.RetrofitManager
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.http.service.AdService
import site.doramusic.app.http.service.MusicService

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

    override fun onCreate() {
        super.onCreate()
        app = this
        init()
    }

    private fun init() {
        initHttp()   // 初始化网络框架
        initDb()    // 初始化SQLite数据库的表
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
