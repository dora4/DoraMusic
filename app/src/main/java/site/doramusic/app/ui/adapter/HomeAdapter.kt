package site.doramusic.app.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.widget.AppCompatImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.skin.SkinManager
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.ui.fragment.HomeFragment
import site.doramusic.app.util.ThemeSelector

class HomeAdapter : BaseQuickAdapter<HomeFragment.HomeItem, BaseViewHolder>(
    R.layout.item_home_module) {

    override fun convert(holder: BaseViewHolder, item: HomeFragment.HomeItem) {
        holder.getView<AppCompatImageView>(R.id.iv_home_module_icon).apply {
            setImageResource(item.iconRes)
            val themeColor = ThemeSelector.getThemeColor(context)
            imageTintList = ColorStateList.valueOf(themeColor)
        }

        holder.getView<TextView>(R.id.tv_home_module_name).text = item.name
        holder.getView<TextView>(R.id.tv_home_module_num).text = "${item.musicNum}"
    }
}
