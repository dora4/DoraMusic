package site.doramusic.app.ui

import android.view.LayoutInflater
import android.view.View
import dora.db.table.OrmTable
import dora.util.StatusBarUtils
import site.doramusic.app.ui.layout.ILyricDrawer

abstract class UIFactory(protected var drawer: ILyricDrawer,
                         protected var manager: UIManager) : ViewInflater {

    protected var inflater: LayoutInflater = manager.inflater

    /**
     * 保证子类可以不用重写这个方法。
     */
    override fun getView(from: Int, obj: OrmTable?): View {
        throw UnsupportedOperationException("不支持在此层级调用")
    }

    protected fun getStatusBarHeight() : Int {
        return StatusBarUtils.getStatusBarHeight()
    }
}