package site.doramusic.app.score

import android.view.View
import site.doramusic.app.R
import site.doramusic.app.databinding.ItemGalleryBackgroundBinding
import dora.util.SPUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 播放器背景 Adapter。
 */
class GalleryBackgroundAdapter(backgrounds: MutableList<PlayerBgSelectActivity.GalleryBackground>)
    : BaseQuickAdapter<PlayerBgSelectActivity.GalleryBackground, BaseViewHolder>(
    R.layout.item_gallery_background,
    backgrounds
) {

    private var selectedBackground: String = DEFAULT_BACKGROUND

    override fun convert(
        holder: BaseViewHolder,
        item: PlayerBgSelectActivity.GalleryBackground
    ) {
        val binding = ItemGalleryBackgroundBinding.bind(holder.itemView)
        val context = holder.itemView.context
        // 判断是否解锁。
        val unlocked =
            item.id == DEFAULT_BACKGROUND ||
                    SPUtils.readBoolean(
                        context,
                        item.galleryId
                    )
        binding.ivBackground.setImageResource(item.imageRes)
        binding.tvName.text = item.name
        if (unlocked) {
            binding.ivLock.visibility = View.GONE
            binding.tvStatus.visibility = View.VISIBLE
            binding.tvStatus.text = context.getString(R.string.background_unlocked)
            binding.flBackgroundDisplay.alpha = 1f
        } else {
            binding.ivLock.visibility = View.VISIBLE
            binding.tvStatus.visibility = View.VISIBLE
            binding.tvStatus.text = context.getString(R.string.background_locked)
            binding.flBackgroundDisplay.alpha = 0.6f
        }
        // 当前选择状态。
        binding.tvSelected.visibility =
            if (selectedBackground == item.id) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    /**
     * 设置当前背景。
     */
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