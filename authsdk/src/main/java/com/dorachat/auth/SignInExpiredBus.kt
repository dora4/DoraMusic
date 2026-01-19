package com.dorachat.auth

import dora.util.RxBus
import java.util.concurrent.atomic.AtomicBoolean

internal object SignInExpiredBus {

    private val fired = AtomicBoolean(false)

    /**
     * 只执行一次。
     */
    fun postOnce() {
        if (fired.compareAndSet(false, true)) {
            RxBus.getInstance().post(SignOutEvent())
        }
    }

    /**
     * 在重新登录成功后，标志位会被重置。
     */
    fun reset() {
        fired.set(false)
    }
}
