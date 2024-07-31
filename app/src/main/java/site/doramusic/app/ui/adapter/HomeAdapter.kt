package site.doramusic.app.ui.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.ui.fragment.HomeFragment

class HomeAdapter : BaseQuickAdapter<HomeFragment.HomeItem, BaseViewHolder>(R.layout.item_home_module) {

    override fun convert(holder: BaseViewHolder, item: HomeFragment.HomeItem) {
        holder.getView<ImageView>(R.id.iv_home_module_icon).setImageResource(item.iconRes)
        holder.getView<TextView>(R.id.tv_home_module_name).text = item.name
        holder.getView<TextView>(R.id.tv_home_module_num).text = "${item.musicNum}"
    }
}