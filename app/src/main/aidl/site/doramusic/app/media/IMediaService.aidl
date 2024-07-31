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
    boolean prev();
    boolean next();
    void stop();
    int duration();
    int position();
    int pendingProgress();
    boolean seekTo(int progress);
    void refreshPlaylist(in List<Music> playlist);

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
    void updateNotification(in Bitmap bitmap, String title, String name);
    void cancelNotification();
}
