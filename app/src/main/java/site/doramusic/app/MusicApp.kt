package site.doramusic.app

import com.lsxiao.apollo.core.Apollo
import com.lwh.jackknife.xskin.SkinManager
import dora.BaseApplication
import dora.db.Orm
import dora.db.OrmConfig
import dora.http.log.FormatLogInterceptor
import dora.http.retrofit.RetrofitManager
import io.reactivex.android.schedulers.AndroidSchedulers
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.http.service.UpdateService
import site.doramusic.app.http.service.UserService
import site.doramusic.app.media.MediaManager

/**
 * 朵拉音乐APP。
 */
class MusicApp : BaseApplication(), AppConfig {

    /**
     * 全局的音乐播放控制管理器。
     */
    var mediaManager: MediaManager? = null
        private set

    companion object {

        /**
         * 全局Application单例。
         */
        var instance: MusicApp? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        init()
    }

    private fun init() {
        initSdk()   //初始化第三方SDK
        initDb()    //初始化SQLite数据库的表
        initMedia() //初始化媒体管理器
    }

    private fun initMedia() {
        mediaManager = MediaManager(this)
    }

    private fun initSdk() {
        //XSkin
        SkinManager.getInstance().init(this)
        //Apollo
        Apollo.init(AndroidSchedulers.mainThread(), this)
        RetrofitManager.initConfig {
            okhttp {
                interceptors().add(FormatLogInterceptor())
                build()
            }
            mappingBaseUrl(MusicService::class.java, AppConfig.URL_APP_SERVER)
            mappingBaseUrl(UserService::class.java, AppConfig.URL_APP_SERVER)
        }
    }

    private fun initDb() {
        Orm.init(this, OrmConfig.Builder()
            .database("db_doramusic")
            .version(1)
            .tables(Music::class.java, Artist::class.java,
                Album::class.java, Folder::class.java)
            .build())
    }
}
