package site.doramusic.app.ui.activity

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
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
import site.doramusic.app.http.guessing.ReqGuessingNicknameSet
import site.doramusic.app.http.guessing.ReqGuessingToken
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.score.PointsManager
import site.doramusic.app.ui.adapter.GuessingAdapter
import site.doramusic.app.ui.dialog.NicknameDialog
import site.doramusic.app.util.ThemeSelector
import androidx.core.graphics.drawable.toDrawable
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_TOKEN
import site.doramusic.app.conf.AppConfig.Companion.EXTRA_USER_ID

@Route(path = ARoutePath.ACTIVITY_GUESSING)
class GuessingActivity : BaseSkinActivity<ActivityGuessingBinding>() {

    private lateinit var adapter: GuessingAdapter
    private lateinit var token: String
    private lateinit var userId: String

    override fun onSetStatusBar() {
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    private fun showRulePopup(anchor: View) {
        val contentView = LayoutInflater.from(this)
            .inflate(R.layout.popup_guessing_rule, null)

        val tvRuleContent = contentView.findViewById<TextView>(R.id.tvRuleContent)
        val ivRuleClose = contentView.findViewById<ImageView>(R.id.ivRuleClose)

        val content = """
        【参与方式】
        
        支持游客登录与Dora Chat账号登录。
        游客可直接参与竞猜，更换设备建议使用账号登录。
        
        【积分获取】
        
        每累计播放本地音乐1分钟，可获得10积分。
        
        【投注规则】
        
        • 单场竞猜投注范围：1~100000积分
        • 站队后仅可追加当前选项
        • 不可再投注其它选项
        • 单场累计最高100000积分
        
        【赔率说明】
        
        赔率 = 奖励积分 ÷ 投入积分
        
        【排行榜】
        
        排行榜分为胜率榜、盈亏榜、投注榜。
        胜率榜至少需要参与5场竞猜才会计入。
        
        游客登录显示游客ID；
        账号登录显示ERC20钱包地址部分内容；
        设置昵称后优先显示昵称。
        
        【竞猜结算】
        
        比赛开始自动封盘，特殊情况管理员可手动封盘。
        封盘后不可继续投注。
        
        比赛结束后系统自动计算奖励积分，
        需前往“我的竞猜”手动领取奖励。
        
        【淘汰赛猜对胜负额外奖励】
        
        冠亚军决赛：100000积分
        三四名决赛：90000积分
        半决赛：80000积分
        1/4决赛：70000积分
        1/8决赛：60000积分
        1/16决赛：50000积分
        
    """.trimIndent()
        val spannable = SpannableString(content)
        val highlights = arrayOf(
            "100000",
            "90000",
            "80000",
            "70000",
            "60000",
            "50000",
            "5",
            "1~100000",
            "“我的竞猜”"
        )
        highlights.forEach { item ->
            var index = content.indexOf(item)
            while (index >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(getColor(R.color.colorPrimary)),
                    index,
                    index + item.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + item.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = content.indexOf(
                    item,
                    index + item.length
                )
            }
        }
        tvRuleContent.text = spannable
        val popupWindow = PopupWindow(
            contentView,
            (Resources.getSystem().displayMetrics.widthPixels * 0.85f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = 12f
        popupWindow.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )
        val rootView = window.decorView
        popupWindow.showAtLocation(
            rootView,
            Gravity.CENTER,
            0,
            0
        )
        ivRuleClose.setOnClickListener {
            popupWindow.dismiss()
        }
    }

    override fun onGetExtras(action: String?, bundle: Bundle?, intent: Intent) {
        token = IntentUtils.getStringExtra(intent, EXTRA_TOKEN)
        userId = IntentUtils.getStringExtra(intent, EXTRA_USER_ID)
    }

    override fun onResume() {
        super.onResume()
        mBinding.tvMyPoints.text =
            getString(R.string.my_points_format, PointsManager.getTotalPoints())
        loadList()
        loadProfile()
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityGuessingBinding) {
        binding.statusbarGuessing.layoutParams = LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StatusBarUtils.getStatusBarHeight())
        ThemeSelector.applyViewTheme(binding.statusbarGuessing)
        ThemeSelector.applyViewTheme(binding.titlebarGuessing)
        binding.tvMyPoints.text =
            getString(R.string.my_points_format, PointsManager.getTotalPoints())
        adapter = GuessingAdapter(token) {
            // 投注成功后刷新积分
            binding.tvMyPoints.text =
                getString(R.string.my_points_format, PointsManager.getTotalPoints())
        }
        binding.layoutRule.setOnClickListener { v ->
            showRulePopup(v)
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
                        withString(EXTRA_USER_ID, userId)
                        withString(EXTRA_TOKEN, token)
                    }
                }
            }
        })
        binding.ivEditNickname.setOnClickListener {
            showNicknameDialog()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GuessingActivity)
            adapter = this@GuessingActivity.adapter
        }
        binding.swipeLayout.setOnSwipeListener(object : SwipeLayout.OnSwipeListener {
            override fun onRefresh(swipeLayout: SwipeLayout) {
                loadProfile()
                loadList()
                // 也刷新积分
                binding.tvMyPoints.text =
                    getString(R.string.my_points_format, PointsManager.getTotalPoints())
                swipeLayout.refreshFinish(SwipeLayout.SUCCEED)
            }

            override fun onLoadMore(swipeLayout: SwipeLayout) {
            }
        })
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

    private fun showNicknameDialog() {
        NicknameDialog(
            this,
            mBinding.tvGuessingUserId.text.toString()
        ) { nickname ->
            updateNickname(nickname)
        }.show()
    }

    private fun updateNickname(nickname: String) {
        net {
            val req = ReqGuessingNicknameSet(token, nickname)
            val resp = result(GuessingService::class) {
                setNickname(req.toRequestBody())
            }
            if (resp?.data == true) {
                mBinding.tvGuessingUserId.text = nickname
                showShortToast("设置成功")
            } else {
                showShortToast("设置失败")
            }
        }
    }

    private fun loadProfile() {
        net {
            val req = ReqGuessingToken(token)
            val user = result(GuessingService::class) {
                getProfile(req.toRequestBody())
            }?.data
            val title = user?.nickname
                ?.ifEmpty { formatUserId(userId) }
                ?: formatUserId(userId)
            mBinding.tvGuessingUserId.text = title
        }
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
