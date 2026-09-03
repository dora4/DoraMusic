package site.doramusic.app.score

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.util.SPUtils
import site.doramusic.app.R

class GalleryBackgroundAdapter(
    backgrounds: MutableList<PlayerBgSelectActivity.GalleryBackground>,
    private var selectedBackground: String = DEFAULT_BACKGROUND
) : BaseQuickAdapter<PlayerBgSelectActivity.GalleryBackground, BaseViewHolder>(
    R.layout.item_gallery_background,
    backgrounds
) {

    override fun convert(
        holder: BaseViewHolder,
        item: PlayerBgSelectActivity.GalleryBackground
    ) {
        val itemView = holder.itemView
        val context = itemView.context
        val ivBackground =
            itemView.findViewById<ImageView>(R.id.ivBackground)
        val tvName =
            itemView.findViewById<TextView>(R.id.tvName)
        val ivLock =
            itemView.findViewById<ImageView>(R.id.ivLock)
        val tvStatus =
            itemView.findViewById<TextView>(R.id.tvStatus)
        val flBackgroundDisplay =
            itemView.findViewById<FrameLayout>(R.id.flBackgroundDisplay)
        val tvSelected =
            itemView.findViewById<TextView>(R.id.tvSelected)
        val unlocked =
            item.id == DEFAULT_BACKGROUND ||
                    SPUtils.readBoolean(
                        context,
                        item.galleryId
                    )
        if (item.imageRes != 0) {
            ivBackground.visibility = View.VISIBLE
            ivBackground.setImageResource(item.imageRes)
        } else {
            ivBackground.visibility = View.GONE
        }
        tvName.text = item.name
        if (unlocked) {
            ivLock.visibility = View.GONE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = context.getString(R.string.background_unlocked)
            flBackgroundDisplay.alpha = 1f
        } else {
            ivLock.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = context.getString(R.string.background_locked)
            flBackgroundDisplay.alpha = 0.6f
        }
        tvSelected.visibility =
            if (selectedBackground == item.id) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    fun setSelectedBackground(id: String) {
        if (selectedBackground == id) {
            return
        }
        val oldSelected = selectedBackground
        selectedBackground = id
        val oldIndex = data.indexOfFirst {
            it.id == oldSelected
        }
        val newIndex = data.indexOfFirst {
            it.id == selectedBackground
        }
        if (oldIndex != -1) {
            notifyItemChanged(oldIndex)
        }
        if (newIndex != -1) {
            notifyItemChanged(newIndex)
        }
    }

    companion object {
        private const val DEFAULT_BACKGROUND = "default"
    }
}