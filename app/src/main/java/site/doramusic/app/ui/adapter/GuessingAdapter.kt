package site.doramusic.app.ui.adapter

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.http.DoraHttp.api
import dora.http.DoraHttp.net
import dora.util.DensityUtils
import dora.util.LogUtils
import dora.util.TimeUtils
import dora.util.ToastUtils
import dora.widget.DoraMarkView
import dora.widget.FlowLayout
import site.doramusic.app.R
import site.doramusic.app.http.DoraGuessingInfoWithItems
import site.doramusic.app.http.guessing.ReqGuessingBet
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.score.PointsManager
import site.doramusic.app.score.PointsSource

class GuessingAdapter(private val token: String,
                      private val onPointsChanged: (() -> Unit)? = null) :
    BaseQuickAdapter<DoraGuessingInfoWithItems, BaseViewHolder>(
        R.layout.item_guessing
    ) {

    /**
     * optionId -> 本地累计投注。
     */
    private val betCache = mutableMapOf<Long, Long>()

    /**
     * guessingId -> 已投注选项。
     *
     * 注意：
     * 最终应该以后端返回为准。
     */
    private val selectedOptionCache =
        mutableMapOf<Long, Long>()

    /**
     * 防重复点击。
     */
    private val bettingSet =
        mutableSetOf<Long>()

    override fun convert(
        holder: BaseViewHolder,
        item: DoraGuessingInfoWithItems
    ) {
        val markView =
            holder.getView<DoraMarkView>(
                R.id.mv_guessing_item
            )
        val tvTitle =
            holder.getView<TextView>(
                R.id.tv_guessing_title
            )
        val tvTime =
            holder.getView<TextView>(
                R.id.tv_guessing_time
            )
        val flow =
            holder.getView<FlowLayout>(
                R.id.fl_options
            )
        markView.clearDrawableMarks()
        if (item.status == 2) {
            if (item.isBet) {
                if (item.isHit) {
                    markView.addDrawableMark(ContextCompat.getDrawable(context,
                        R.drawable.ic_seal_win) as Drawable,
                        gravity = Gravity.BOTTOM or Gravity.END, 0)
                } else {
                    markView.addDrawableMark(ContextCompat.getDrawable(context,
                        R.drawable.ic_seal_lose) as Drawable,
                        gravity = Gravity.BOTTOM or Gravity.END, 0)
                }
            } else {
                // 未参与
//                markView.addDrawableMark(ContextCompat.getDrawable(context,
//                    R.drawable.ic_seal_miss) as Drawable,
//                    gravity = Gravity.BOTTOM or Gravity.END, 0)
            }
        }
        tvTitle.text = item.title
        tvTime.text =
            context.getString(
                R.string.time_format, TimeUtils.getTimeString(
                    item.closeTime * 1000,
                    "yyyy-MM-dd HH:mm"
                )
            )
        when (item.status) {
            0 -> {
                markView.addDrawableMark(ContextCompat.getDrawable(context,
                    R.drawable.ic_status_start_betting) as Drawable,
                    gravity = Gravity.TOP or Gravity.END, 0)
            }
            1 -> {
                markView.addDrawableMark(ContextCompat.getDrawable(context,
                    R.drawable.ic_status_stop_betting) as Drawable,
                    gravity = Gravity.TOP or Gravity.END, 0)
            }
        }
        flow.removeAllViews()
        val items = item.items
        items.forEach { option ->
            val chip = LayoutInflater
                .from(context)
                .inflate(R.layout.item_guessing_option,
                    flow, false
                )
            val root =
                chip.findViewById<View>(
                    R.id.bet_item_root
                )
            val tvName =
                chip.findViewById<TextView>(
                    R.id.tv_guessing_bet_description
                )
            val tvOdds =
                chip.findViewById<TextView>(
                    R.id.tv_guessing_bet_odds
                )
            tvName.text = option.itemDesc
            root.isSelected = option.isWin == true
            root.isEnabled = item.status == 0
            val currentBet =
                betCache[option.id]
                    ?: option.totalScore
            val totalPool =
                items.sumOf {
                    betCache[it.id]
                        ?: it.totalScore
                }
            tvOdds.text = context.getString(
                R.string.odds_format, calculateOdds(
                    totalPool,
                    currentBet
                )
            )
            tvOdds.visibility = View.VISIBLE
            val params = FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin =
                        DensityUtils.DP8
                    bottomMargin =
                        DensityUtils.DP8
                }
            chip.layoutParams = params
            root.setOnClickListener {
                // 防重复请求
                if (bettingSet.contains(item.id)) {
                    return@setOnClickListener
                }
                // 当前竞猜已投注选项
                val selectedOptionId =
                    selectedOptionCache[item.id]
                // 已投其它选项
                if (selectedOptionId != null
                    && selectedOptionId != option.id
                ) {
                    ToastUtils.showShort(
                        context.getString(R.string.already_bet_another_option)
                    )
                    return@setOnClickListener
                }
                // 点击时重新计算赔率
                val latestTotalPool =
                    items.sumOf {
                        betCache[it.id]
                            ?: it.totalScore
                    }
                val latestItemBet =
                    betCache[option.id]
                        ?: option.totalScore
                BettingDialog(
                    context = context,
                    betTitle = item.title,
                    optionName = option.itemDesc,
                    odds = calculateOdds(
                        latestTotalPool,
                        latestItemBet
                    )
                ) { amount ->
                    bettingSet.add(item.id)
                    bet(
                        guessingId = item.id,
                        optionId = option.id,
                        amount = amount,
                        onSuccess = {
                            // 成功后记录
                            selectedOptionCache[item.id] =
                                option.id
                            val newTotal =
                                (betCache[option.id]
                                    ?: option.totalScore) + amount
                            betCache[option.id] =
                                newTotal
                            val position =
                                holder.bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                notifyItemChanged(position)
                            }
                        },
                        onFinish = {
                            bettingSet.remove(item.id)
                        }
                    )
                }.show()
            }
            flow.addView(chip)
        }
    }

    /**
     * 赔率计算。
     */
    private fun calculateOdds(
        totalPool: Long,
        itemBet: Long
    ): String {
        if (itemBet <= 0L || totalPool <= 0L) {
            return "0.00"
        }
        val odds =
            totalPool.toDouble() /
                    itemBet.toDouble()
        return String.format("%.2f", odds)
    }

    /**
     * 投注。
     */
    private fun bet(
        guessingId: Long,
        optionId: Long,
        amount: Long,
        onSuccess: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null
    ) {
        val totalPoints = PointsManager.getTotalPoints()
        if (amount > totalPoints) {
            ToastUtils.showShort(R.string.insufficient_points)
            return
        }
        net {
            try {
                val req = ReqGuessingBet(
                    token = token,
                    guessingId = guessingId,
                    itemId = optionId,
                    score = amount
                )
                val ok =
                    api(GuessingService::class) {
                        bet(req.toRequestBody())
                    }?.data
                if (ok == true) {
                    onSuccess?.invoke()
                    ToastUtils.showShort(
                        context.getString(R.string.bet_successful)
                    )
                    PointsManager.addPoints(PointsSource.EXCHANGE.desc, -amount.toInt(),
                        "竞猜扣除")
                    onPointsChanged?.invoke()
                } else {
                    ToastUtils.showShort(
                        context.getString(R.string.failed_to_bet)
                    )
                }
            } catch (e: Exception) {
                LogUtils.e(e)
                ToastUtils.showShort(
                    context.getString(R.string.failed_to_bet)
                )
            } finally {
                onFinish?.invoke()
            }
        }
    }
}
