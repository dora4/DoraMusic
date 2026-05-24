package site.doramusic.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import dora.http.DoraHttp.api
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.util.IntentUtils
import dora.util.LogUtils
import dora.util.StatusBarUtils
import dora.util.ToastUtils
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityGuessingRewardBinding
import site.doramusic.app.http.DoraGuessingReward
import site.doramusic.app.http.guessing.ReqGuessingClaim
import site.doramusic.app.http.guessing.ReqGuessingToken
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.score.PointsManager
import site.doramusic.app.score.PointsSource
import site.doramusic.app.ui.adapter.GuessingRewardAdapter
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_GUESSING_REWARD)
class GuessingRewardActivity :
    BaseSkinActivity<ActivityGuessingRewardBinding>() {

    private val data = mutableListOf<DoraGuessingReward>()

    private lateinit var adapter: GuessingRewardAdapter

    private lateinit var token: String
    private lateinit var userId: String

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        token = IntentUtils.getStringExtra(intent, "token")
        userId = IntentUtils.getStringExtra(intent, "userId")
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: ActivityGuessingRewardBinding
    ) {
        binding.statusbarGuessingReward.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                StatusBarUtils.getStatusBarHeight()
            )
        ThemeSelector.applyViewTheme(
            binding.statusbarGuessingReward
        )
        ThemeSelector.applyViewTheme(
            binding.titlebarGuessingReward
        )
        adapter = GuessingRewardAdapter(data) {
            claim(it)
        }
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.recyclerView.adapter = adapter
        loadData()
    }

    /**
     * 加载竞猜奖励列表
     */
    private fun loadData() {
        net {
            try {
                val req = ReqGuessingToken(
                    token = token
                )
                val result =
                    api(GuessingService::class) {
                        getRewardList(req.toRequestBody())
                    }?.data
                data.clear()
                if (result != null) {
                    data.addAll(result)
                    mBinding.emptyLayout.showContent()
                } else {
                    mBinding.emptyLayout.showEmpty()
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                LogUtils.e(e)
                ToastUtils.showShort(
                    e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 领取奖励
     */
    private fun claim(item: DoraGuessingReward) {
        if (item.claimed || !item.win) return
        net {
            try {
                val req = ReqGuessingClaim(
                    token = token,
                    guessingId = item.guessingId
                )
                val reward =
                    api(GuessingService::class) {
                        claim(req.toRequestBody())
                    }?.data ?: 0L
                if (reward > 0) {
                    PointsManager.addPoints(
                        PointsSource.EVENT.desc,
                        reward.toInt(),
                        "竞猜活动"
                    )
                    ToastUtils.showShort("领取成功 +${reward}积分")
                    item.claimed = true
                    val index = data.indexOfFirst {
                        it.guessingId == item.guessingId
                    }
                    if (index != -1) {
                        adapter.notifyItemChanged(index)
                    }
                } else {
                    ToastUtils.showShort("领取失败")
                }
            } catch (e: Exception) {
                LogUtils.e(e)
                ToastUtils.showShort(e.message ?: "领取失败")
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_guessing_reward
    }
}
