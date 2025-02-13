package site.doramusic.app.ui

import android.view.LayoutInflater
import android.view.View
import dora.db.table.OrmTable
import dora.util.StatusBarUtils
import site.doramusic.app.ui.layout.ILyricDrawer

abstract class UIFactory(protected var drawer: ILyricDrawer,
                         protected var manager: UIManager) : ViewInflater {

    protected val inflater: LayoutInflater = manager.inflater

    override fun getView(from: Int, obj: OrmTable?): View {
        throw UnsupportedOperationException("请在子类实现它")
    }

    protected fun getStatusBarHeight() : Int {
        return StatusBarUtils.getStatusBarHeight()
    }
}
