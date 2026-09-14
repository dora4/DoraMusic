package site.doramusic.app.ui.adapter

import android.view.View
import android.widget.ProgressBar
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.widget.DoraAudioWaveProgressBar

import site.doramusic.app.util.MusicUtils

import site.doramusic.app.R
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.db.Music
import site.doramusic.app.media.MediaManager
import site.doramusic.app.util.ThemeSelector

class MusicItemAdapter : BaseSortItemAdapter<Music>(R.layout.item_music) {

    override fun getSortKey(data: Music): String {
        return data.musicName
    }

    override fun convert(holder: BaseViewHolder, item: Music) {
        holder.setText(R.id.tv_music_name, item.musicName)
        holder.setText(R.id.tv_music_artist, item.artist)
        holder.setText(R.id.tv_music_duration, MusicUtils.formatTime(item.duration.toLong()))
        val progressBar = holder.getView<DoraAudioWaveProgressBar>(R.id.progressBar)
        val skinThemeColor = ThemeSelector.getThemeColor(context)
        progressBar.setColor(skinThemeColor)
        if (MediaManager.curMusicId == item.songId
            && MediaManager.playState == AppConfig.MPS_PLAYING
        ) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }
}
