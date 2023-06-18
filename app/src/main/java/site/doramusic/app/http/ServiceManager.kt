package site.doramusic.app.http

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.http.service.MusicService
import site.doramusic.app.http.service.UpdateService
import site.doramusic.app.http.service.UserService
import java.util.*

/**
 * 网络访问接口管理。
 */
class ServiceManager private constructor() : AppConfig {

    var urlMap: MutableMap<Class<*>, String> = HashMap()
    var retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()

    private fun <T> _getService(clazz: Class<T>): T {
        val retrofit: Retrofit?
        if (retrofitMap.containsKey(clazz)) {
            retrofit = retrofitMap[clazz]
            return retrofit!!.create(clazz)
        } else {
            retrofit = Retrofit.Builder()
                    .baseUrl(urlMap[clazz])
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            retrofitMap[clazz] = retrofit
        }
        return retrofit.create(clazz)
    }

    companion object {
        private var sInstance: ServiceManager? = null
        private val instance: ServiceManager?
            private get() {
                if (sInstance == null) {
                    synchronized(ServiceManager::class.java) {
                        if (sInstance == null) {
                            sInstance = ServiceManager()
                        }
                    }
                }
                return sInstance
            }

        fun <T> getService(clazz: Class<T>): T {
            return instance!!._getService(clazz)
        }
    }

    init {
        urlMap[MusicService::class.java] = AppConfig.URL_APP_SERVER
        urlMap[UserService::class.java] = AppConfig.URL_APP_SERVER
        urlMap[UpdateService::class.java] = AppConfig.URL_FILE_SERVER
    }
}