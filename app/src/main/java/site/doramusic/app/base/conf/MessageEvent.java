package site.doramusic.app.base.conf;

public class MessageEvent {

    public int what;

    public static final int OPEN_LYRICS = 0x01;

    public MessageEvent(int what) {
        this.what = what;
    }
}
