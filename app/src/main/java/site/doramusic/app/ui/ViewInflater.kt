package site.doramusic.app.ui

import android.view.View
import dora.db.table.OrmTable
import site.doramusic.app.base.conf.AppConfig

/**
 * 视图加载器，用来返回各种视图。
 */
interface ViewInflater {
    fun getView(from: Int = AppConfig.ROUTE_START_FROM_LOCAL, obj: OrmTable? = null): View
}