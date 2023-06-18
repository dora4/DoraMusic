package site.doramusic.app.ui

import site.doramusic.app.base.callback.OnBackListener

interface IBack {
    fun registerBackListener(listener: OnBackListener)
    fun unregisterBackListener(listener: OnBackListener)
}