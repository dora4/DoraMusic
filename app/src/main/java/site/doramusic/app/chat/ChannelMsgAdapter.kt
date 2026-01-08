package site.doramusic.app.chat

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R

class ChannelMsgAdapter(val erc20: String) :
    BaseQuickAdapter<DoraChannelMsg, BaseViewHolder>(
        R.layout.item_channel_msg
    ) {

    override fun convert(holder: BaseViewHolder, item: DoraChannelMsg) {
        val isLeft = item.senderId != erc20
        if (isLeft) {
            holder.setVisible(R.id.rl_left_content, true)
            holder.setGone(R.id.rl_right_content, true)
            holder.setText(R.id.tv_left_name, item.senderId)
            holder.setText(
                R.id.tv_left_content,
                item.msgContent
            )
        } else {
            holder.setGone(R.id.rl_left_content, true)
            holder.setVisible(R.id.rl_right_content, true)
            holder.setText(
                R.id.tv_right_content,
                item.msgContent
            )
        }
    }
}
