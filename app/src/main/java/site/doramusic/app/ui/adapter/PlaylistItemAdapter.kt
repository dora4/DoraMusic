package site.doramusic.app.ui.adapter

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager

class PlaylistItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_playlist) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    override fun convert(holder: BaseViewHolder, music: Music) {
        val mediaManager = MediaManager(context)
        if (music.songId == mediaManager.curMusicId
            && mediaManager.playState == AppConfig.MPS_PLAYING) {
            holder.getView<LinearLayout>(R.id.ll_playlist).setBackgroundColor(Color.LTGRAY)
        } else {
            holder.getView<LinearLayout>(R.id.ll_playlist).background = context.resources
                .getDrawable(R.drawable.selector_item_common)
        }
        holder.getView<TextView>(R.id.tv_playlist_music_number).text = (holder.adapterPosition + 1).toString()
        holder.getView<TextView>(R.id.tv_playlist_music_name).text = music.musicName
        holder.getView<TextView>(R.id.tv_playlist_music_artist).text = music.artist
    }
}
