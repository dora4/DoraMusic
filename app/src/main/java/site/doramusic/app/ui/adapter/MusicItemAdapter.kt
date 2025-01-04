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
//        val ivMusicPlayState = holder.getView(R.id.iv_music_play_state) as ImageView
//        val mediaManager = MusicApp.app.mediaManager
//        if (mediaManager.curMusicId !== -1 && mediaManager.curMusicId === music.songId
//            && mediaManager.playState === AppConfig.MPS_PLAYING) {
//            ivMusicPlayState.visibility = View.VISIBLE
//        } else {
//            ivMusicPlayState.visibility = View.GONE
//        }
    }
}
