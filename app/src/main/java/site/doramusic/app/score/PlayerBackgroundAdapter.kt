package site.doramusic.app.score

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.util.RxBus
import dora.util.SPUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import site.doramusic.app.R
import site.doramusic.app.event.PlayerBackgroundChangedEvent
import site.doramusic.app.util.MusicTimer

class PlayerBackgroundAdapter(
    context: Context,
    backgrounds: MutableList<PlayerBackground>
) : BaseQuickAdapter<PlayerBackground, BaseViewHolder>(
    R.layout.item_player_background,
    backgrounds
) {

    private var selectedBackground: String = PlayerBgManager.getSelectedBgId(context)
    private var disposable: Disposable? = null

    init {
        disposable = RxBus.getInstance()
            .toObservable(PlayerBackgroundChangedEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val newId = PlayerBgManager.getSelectedBgId(context)
                setSelectedBackground(newId)
            }
    }

    override fun convert(
        holder: BaseViewHolder,
        item: PlayerBackground
    ) {
        val itemView = holder.itemView
        val context = itemView.context
        val ivBackground = itemView.findViewById<ImageView>(R.id.ivBackground)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val ivLock = itemView.findViewById<ImageView>(R.id.ivLock)
        val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
        val flBackgroundDisplay = itemView.findViewById<FrameLayout>(R.id.flBackgroundDisplay)
        val tvSelected = itemView.findViewById<TextView>(R.id.tvSelected)
        val totalMinutes = SPUtils.readInteger(context, MusicTimer.KEY_TOTAL_PLAY_MINUTES, 0)
        val requireMinutes = getSystemBgRequireMinutes(item.id)
        val unlocked = when (item.bgType) {
            PlayerBackground.BACKGROUND_TYPE_SYSTEM -> {
                totalMinutes >= requireMinutes
            }
            PlayerBackground.BACKGROUND_TYPE_MALL,
            PlayerBackground.BACKGROUND_TYPE_GALLERY -> {
                SPUtils.readBoolean(context, item.id)
            }
            else -> false
        }
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
            flBackgroundDisplay.alpha = 1f
            tvStatus.text = context.getString(R.string.background_available)
        } else {
            ivLock.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            flBackgroundDisplay.alpha = 0.6f
            when (item.bgType) {
                PlayerBackground.BACKGROUND_TYPE_MALL -> {
                    tvStatus.text = context.getString(R.string.bg_mall_cost_format, item.price)
                }
                PlayerBackground.BACKGROUND_TYPE_SYSTEM -> {
                    tvStatus.text = context.getString(R.string.bg_system_unlock_rule, requireMinutes)
                }
                else -> {
                    tvStatus.text = context.getString(R.string.background_locked)
                }
            }
        }
        tvSelected.visibility = if (selectedBackground == item.id) View.VISIBLE else View.GONE
    }

    private fun getSystemBgRequireMinutes(bgId: String): Int {
        return when (bgId) {
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_1 -> 1
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_2 -> 10
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_3 -> 60
            PlayerBgFactory.PlayerBgId.SYSTEM_BACKGROUND_4 -> 120
            else -> Int.MAX_VALUE
        }
    }

    fun setSelectedBackground(id: String) {
        if (selectedBackground == id) return
        val oldSelected = selectedBackground
        selectedBackground = id
        val oldIndex = data.indexOfFirst { it.id == oldSelected }
        val newIndex = data.indexOfFirst { it.id == selectedBackground }
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    fun dispose() {
        disposable?.dispose()
    }
}
