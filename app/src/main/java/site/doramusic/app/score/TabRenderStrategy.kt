package site.doramusic.app.score

import site.doramusic.app.databinding.ActivityPlayerBgSelectBinding

interface TabRenderStrategy {

    fun initTab(
        binding: ActivityPlayerBgSelectBinding,
        titles: List<String>,
        fragments: List<BackgroundListFragment>
    )

    fun onConfigurationChanged()
    
    fun release()
}
