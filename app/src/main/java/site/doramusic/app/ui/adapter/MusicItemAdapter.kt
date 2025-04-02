package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.util.MusicUtils

import site.doramusic.app.R
import site.doramusic.app.db.Music

class MusicItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_music) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    override fun convert(holder: BaseViewHolder, item: Music) {
        holder.setText(R.id.tv_music_name, item.musicName)
        holder.setText(R.id.tv_music_artist, item.artist)
        holder.setText(R.id.tv_music_duration, MusicUtils.formatTime(item.duration.toLong()))
    }
}
