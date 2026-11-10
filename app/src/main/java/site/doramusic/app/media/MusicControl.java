package site.doramusic.app.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dora.db.builder.WhereBuilder;
import dora.db.dao.DaoFactory;
import dora.db.dao.OrmDao;
import dora.db.table.OrmTable;
import dora.util.LogUtils;
import dora.util.RxBus;
import dora.util.TextUtils;
import dora.util.ToastUtils;
import site.doramusic.app.conf.AppConfig;
import site.doramusic.app.db.Music;
import site.doramusic.app.event.PlayMusicEvent;
import site.doramusic.app.event.RefreshHomeItemEvent;
import site.doramusic.app.event.RefreshStatEvent;
import site.doramusic.app.stat.PlayStatManager;
import site.doramusic.app.util.PrefsManager;

/**
 * 音乐播放流程控制。
 */
public class MusicControl implements MediaPlayer.OnCompletionListener, AppConfig {

    private final Random mRandom;
    private int mPlayMode;
    private final MediaPlayerProxy mMediaPlayer;
    private final List<Music> mPlaylist;
    private final Context mContext;
    private int mCurPlayIndex;
    private int mPlayState;
    private int mPendingProgress;
    private final int mCurMusicId;
    private Music mCurMusic;
    private boolean mPlaying;
    private final AudioManager mAudioManager;
    private final OrmDao<Music> mDao;
    private final PrefsManager mPrefsManager;
    // 用于播放计时
    private long mPlayStartTs;
    private boolean mIsTiming;
    // 暂停时累计的播放时长，同一次播放会话累加
    private long mPausedAccumulatedMs = 0;
    // 最小有效播放时长，过滤误触（太短不统计，单位ms）
    private static final long MIN_VALID_PLAY_MS = 3000;

