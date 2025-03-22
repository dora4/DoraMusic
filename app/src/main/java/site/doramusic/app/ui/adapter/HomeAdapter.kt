package site.doramusic.app.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.skin.SkinManager
import dora.util.DensityUtils
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.ui.fragment.HomeFragment

class HomeAdapter : BaseQuickAdapter<HomeFragment.HomeItem, BaseViewHolder>(R.layout.item_home_module) {

    override fun convert(holder: BaseViewHolder, item: HomeFragment.HomeItem) {
        holder.getView<AppCompatImageView>(R.id.iv_home_module_icon).apply {
            setImageResource(item.iconRes)
            // 创建圆形背景
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                val skinThemeColor = SkinManager.getLoader().getColor(COLOR_THEME)
                setColor(skinThemeColor)
                setStroke(DensityUtils.DP1, Color.WHITE)
            }
            background = backgroundDrawable
        }
        holder.getView<TextView>(R.id.tv_home_module_name).text = item.name
        holder.getView<TextView>(R.id.tv_home_module_num).text = "${item.musicNum}"
    }
}
