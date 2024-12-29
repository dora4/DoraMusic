package site.doramusic.app.ui.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.ui.activity.ChoiceColorActivity

class ChoiceColorAdapter : BaseQuickAdapter<ChoiceColorActivity
    .ColorData, BaseViewHolder>(R.layout.item_choose_color) {

    var selectedPosition = -1 // 选中的位置

    override fun convert(holder: BaseViewHolder, item: ChoiceColorActivity.ColorData) {
        val ivChoiceColorSkinBg = holder.getView(R.id.iv_choice_color_skin_bg) as ImageView
        val ivChoiceColorSkinSelect = holder.getView(R.id.iv_choice_color_skin_select) as ImageView
        ivChoiceColorSkinBg.setImageResource(item.backgroundResId)
        if (getItemPosition(item) == selectedPosition) {
            ivChoiceColorSkinSelect.visibility = View.VISIBLE
        } else {
            ivChoiceColorSkinSelect.visibility = View.GONE
        }
    }
}
