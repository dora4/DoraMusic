package site.doramusic.app.ui.adapter

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        if (mediaManager.curMusicId != -1 && music.songId == mediaManager.curMusicId
            && mediaManager.playState == AppConfig.MPS_PLAYING) {
            holder.getView<LinearLayout>(R.id.ll_playlist).setBackgroundColor(Color.LTGRAY)
        } else {
            holder.getView<LinearLayout>(R.id.ll_playlist).background = ContextCompat
                .getDrawable(context, R.drawable.selector_item_common)
        }
        holder.getView<TextView>(R.id.tv_playlist_music_number).text = "${holder.layoutPosition + 1}"
        holder.getView<TextView>(R.id.tv_playlist_music_name).text = music.musicName
        holder.getView<TextView>(R.id.tv_playlist_music_artist).text = music.artist
    }
}
