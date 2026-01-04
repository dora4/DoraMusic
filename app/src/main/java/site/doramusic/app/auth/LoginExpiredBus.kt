package site.doramusic.app.auth

import java.util.concurrent.atomic.AtomicBoolean

object LoginExpiredBus {

    private val fired = AtomicBoolean(false)

    fun postOnce() {
        if (fired.compareAndSet(false, true)) {
            // 在这里统一退出登录
        }
    }

    fun reset() {
        fired.set(false)
    }
}
