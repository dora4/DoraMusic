package site.doramusic.app.ui

import android.view.View
import dora.db.table.OrmTable
import site.doramusic.app.conf.AppConfig

/**
 * 视图加载器。
 */
interface ViewInflater {

    /**
     * 区分来源，获取播放列表的视图。
     */
    fun getView(from: Int = AppConfig.ROUTE_START_FROM_LOCAL, table: OrmTable? = null): View
}