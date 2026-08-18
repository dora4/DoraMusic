package site.doramusic.app.search

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.db.Music

class MusicResultAdapter(
    list: MutableList<Music>,
    private val selectedIds: Set<Int>,
    private val listener: Listener
) : BaseQuickAdapter<Music, BaseViewHolder>(
    R.layout.item_music_result,
    list
) {

    interface Listener {
        fun onChecked(song: Music)
    }

    override fun convert(
        holder: BaseViewHolder,
        item: Music
    ) {
        holder.setText(
            R.id.tv_title,
            item.musicName ?: "未知歌曲"
        )

        holder.setText(
            R.id.tv_song_subtitle,
            "${item.artist ?: "未知歌手"} · ${getAlbumName(item)}"
        )

        val selected = selectedIds.contains(item.songId)

        holder.setText(
            R.id.tv_song_check,
            if (selected) {
                "已选择"
            } else {
                "添加"
            }
        )

        holder.itemView.setOnClickListener {
            listener.onChecked(item)
        }

        holder.getView<TextView>(R.id.tv_song_check)
            .setOnClickListener {
                listener.onChecked(item)
            }
    }

    private fun getAlbumName(item: Music): String {
        /*
         * 你当前 MusicScanner 中没有给 Music 设置 album 名称，
         * 只有 albumId，所以这里暂时显示 albumId。
         *
         * 如果 Music 实体实际存在 album 字段，
         * 可以直接改成：
         *
         * return item.album ?: "未知专辑"
         */
        return if (item.albumId > 0) {
            "专辑 ${item.albumId}"
        } else {
            "未知专辑"
        }
    }
}