package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.http.DoraGuessingRank

class GuessingRankAdapter(val type: Int) :
    BaseQuickAdapter<DoraGuessingRank, BaseViewHolder>(R.layout.item_guessing_rank) {

    private fun formatUserId(userId: String): String {
        return if (isEvmAddress(userId)) {
            "${userId.take(6)}****${userId.takeLast(4)}"
        } else {
            userId
        }
    }

    private fun isEvmAddress(address: String): Boolean {
        return address.matches(Regex("^0x[a-fA-F0-9]{40}$"))
    }

    override fun convert(holder: BaseViewHolder, item: DoraGuessingRank) {
        holder.setText(R.id.tvRank, (holder.adapterPosition + 1).toString())
        if (getItemPosition(item) == 0) {
            holder.setGone(R.id.tvRank, true)
            holder.setVisible(R.id.ivRank, true)
            holder.setImageResource(R.id.ivRank, R.drawable.ic_rank_crown_gold)
        } else if (getItemPosition(item) == 1) {
            holder.setGone(R.id.tvRank, true)
            holder.setVisible(R.id.ivRank, true)
            holder.setImageResource(R.id.ivRank, R.drawable.ic_rank_crown_silver)
        } else if (getItemPosition(item) == 2) {
            holder.setGone(R.id.tvRank, true)
            holder.setVisible(R.id.ivRank, true)
            holder.setImageResource(R.id.ivRank, R.drawable.ic_rank_crown_copper)
        }
        holder.setText(R.id.tvTitle, formatUserId(item.userId))
        if (type == 0) {
            holder.setText(
                R.id.tvValue,
                ("%.2f".format(item.winRate * 100))
                    .trimEnd('0')
                    .trimEnd('.')
                    .plus("%")
            )
            holder.setText(R.id.tvLabel, context.getString(R.string.rate))
        } else if (type == 1) {
            holder.setText(R.id.tvValue, item.profit.toString())
            holder.setText(R.id.tvLabel, context.getString(R.string.profit))
        } else if (type == 2) {
            holder.setText(R.id.tvValue, item.totalBet.toString())
            holder.setText(R.id.tvLabel, context.getString(R.string.bet))
        }
    }
}
