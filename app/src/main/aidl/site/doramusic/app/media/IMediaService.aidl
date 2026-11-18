// IMediaService.aidl
package site.doramusic.app.media;

import site.doramusic.app.db.Music;
import android.graphics.Bitmap;

interface IMediaService {

    boolean play(int pos);
    boolean playById(int id);
    void playByPath(String path);
    void playByUrl(in Music music, String url);
    boolean replay();
    boolean pause();
    /** 拖动进度条期间为 true：暂停只累加时长，不翻转播放状态，避免松手回弹。 */
    boolean resume();
    boolean prev();
    boolean next();
    void stop();
    int duration();
    int position();
    int pendingProgress();
    boolean seekTo(int progress);
    /** 拖动进度条闸门，由 UI 层在 onStart/onStopTrackingTouch 中调用。 */
    void setSeeking(boolean seeking);
    boolean isSeeking();
    void refreshPlaylist(in List<Music> playlist);
    /** 从播放队列移除指定歌曲（不停止当前播放）。 */
    boolean removeFromPlaylist(in Music music);

    void setBassBoost(int strength);
    void setEqualizer(in int[] bandLevels);
    int[] getEqualizerFreq();

    int getPlayState();
    int getPlayMode();
    void setPlayMode(int mode);
    int getCurMusicId();
    boolean loadCurMusic(in Music music);
    void setCurMusic(in Music music);
    Music getCurMusic();
    List<Music> getPlaylist();
    boolean isPlaying();
    void updateNotification(in Bitmap bitmap, String title, String name);
    void updateFavorite(int favorite);
    void cancelNotification();
}