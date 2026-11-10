package site.doramusic.app.score

import android.content.Context
import dora.util.SPUtils
import dora.util.RxBus
import site.doramusic.app.event.PlayerBackgroundChangedEvent

object PlayerBgManager {

    const val KEY_SELECTED_BACKGROUND = "selected_background"
    @Deprecated("后期移除")
    const val KEY_OLD_GALLERY_BACKGROUND = GalleryBackgroundManager.KEY_SELECTED_GALLERY_BACKGROUND // 老key
    const val DEFAULT_BACKGROUND = "default"

    // 全局背景数据源，Activity初始化时赋值，保证Adapter、播放器查询一致
    var allBgList: MutableList<PlayerBackground> = mutableListOf()

    /**
     * 获取当前选中背景id，默认 default。
     * 兼容老用户：如果新key是default，尝试读取老图鉴背景key做迁移。
     */
    fun getSelectedBgId(context: Context): String {
        val newSelected = SPUtils.readString(context, KEY_SELECTED_BACKGROUND, DEFAULT_BACKGROUND)
        // 新key还没有被用户设置过，才尝试迁移老数据（幂等：已经迁移过就不再走）
        if (newSelected == DEFAULT_BACKGROUND) {
            val oldGalleryId = SPUtils.readString(context, KEY_OLD_GALLERY_BACKGROUND, "")
            if (oldGalleryId.isNotEmpty()) {
                // 老用户有选中图鉴背景，迁移到新key
                SPUtils.writeString(context, KEY_SELECTED_BACKGROUND, oldGalleryId)
                // 迁移之后清除老key，避免后续再次读取老数据
                SPUtils.remove(context, KEY_OLD_GALLERY_BACKGROUND)
                return oldGalleryId
            }
        }
        return newSelected
    }

    /**
     * 选中背景，持久化保存 + 发送刷新事件。
     */
    fun selectBg(context: Context, bgId: String) {
        SPUtils.writeString(context, KEY_SELECTED_BACKGROUND, bgId)
        val targetBg = findBgById(bgId)
        RxBus.getInstance().post(PlayerBackgroundChangedEvent(targetBg?.imageRes))
    }

    /**
     * 根据id查找背景实体。
     */
    fun findBgById(bgId: String): PlayerBackground? {
        if (bgId == DEFAULT_BACKGROUND) return null
        return allBgList.firstOrNull { it.id == bgId }
    }
}
