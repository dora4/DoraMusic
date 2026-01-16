package site.doramusic.app.ui.adapter

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.skin.SkinManager

import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig.Companion.COLOR_THEME
import site.doramusic.app.db.Music
import site.doramusic.app.util.PrefsManager

class PlaylistItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_playlist) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: Music) {
        val tvNumber = holder.getView<TextView>(R.id.tv_playlist_music_number)
        holder.getView<LinearLayout>(R.id.ll_playlist).background = ContextCompat
            .getDrawable(context, R.drawable.selector_item_common)
        tvNumber.text = "${holder.layoutPosition + 1}"
        holder.getView<TextView>(R.id.tv_playlist_music_name).text = item.musicName
        holder.getView<TextView>(R.id.tv_playlist_music_artist).text = item.artist
        val prefsManager = PrefsManager(context)
        if (prefsManager.getSkinType() == 0) {
            tvNumber.setBackgroundColor(prefsManager.getSkinColor())
        } else {
            tvNumber.setBackgroundColor(SkinManager.getLoader().getColor(COLOR_THEME))
        }
    }
}
