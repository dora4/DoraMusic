package site.doramusic.app.search

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import site.doramusic.app.R
import site.doramusic.app.db.Music

/**
 * 本地音乐扫描结果 Adapter。
 *
 * 注意：
 * 扫描阶段的 Music 还没有写入本地 music 数据库，
 * 所以不能使用 Music.id 判断选中状态。
 *
 * 使用 Music.data（歌曲文件绝对路径）作为唯一 Key。
 */
class MusicResultAdapter(
    list: MutableList<Music>,
    private val selectedIds: Set<String>,
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

        /**
         * 歌曲名称。
         */
        holder.setText(
            R.id.tv_song_title,
            item.musicName ?: ContextCompat.getString(context, R.string.unknown)
        )

        /**
         * 歌手。
         */
        holder.setText(
            R.id.tv_song_subtitle,
            item.artist ?: ContextCompat.getString(context, R.string.unknown)
        )

        /**
         * 使用文件路径判断选中状态。
         *
         * 不使用 item.id。
         */
        val key = item.data

        val selected =
            !key.isNullOrBlank() &&
                    selectedIds.contains(key)

        /**
         * 更新按钮文字。
         */
        holder.setText(
            R.id.tv_song_check,
            if (selected) {
                context.getString(R.string.selected)
            } else {
                context.getString(R.string.added)
            }
        )

        /**
         * 点击整行。
         */
        holder.itemView.setOnClickListener {
            listener.onChecked(item)
        }

        /**
         * 点击添加 / 已选择按钮。
         */
        holder.getView<TextView>(
            R.id.tv_song_check
        ).setOnClickListener {
            listener.onChecked(item)
        }
    }
}