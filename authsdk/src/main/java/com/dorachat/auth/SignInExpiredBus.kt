package com.dorachat.auth

import dora.util.RxBus
import java.util.concurrent.atomic.AtomicBoolean

internal object SignInExpiredBus {

    private val fired = AtomicBoolean(false)

    fun postOnce() {
        if (fired.compareAndSet(false, true)) {
            RxBus.getInstance().post(SignOutEvent())
        }
    }

    /**
     * 在重新登录成功后重置。
     */
    fun reset() {
        fired.set(false)
    }
}
