package site.doramusic.app.ui.adapter

import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.http.DoraGuessingReward

class GuessingRewardAdapter(
    data: MutableList<DoraGuessingReward>,
    private val onClaim: (DoraGuessingReward) -> Unit
) : BaseQuickAdapter<DoraGuessingReward, BaseViewHolder>(
    R.layout.item_guessing_reward,
    data
) {
    override fun convert(
        holder: BaseViewHolder,
        item: DoraGuessingReward
    ) {
        val markView = holder.getView<dora.widget.DoraMarkView>(R.id.markView)
        val btn = holder.getView<android.widget.TextView>(R.id.tv_claim)
        markView.clearDrawableMarks()
        holder.setText(R.id.tv_title, item.title)
        holder.setText(R.id.tv_score, context.getString(R.string.bet_format, item.totalScore))
        holder.setGone(R.id.tv_reward, !item.win)
        holder.setGone(R.id.tv_odds, !item.win)
        if (item.win) {
            holder.setText(R.id.tv_reward,
                context.getString(R.string.reward_format, item.totalRewardScore))
            holder.setText(R.id.tv_odds, context.getString(R.string.odds_format, item.totalRewardScore))

        }
        markView.addDrawableMark(
            ContextCompat.getDrawable(
                context,
                if (item.win) R.drawable.ic_seal_win else R.drawable.ic_seal_lose
            )!!,
            Gravity.TOP or Gravity.END,
            10
        )
        btn.visibility = if (item.win) View.VISIBLE else View.GONE
        btn.setOnClickListener(null)
        if (!item.win) return
        if (item.claimed) {
            btn.text = context.getString(R.string.claimed)
            btn.isEnabled = false
            btn.alpha = 0.5f
            return
        }
        btn.text = context.getString(R.string.claim_reward)
        btn.isEnabled = true
        btn.alpha = 1f
        btn.setOnClickListener {
            onClaim(item)
        }
    }
}
