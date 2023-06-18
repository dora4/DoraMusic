package site.doramusic.app.ui.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.ui.activity.ChoiceColorActivity

class ChoiceColorAdapter : BaseQuickAdapter<ChoiceColorActivity
    .ColorData, BaseViewHolder>(R.layout.item_choice_color) {

    var selectedPosition = -1 // 选中的位置

    override fun convert(holder: BaseViewHolder, item: ChoiceColorActivity.ColorData) {
        val iv_choice_color_skin_bg = holder.getView(R.id.iv_choice_color_skin_bg) as ImageView
        val iv_choice_color_skin_select = holder.getView(R.id.iv_choice_color_skin_select) as ImageView
        iv_choice_color_skin_bg.setBackgroundResource(item.backgroundResId)
        if (getItemPosition(item) == selectedPosition) {
            iv_choice_color_skin_select.visibility = View.VISIBLE
        } else {
            iv_choice_color_skin_select.visibility = View.GONE
        }
    }
}
