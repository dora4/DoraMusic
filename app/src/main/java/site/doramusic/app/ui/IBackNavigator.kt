package site.doramusic.app.ui

import site.doramusic.app.ui.OnBackListener

/**
 * 界面返回接口。
 */
interface IBackNavigator {

    /**
     * 注册界面返回的监听器。
     */
    fun registerBackListener(listener: OnBackListener)

    /**
     * 取消注册界面返回的监听器。
     */
    fun unregisterBackListener(listener: OnBackListener)
}