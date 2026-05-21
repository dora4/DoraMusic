package site.doramusic.app.ui.adapter

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

        holder.setText(R.id.tv_title, item.title)

        holder.setText(
            R.id.tv_score,
            "投注：${item.totalScore}"
        )

        holder.setText(
            R.id.tv_reward,
            "奖励：${item.totalRewardScore}"
        )

        val btn = holder.getView<android.widget.TextView>(
            R.id.tv_claim
        )

        when {

            !item.win -> {

                btn.text = "未中奖"

                btn.isEnabled = false

                btn.alpha = 0.5f
            }

            item.claimed -> {

                btn.text = "已领取"

                btn.isEnabled = false

                btn.alpha = 0.5f
            }

            else -> {

                btn.text = "领取奖励"

                btn.isEnabled = true

                btn.alpha = 1f

                btn.setOnClickListener {

                    onClaim(item)
                }
            }
        }
    }
}