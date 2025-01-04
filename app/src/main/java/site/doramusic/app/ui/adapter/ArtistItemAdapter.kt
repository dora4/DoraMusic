package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.db.Artist

class ArtistItemAdapter(list: MutableList<Artist>) : BaseSortItemAdapter<Artist>(R.layout.item_artist, list) {

    override fun getSortKey(data: Artist): String {
        return data.name
    }

    override fun convert(holder: BaseViewHolder, item: Artist) {
        holder.setText(R.id.tv_artist_name, item.name)
        holder.setText(R.id.tv_artist_number_of_tracks,
            String.format(context.getString(R.string.items), item.number_of_tracks))
    }
}
