package site.doramusic.app.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route

import dora.BaseActivity
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.skin.SkinManager
import dora.util.StatusBarUtils

import site.doramusic.app.R
import site.doramusic.app.base.conf.ARoutePath
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.base.conf.AppConfig.Companion.COLUMN_PENDING
import site.doramusic.app.databinding.ActivityDonationBinding
import site.doramusic.app.model.Donation
import site.doramusic.app.ui.adapter.DonationAdapter

/**
 * 感谢信和捐赠记录。就算清除了数据，捐赠记录也会永久在区块链上存证，请放心。
 */
@Route(path = ARoutePath.ACTIVITY_DONATION)
class DonationActivity : BaseActivity<ActivityDonationBinding>() {

    private val adapter: DonationAdapter = DonationAdapter()

    override fun getLayoutId(): Int {
        return R.layout.activity_donation
    }

    override fun onSetStatusBar() {
        super.onSetStatusBar()
        StatusBarUtils.setTransparencyStatusBar(this)
    }

    override fun initData(savedInstanceState: Bundle?, binding: ActivityDonationBinding) {
        binding.statusbarDonation.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtils.getStatusBarHeight()
        )
        SkinManager.getLoader().setBackgroundColor(binding.statusbarDonation, COLOR_THEME)
        adapter.setList(DaoFactory.getDao(Donation::class.java).select(
            QueryBuilder.create().where(
                WhereBuilder.create().addWhereEqualTo(COLUMN_PENDING, true)
            ).orderBy("timestamp DESC"))
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = adapter
    }
}