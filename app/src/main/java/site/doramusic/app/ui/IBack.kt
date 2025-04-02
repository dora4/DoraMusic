package site.doramusic.app.ui

import site.doramusic.app.base.callback.OnBackListener

/**
 * 界面返回接口。
 */
interface IBack {

    /**
     * 注册界面返回的监听器。
     */
    fun registerBackListener(listener: OnBackListener)

    /**
     * 取消注册界面返回的监听器。
     */
    fun unregisterBackListener(listener: OnBackListener)
}