package site.doramusic.app.chat

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R

class ChannelMsgAdapter :
    BaseQuickAdapter<DoraChannelMsg, BaseViewHolder>(
        R.layout.item_channel_msg
    ) {

    override fun convert(holder: BaseViewHolder, item: DoraChannelMsg) {
        holder.setText(
            R.id.tvMsg,
            "${item.senderId}：${item.msgContent}"
        )
    }
}
