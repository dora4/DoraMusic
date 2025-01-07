package site.doramusic.app.ui.layout

/**
 * 提供一种可以打开可滑动的播放控制与歌词界面的能力。
 */
interface ILyricDrawer {

    /**
     * 打开底部抽屉界面。
     */
    fun showDrawer()

    /**
     * 关闭底部抽屉界面。
     */
    fun closeDrawer()
}