package site.doramusic.app.ui.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder

import site.doramusic.app.R
import site.doramusic.app.db.Folder

class FolderItemAdapter(list: MutableList<Folder>) : BaseSortItemAdapter<Folder>(R.layout.item_folder, list) {

    override fun getSortKey(data: Folder): String {
        return data.name
    }

    override fun convert(holder: BaseViewHolder, item: Folder) {
        holder.setText(R.id.tv_folder_name, item.name)
        holder.setText(R.id.tv_folder_path, item.path)
    }
}
