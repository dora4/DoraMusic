package site.doramusic.app.chat

import dora.util.RxBus
import java.util.concurrent.atomic.AtomicLong

/**
 * 用于统一分发消息。
 */
object ChannelMsgDispatcher {

    private val localMaxMsgSeq = AtomicLong(0)

    fun getMaxSeq() : Long {
        return localMaxMsgSeq.get()
    }

    fun initMaxSeq(seq: Long) {
        localMaxMsgSeq.set(seq)
    }

    fun dispatch(msg: DoraChannelMsg) {
        val seq = msg.msgSeq
        val last = localMaxMsgSeq.get()
        // 保证顺序，去重
        if (seq <= last) return
        if (!localMaxMsgSeq.compareAndSet(last, seq)) {
            return
        }
        RxBus.getInstance().post(ChannelMsgEvent(msg))
    }

    fun reset() {
        localMaxMsgSeq.set(0)
    }
}
