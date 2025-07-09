package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.model.Donation
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 捐赠记录。
 */
class DonationAdapter : BaseQuickAdapter<Donation, BaseViewHolder>(R.layout.item_donation) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#.########") // 最多8位小数，非必须

    override fun convert(holder: BaseViewHolder, item: Donation) {
        val formattedTime = dateFormat.format(Date(item.timestamp))
        val formattedAmount = decimalFormat.format(item.tokenAmount)
        holder.setText(R.id.tv_donation_time, formattedTime)
        holder.setText(R.id.tv_donation_value, "$formattedAmount ${item.tokenSymbol}")
        holder.setText(R.id.tv_donation_hash, item.transactionHash)
    }
}
