package site.doramusic.app.base.conf

interface ApolloEvent {

    companion object {

        /**
         * 刷新播放列表。
         */
        const val REFRESH_MUSIC_PLAY_LIST = "refresh_music_play_list"
    }
}