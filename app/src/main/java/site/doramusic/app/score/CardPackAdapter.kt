package site.doramusic.app.score

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R

class CardPackAdapter(
    data: List<CardPack>,
    private val onItemClick: (CardPack) -> Unit
) : BaseQuickAdapter<CardPack, BaseViewHolder>(R.layout.item_card_pack, data.toMutableList()) {

    override fun convert(holder: BaseViewHolder, item: CardPack) {
        holder.setImageResource(R.id.ivIcon, item.drawableRes)
        holder.setText(R.id.tvName, item.name)
        holder.setText(R.id.tvStatus, if (item.ownNum == 54) context.getString(R.string.all_collected) else "(${item.ownNum}/54)")
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
}
