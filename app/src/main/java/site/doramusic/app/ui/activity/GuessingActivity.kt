package site.doramusic.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import dora.arouter.open
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import dora.util.DensityUtils
import dora.util.IntentUtils
import dora.util.StatusBarUtils
import dora.widget.DoraTitleBar
import dora.widget.pull.SwipeLayout
import site.doramusic.app.R
import site.doramusic.app.conf.ARoutePath
import site.doramusic.app.databinding.ActivityGuessingBinding
import site.doramusic.app.http.guessing.ReqGuessingToken
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.score.PointsManager
import site.doramusic.app.ui.adapter.GuessingAdapter
import site.doramusic.app.util.ThemeSelector

@Route(path = ARoutePath.ACTIVITY_GUESSING)
class GuessingActivity : BaseSkinActivity<ActivityGuessingBinding>() {

    private lateinit var adapter: GuessingAdapter
    private lateinit var token: String
    private lateinit var userId: String

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        token = IntentUtils.getStringExtra(intent, "token")
        userId = IntentUtils.getStringExtra(intent, "userId")
    }

    override fun onResume() {
        super.onResume()
        mBinding.tvMyPoints.text =
            getString(R.string.my_points_format, PointsManager.getTotalPoints())
        loadList()
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityGuessingBinding) {
        binding.statusbarGuessing.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarGuessing)
        ThemeSelector.applyViewTheme(binding.titlebarGuessing)
        binding.tvMyPoints.text =
            getString(R.string.my_points_format, PointsManager.getTotalPoints())
        binding.tvGuessingUserId.text = formatUserId(userId)
        adapter = GuessingAdapter(token) {
            // 投注成功后刷新积分
            binding.tvMyPoints.text =
                getString(R.string.my_points_format, PointsManager.getTotalPoints())
        }
        binding.titlebarGuessing.addMenuButton(
            iconResId = R.drawable.ic_crown,
            iconSize = DensityUtils.DP24,
            tintColor = ContextCompat.getColor(
                this,
                R.color.gold_yellow
            ).toInt()
        ).addMenuButton(
            iconResId = R.drawable.ic_reward,
            iconSize = DensityUtils.DP24,
            tintColor = ContextCompat.getColor(
                this,
                R.color.vibrant_orange
            ).toInt()
        ).setOnIconClickListener(object : DoraTitleBar.OnIconClickListener {

            override fun onIconBackClick(icon: AppCompatImageView) {
            }

            override fun onIconMenuClick(
                position: Int,
                icon: AppCompatImageView
            ) {
                if (position == 0) {
                    open(ARoutePath.ACTIVITY_GUESSING_RANK)
                } else {
                    open(ARoutePath.ACTIVITY_GUESSING_REWARD) {
                        withString("userId", userId)
                        withString("token", token)
                    }
                }
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GuessingActivity)
            adapter = this@GuessingActivity.adapter
        }
        binding.swipeLayout.setOnSwipeListener(object : SwipeLayout.OnSwipeListener {
            override fun onRefresh(swipeLayout: SwipeLayout) {
                loadList()
                swipeLayout.refreshFinish(SwipeLayout.SUCCEED)
            }

            override fun onLoadMore(swipeLayout: SwipeLayout) {
            }
        })
//        loadList()
    }

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

    private fun loadList() {
        net {
            val req = ReqGuessingToken(token)
            val data = result(GuessingService::class) { getList(req.toRequestBody()) }?.data
            adapter.setList(data)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_guessing
    }
}
