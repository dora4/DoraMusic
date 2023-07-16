package site.doramusic.app.ui.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.util.MusicUtils

import site.doramusic.app.MusicApp
import site.doramusic.app.R
import site.doramusic.app.base.conf.AppConfig
import site.doramusic.app.db.Music

class MusicItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_music) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    override fun convert(holder: BaseViewHolder, music: Music) {
        holder.setText(R.id.tv_music_name, music.musicName)
        holder.setText(R.id.tv_music_artist, music.artist)
        holder.setText(R.id.tv_music_duration, MusicUtils.formatTime(music.duration.toLong()))
        val iv_music_play_state = holder.getView(R.id.iv_music_play_state) as ImageView
        val mediaManager = MusicApp.instance!!.mediaManager
        if (mediaManager!!.curMusicId !== -1 && mediaManager!!.curMusicId === music.songId
            && mediaManager!!.playState === AppConfig.MPS_PLAYING) {
            iv_music_play_state.visibility = View.VISIBLE
        } else {
            iv_music_play_state.visibility = View.GONE
        }
    }
}