    public MusicControl(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mPrefsManager = new PrefsManager(context);
        this.mPlayMode = MPM_PLAYLIST_LOOP;    // 默认列表循环
        this.mPlayState = MPS_NO_FILE;  // 默认没有音频文件播放
        this.mCurPlayIndex = -1;
        this.mCurMusicId = -1;
        this.mPlaylist = new ArrayList<>();
        this.mDao = DaoFactory.INSTANCE.getDao(Music.class);
        this.mMediaPlayer = new MediaPlayerProxy();
        this.mMediaPlayer.setNeedCacheAudio(true);
        this.mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK); // 播放音频的时候加锁，防止CPU休眠
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        this.mMediaPlayer.setAudioAttributes(attrs);
        this.mMediaPlayer.setOnCompletionListener(this);
        this.mRandom = new Random();
        this.mRandom.setSeed(System.currentTimeMillis());
    }

    /**
     * 结算当前歌曲已播放时长。
     * 调用时机：切歌、播放完成、停止、退出。
     * ⚠️ 暂停不要调用这个！
     */
    private void commitCurrentPlayDuration() {
        if (mCurMusic == null) {
            resetTimer();
            return;
        }
        long totalPlayed = mPausedAccumulatedMs;
        if (mIsTiming) {
            totalPlayed += System.currentTimeMillis() - mPlayStartTs;
        }
        if (totalPlayed < MIN_VALID_PLAY_MS) {
            resetTimer();
            return;
        }
        // 调用新增的中途记录方法
        PlayStatManager.INSTANCE.addPartialPlayRecord(
                mCurMusic.songId,
                mCurMusic.musicName,
                mCurMusic.duration, // 歌曲原始全长
                totalPlayed
        );
        resetTimer();
        RxBus.getInstance().post(new RefreshStatEvent());
    }

    /**
     * 重置计时器，清空本次会话计时数据。
     */
    private void resetTimer() {
        mIsTiming = false;
        mPausedAccumulatedMs = 0;
        mPlayStartTs = 0;
    }

    /**
     * 设置重低音参数。
     *
     * @param strength
     */
    private BassBoost bassBoost;

    public void setBassBoost(int strength) {
        try {
            if (mMediaPlayer == null) return;
            int audioSessionId = mMediaPlayer.getAudioSessionId();
            if (audioSessionId == AudioManager.AUDIO_SESSION_ID_GENERATE || audioSessionId < 0) {
                LogUtils.e("BassBoost 初始化失败：无效的 audioSessionId");
                return;
            }
            // 避免重复创建
            if (bassBoost != null) {
                bassBoost.release();
                bassBoost = null;
            }
            bassBoost = new BassBoost(0, audioSessionId);
            if (bassBoost.getStrengthSupported()) {
                BassBoost.Settings settings = new BassBoost.Settings();
                settings.strength = (short) Math.max(0, Math.min(1000, strength)); // 限制范围
                bassBoost.setProperties(settings);
            }
            bassBoost.setEnabled(true);
            bassBoost.setParameterListener((effect, status, param, value) -> {
                LogUtils.i("重低音参数改变");
            });
            LogUtils.i("BassBoost 初始化成功，强度=" + strength);
        } catch (Exception e) {
            LogUtils.e("BassBoost 初始化异常：" + e.getMessage());
        }
    }

    /**
     * 获取均衡器支持的频率。
     *
     * @return
     */
    public int[] getEqualizerFreq() {
        int audioSessionId = mMediaPlayer.getAudioSessionId();
        Equalizer equalizer = new Equalizer(0, audioSessionId);
        short bands = equalizer.getNumberOfBands();
        int[] freqs = new int[bands];
        for (short i = 0; i < bands; i++) {
            int centerFreq = equalizer.getCenterFreq(i) / 1000;
            freqs[i] = centerFreq;
        }
        return freqs;
    }

    /**
     * 设置均衡器。
     *
     * @param bandLevels
     */
    public void setEqualizer(int[] bandLevels) {
        Equalizer equalizer = getEqualizer(bandLevels);
        if (equalizer != null) {
            equalizer.setEnabled(true);
            equalizer.setParameterListener(new Equalizer.OnParameterChangeListener() {
                @Override
                public void onParameterChange(Equalizer effect, int status, int param1, int param2, int value) {
                    LogUtils.i("均衡器参数改变:" + status + "," + param1 + "," + param2 + "," + value);
                }
            });
        }
    }

    private Equalizer getEqualizer(int[] bandLevels) {
        int audioSessionId = mMediaPlayer.getAudioSessionId();
        if (audioSessionId == AudioManager.AUDIO_SESSION_ID_GENERATE || audioSessionId < 0) {
            LogUtils.e("Equalizer 初始化失败：无效的 audioSessionId");
            return null;
        }
        Equalizer equalizer = new Equalizer(1, audioSessionId);
        // 获取均衡控制器支持最小值和最大值
        short minEQLevel = equalizer.getBandLevelRange()[0];  // 第一个下标为最低的限度范围
        short maxEQLevel = equalizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
        int distanceEQLevel = maxEQLevel - minEQLevel;
        int singleEQLevel = distanceEQLevel / 25;
        for (short i = 0; i < bandLevels.length; i++) {
            equalizer.setBandLevel(i, (short) (singleEQLevel * bandLevels[i]));
        }
        return equalizer;
    }

    /**
     * 保存收藏。
     *
     * @param music
     */
    private void saveFavorite(Music music) {
        music.favorite = 1;
        mDao.update(WhereBuilder.Companion.create().addWhereEqualTo(OrmTable.INDEX_ID, music.id), music);
    }

    /**
     * 保存最近播放。
     *
     * @param music
     */
    private void saveLatest(Music music) {
        // 更新本地缓存歌曲
        music.lastPlayTime = System.currentTimeMillis();
        mDao.update(WhereBuilder.Companion.create().addWhereEqualTo(OrmTable.INDEX_ID, music.id), music);
    }

    /**
     * 设置播放。
     *
     * @param playState
     */
    public void setPlaying(int playState) {
        switch (playState) {
            case MPS_PLAYING:
                mPlaying = true;
                break;
            default:
                mPlaying = false;
        }
    }

    /**
     * 设置当前播放的歌曲。
     *
     * @param music
     * @return
     */
    public boolean loadCurMusic(Music music) {
        if (prepare(seekPosById(mPlaylist, music.songId))) {
            this.mCurMusic = music;
            return true;
        }
        return false;
    }

    /**
     * 修改当前播放歌曲的信息。
     *
     * @param music
     * @return
     */
    public void setCurMusic(Music music) {
        this.mPlaylist.set(mCurPlayIndex, music);
        this.mCurMusic = music;
    }

    /**
     * 缓冲准备。
     *
     * @param pos
     * @return
     */
    public boolean prepare(int pos) {
        if (pos == -1 || pos >= mPlaylist.size()) {
            return false;
        }
        // 切歌前先结算上一首
        commitCurrentPlayDuration();
        mCurPlayIndex = pos;
        mPendingProgress = 0;
        mMediaPlayer.reset();
        if (mPrefsManager.getBassBoost()) {
            setBassBoost(1000);
        } else {
            setBassBoost(1);
        }
        if (!mPrefsManager.getEqualizerDecibels().isEmpty()) {
            try {
                String[] values = mPrefsManager.getEqualizerDecibels().split(",");
                int[] equalizerFreq = getEqualizerFreq();
                int[] decibels = new int[equalizerFreq.length];
                for (int i = 0; i < decibels.length; i++) {
                    decibels[i] = Integer.parseInt(values[i]);
                }
                setEqualizer(decibels);
            } catch (Exception ignore) {
            }
        }
        String path = mPlaylist.get(pos).data;
        if (TextUtils.isNotEmpty(path)) {
            try {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
                mPlayState = MPS_PREPARE;
            } catch (Exception e) {
                mPlayState = MPS_INVALID;
                if (pos < mPlaylist.size()) {
                    pos++;
                    if (pos < mPlaylist.size()) {
                        playById(mPlaylist.get(pos).songId);
                    }
                }
                return false;
            }
        } else {
            ToastUtils.showShort(mContext, "歌曲路径为空");
        }
        mCurMusic = mPlaylist.get(mCurPlayIndex);
        sendPlayMusicEvent();
        return true;
    }

    /**
     * 根据歌曲的id来播放。
     *
     * @param id
     * @return
     */
    public boolean playById(int id) {
        if (requestFocus()) {
            int position = seekPosById(mPlaylist, id);
            if (position == -1) {
                return false;
            }
            mCurPlayIndex = position;
            if (mCurMusicId == id) {
                if (!mMediaPlayer.isPlaying()) {
                    // 从暂停恢复播放，重新开启计时
                    mPlayStartTs = System.currentTimeMillis();
                    mIsTiming = true;

                    mMediaPlayer.start();
                    mPlayState = MPS_PLAYING;
                    mCurMusic = mPlaylist.get(mCurPlayIndex);
                    saveLatest(mCurMusic);
                    sendPlayMusicEvent();
                } else {
                    pause();
                }
                return true;
            }
            if (!prepare(position)) {
                return false;
            }
            return replay();
        } else {
            return false;
        }
    }

    /**
     * 根据URL播放歌曲。
     *
     * @param music
     * @param url
     */
    public void playByUrl(Music music, String url) {
        if (requestFocus()) {
            try {
                mMediaPlayer.setAudioCachePath(music.data);
                mMediaPlayer.setOnCachedProgressUpdateListener(new MediaPlayerProxy.OnCachedProgressUpdateListener() {
                    @Override
                    public void updateCachedProgress(int progress) {
                        mPendingProgress = progress;
                    }
                });
                String localProxyUrl = mMediaPlayer.getLocalURLAndSetRemoteSocketAddress(url);
                mPlaylist.add(mCurPlayIndex, music);    // 插入到当前播放位置
                mCurMusic = music;
                mMediaPlayer.startProxy();
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(localProxyUrl);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.start();
                mPlayState = MPS_PLAYING;
                // 开始计时
                mPlayStartTs = System.currentTimeMillis();
                mIsTiming = true;
                mPausedAccumulatedMs = 0;
                sendPlayMusicEvent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据本地文件路径播放歌曲。
     *
     * @param path
     */
    public void play(String path) {
        if (requestFocus()) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnPreparedListener(mp -> {
                    mMediaPlayer.start();
                    mPlayState = MPS_PLAYING;
                    mPlayStartTs = System.currentTimeMillis();
                    mIsTiming = true;
                    mPausedAccumulatedMs = 0;
                    sendPlayMusicEvent();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放歌曲。
     */
    public void stop() {
        // 停止前结算时长
        commitCurrentPlayDuration();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // 临时失去焦点，只暂停，不结算
                pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                replay();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(audioFocusListener);
                // 永久失去焦点，结算
                commitCurrentPlayDuration();
                pause();
            }
        }
    };

    /**
     * 请求音频焦点。
     *
     * @return
     */
    private boolean requestFocus() {
        // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(audioFocusListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 根据位置播放列表中的歌曲。
     *
     * @param pos
     * @return
     */
    public boolean play(int pos) {
        if (requestFocus()) {
            if (mCurPlayIndex == pos) {
                if (!mMediaPlayer.isPlaying()) {
                    // 暂停后恢复播放
                    mPlayStartTs = System.currentTimeMillis();
                    mIsTiming = true;

                    mMediaPlayer.start();
                    mPlayState = MPS_PLAYING;
                    mCurMusic = mPlaylist.get(mCurPlayIndex);
                    saveLatest(mCurMusic);
                    sendPlayMusicEvent();
                } else {
                    pause();
                }
                return true;
            }
            if (!prepare(pos)) {
                return false;
            }
            return replay();
        } else {
            return false;
        }
    }

    /**
     * 获取当前播放歌曲的索引。
     *
     * @return
     */
    public int getCurPlayIndex() {
        return mCurPlayIndex;
    }

    /**
     * 保证索引在播放列表索引范围内。
     *
     * @param index
     * @return
     */
    private int reviseIndex(int index) {
        if (index < 0) {
            index = mPlaylist.size() - 1;
        }
        if (index >= mPlaylist.size()) {
            index = 0;
        }
        return index;
    }

    /**
     * 获取当前歌曲播放的位置。
     *
     * @return
     */
    public int position() {
        if (mPlayState == MPS_PLAYING || mPlayState == MPS_PAUSE) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取当前歌曲的时长。
     *
     * @return 毫秒
     */
    public int duration() {
        if (mPlayState == MPS_INVALID || mPlayState == MPS_NO_FILE) {
            return 0;
        }
        return mMediaPlayer.getDuration();
    }

    /**
     * 跳到指定进度播放歌曲。
     *
     * @param progress
     * @return
     */
    public boolean seekTo(int progress) {
        if (mPlayState == MPS_INVALID || mPlayState == MPS_NO_FILE) {
            return false;
        }
        int pro = reviseSeekValue(progress);
        int time = mMediaPlayer.getDuration();
        int curTime = (int) ((float) pro / 100 * time);
        mMediaPlayer.seekTo(curTime);
        // seek跳转之后，计时起点重置，不影响累计
        mPlayStartTs = System.currentTimeMillis();
        return true;
    }

    /**
     * 获取歌曲的播放模式。
     *
     * @return
     */
    public int getPlayMode() {
        return mPlayMode;
    }

    /**
     * 设置歌曲的播放模式。
     *
     * @param mode
     */
    public void setPlayMode(int mode) {
        this.mPlayMode = mode;
    }

    /**
     * 清空播放列表。
     */
    public void clear() {
        commitCurrentPlayDuration();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    /**
     * 在线缓冲进度。
     *
     * @return
     */
    public int pendingProgress() {
        return mPendingProgress;
    }

    public interface OnConnectCompletionListener {

        void onConnectCompletion(IMediaService service);
    }

    /**
     * 获取当前正在播放的歌曲。
     *
     * @return
     */
    public Music getCurMusic() {
        return mCurMusic;
    }

    /**
     * 检测当前歌曲是否正在播放中。
     *
     * @return
     */
    public boolean isPlaying() {
        return mPlaying;
    }

    /**
     * 暂停当前歌曲的播放。暂停只累加时长，不入库。
     *
     * @return
     */
    public boolean pause() {
        if (mPlayState != MPS_PLAYING) {
            return false;
        }
        // 把当前这段播放时间加到累计，不commit入库
        if (mIsTiming) {
            mPausedAccumulatedMs += System.currentTimeMillis() - mPlayStartTs;
            mIsTiming = false;
        }
        mMediaPlayer.pause();
        mPlayState = MPS_PAUSE;
        mCurMusic = mPlaylist.get(mCurPlayIndex);
        sendPlayMusicEvent();
        return true;
    }

    /**
     * 播放上一首。
     *
     * @return
     */
    public boolean prev() {
        // 先结算当前这首的播放时长
        commitCurrentPlayDuration();
        switch (mPlayMode) {
            case AppConfig.MPM_PLAYLIST_LOOP:    // 列表循环
                return moveLeft();
            case AppConfig.MPM_SEQUENTIAL_PLAYBACK:    // 顺序播放
                if (mCurPlayIndex != 0) {
                    return moveLeft();
                } else {
                    return prepare(mCurPlayIndex);
                }
            case AppConfig.MPM_SHUFFLE_PLAYBACK:   // 随机播放
                int index = getRandomIndex();
                if (index != -1) {
                    mCurPlayIndex = index;
                } else {
                    mCurPlayIndex = 0;
                }
                if (prepare(mCurPlayIndex)) {
                    return replay();
                }
                return false;
            case MPM_SINGLE_TRACK_LOOP:  // 单曲循环
                prepare(mCurPlayIndex);
                return replay();
            default:
                return false;
        }
    }

    /**
     * 播放下一首。
     *
     * @return
     */
    public boolean next() {
        // 先结算当前这首的播放时长
        commitCurrentPlayDuration();
        switch (mPlayMode) {
            case MPM_PLAYLIST_LOOP:    // 列表循环
                return moveRight();
            case MPM_SEQUENTIAL_PLAYBACK:    // 顺序播放
                if (mCurPlayIndex != mPlaylist.size() - 1) {
                    return moveRight();
                } else {
                    return prepare(mCurPlayIndex);
                }
            case MPM_SHUFFLE_PLAYBACK:   // 随机播放
                int index = getRandomIndex();
                if (index != -1) {
                    mCurPlayIndex = index;
                } else {
                    mCurPlayIndex = 0;
                }
                if (prepare(mCurPlayIndex)) {
                    return replay();
                }
                return false;
            case MPM_SINGLE_TRACK_LOOP:  // 单曲循环
                prepare(mCurPlayIndex);
                return replay();
            default:
                return false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 整首播放完毕，结算时长并计入完整播放记录
        commitCurrentPlayDuration();
        if (mCurMusic != null) {
            PlayStatManager.INSTANCE.insertPlayRecord(mCurMusic.songId, mCurMusic.musicName,
                    mCurMusic.duration);
        }
        RxBus.getInstance().post(new RefreshStatEvent());
        next();
    }

    /**
     * 随机播放模式下获取播放索引。
     *
     * @return
     */
    private int getRandomIndex() {
        int size = mPlaylist.size();
        if (size == 0) {
            return -1;
        }
        return Math.abs(mRandom.nextInt() % size);
    }

    /**
     * 修正缓冲播放的进度在合理的范围内。
     *
     * @param progress
     * @return
     */
    private int reviseSeekValue(int progress) {
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        return progress;
    }

    /**
     * 刷新播放列表的歌曲。
     *
     * @param playlist
     */
    public void refreshPlaylist(List<Music> playlist) {
        mPlaylist.clear();
        mPlaylist.addAll(playlist);
        if (mPlaylist.isEmpty()) {
            mPlayState = MPS_NO_FILE;
            mCurPlayIndex = -1;
        }
    }

    /**
     * 在当前播放模式下播放上一首。
     *
     * @return
     */
    public boolean moveLeft() {
        if (mPlayState == MPS_NO_FILE) {
            return false;
        }
        mCurPlayIndex--;
        mCurPlayIndex = reviseIndex(mCurPlayIndex);
        if (!prepare(mCurPlayIndex)) {
            return false;
        }
        return replay();
    }

    /**
     * 在当前播放模式下播放下一首。
     *
     * @return
     */
    public boolean moveRight() {
        if (mPlayState == MPS_NO_FILE) {
            return false;
        }
        mCurPlayIndex++;
        mCurPlayIndex = reviseIndex(mCurPlayIndex);
        if (!prepare(mCurPlayIndex)) {
            return false;
        }
        return replay();
    }

    /**
     * 重头开始播放当前歌曲。
     *
     * @return
     */
    public boolean replay() {
        if (requestFocus()) {
            if (mPlayState == MPS_INVALID || mPlayState == MPS_NO_FILE) {
                return false;
            }
            mMediaPlayer.start();
            mPlayState = MPS_PLAYING;
            mCurMusic = mPlaylist.get(mCurPlayIndex);
            saveLatest(mCurMusic);
            // 歌曲重新开始播放，重置起始时间，开启计时，清空累计
            mPlayStartTs = System.currentTimeMillis();
            mIsTiming = true;
            mPausedAccumulatedMs = 0;
            sendPlayMusicEvent();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送音乐播放/暂停的广播。
     */
    private void sendPlayMusicEvent() {
        setPlaying(mPlayState);
        RxBus.getInstance().post(new PlayMusicEvent(mPlayState, mPendingProgress));
        RxBus.getInstance().post(new RefreshHomeItemEvent());
    }

    /**
     * 获取当前的播放状态。
     *
     * @return
     */
    public int getPlayState() {
        return mPlayState;
    }

    /**
     * 获取播放列表。
     *
     * @return
     */
    public List<Music> getPlaylist() {
        return mPlaylist;
    }

    /**
     * 退出媒体播放。
     */
    public void exit() {
        commitCurrentPlayDuration();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mCurPlayIndex = -1;
        mPlaylist.clear();
    }

    /**
     * 根据歌曲的ID，寻找出歌曲在当前播放列表中的位置。
     *
     * @param playlist
     * @param id
     * @return
     */
    public int seekPosById(List<Music> playlist, int id) {
        if (id == -1) {
            return -1;
        }
        int result = -1;
        if (playlist != null) {
            for (int i = 0; i < playlist.size(); i++) {
                if (id == playlist.get(i).songId) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }
}