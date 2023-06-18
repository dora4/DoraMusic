package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.db.Album

class AlbumItemAdapter(list: MutableList<Album>) : BaseSortItemAdapter<Album>(R.layout.item_album, list) {

    override fun getSortKey(data: Album): String {
        return data.album_name
    }

    override fun convert(holder: BaseViewHolder, album: Album) {
        holder.setText(R.id.tv_album_name, album.album_name)
        holder.setText(R.id.tv_album_number_of_songs, "${album.number_of_songs}首")
    }
}
