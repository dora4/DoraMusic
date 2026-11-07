package site.doramusic.app.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.skin.SkinManager
import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.util.PrefsManager

class PlaylistItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_playlist) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: Music) {
        val tvIndex = holder.getView<TextView>(R.id.tvIndex)
        val vIndicator = holder.getView<View>(R.id.vIndicator)
        holder.getView<LinearLayout>(R.id.ll_playlist).background = ContextCompat
            .getDrawable(context, R.drawable.selector_item_common)
        tvIndex.text = "${holder.layoutPosition + 1}"
        holder.getView<TextView>(R.id.tvTitle).text = item.musicName
        holder.getView<TextView>(R.id.tvSubTitle).text = item.artist
        val prefsManager = PrefsManager(context)
        val themeColor = if (prefsManager.getSkinType() == 0) {
            prefsManager.getSkinColor()
        } else {
            SkinManager.getLoader().getColor(COLOR_THEME)
        }
        tvIndex.setTextColor(themeColor)
        vIndicator.setBackgroundColor(themeColor)
        if (MediaManager.curMusicId == item.songId && MediaManager.playState == AppConfig.MPS_PLAYING) {
            vIndicator.visibility = View.VISIBLE
        } else {
            vIndicator.visibility = View.GONE
        }
    }
}
