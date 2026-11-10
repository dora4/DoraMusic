package site.doramusic.app.score

data class PlayerBackground(
    // ID，全局唯一
    val id: String,
    // 背景名称
    val name: String,
    // 商城购买的背景才有价格
    val price: Int = 0,
    val imageRes: Int,
    val bgType: Int     // 0：系统背景，1：图鉴背景，2：商城背景
) {

    companion object {

        const val BACKGROUND_TYPE_SYSTEM = 0
        const val BACKGROUND_TYPE_GALLERY = 1
        const val BACKGROUND_TYPE_MALL = 2
    }
}