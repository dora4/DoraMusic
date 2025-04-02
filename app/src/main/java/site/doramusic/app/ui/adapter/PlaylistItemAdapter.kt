package site.doramusic.app.ui.adapter

import android.annotation.SuppressLint
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.db.Music

class PlaylistItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_playlist) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: Music) {
        holder.getView<LinearLayout>(R.id.ll_playlist).background = ContextCompat
            .getDrawable(context, R.drawable.selector_item_common)
        holder.getView<TextView>(R.id.tv_playlist_music_number).text = "${holder.layoutPosition + 1}"
        holder.getView<TextView>(R.id.tv_playlist_music_name).text = item.musicName
        holder.getView<TextView>(R.id.tv_playlist_music_artist).text = item.artist
    }
}
