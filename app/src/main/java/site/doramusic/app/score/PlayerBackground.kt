package site.doramusic.app.score

data class PlayerBackground(
    val id: String,
    val galleryId: String,
    val name: String,
    val imageRes: Int,
    val bgType: Int     // 0：系统背景，1：图鉴背景，2：商城背景
) {

    companion object {

        const val BACKGROUND_TYPE_SYSTEM = 0
        const val BACKGROUND_TYPE_GALLERY = 1
        const val BACKGROUND_TYPE_MALL = 2
    }
}