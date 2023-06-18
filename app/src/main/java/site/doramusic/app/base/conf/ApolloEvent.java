package site.doramusic.app.base.conf;

public interface ApolloEvent {

    /**
     * 打开歌词滚动界面。
     */
    String OPEN_SLIDING_DRAWER = "open_sliding_drawer";

    /**
     * 刷新本地歌曲数量。
     */
    String REFRESH_LOCAL_NUMS = "refresh_local_nums";

    /**
     * 此消息要在主进程发送和接收。
     */
    String REFRESH_MUSIC_PLAY_LIST = "refresh_music_play_list";
}
