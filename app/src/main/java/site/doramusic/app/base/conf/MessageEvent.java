package site.doramusic.app.base.conf;

public class MessageEvent {

    public int what;

    public static final int REFRESH_MUSIC_INFOS = 0x01;

    public MessageEvent(int what) {
        this.what = what;
    }
}
