package site.doramusic.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

import dora.util.GlobalContext;
import dora.util.SPUtils;
import site.doramusic.app.conf.AppConfig;
import site.doramusic.app.media.MediaManager;
import site.doramusic.app.score.PointsManager;
import site.doramusic.app.score.PointsSource;

/**
 * 音乐播放定时器。
 * 功能：
 * 1. 定时刷新播放进度
 * 2. 统计有效播放时长（音符）
 * 3. 每分钟自动奖励音符
 * 4. 全局累计听歌分钟，用于解锁播放器系统背景
 * SPUtils 只支持 int/string/boolean，碎片时间改用秒（int）存储
 */
public class MusicTimer {

    /**
     * 刷新进度条事件。
     */
    public static final int REFRESH_PROGRESS_EVENT = 0x100;

    /**
     * 定时器刷新间隔（0.5秒）。
     */
    private static final int INTERVAL_TIME = 500;

    /**
     * 每多少毫秒奖励一次积分（1分钟）。
     */
    private static final long REWARD_INTERVAL = 60_000L;

    /**
     * 每次奖励音符数量。
     */
    private static final int REWARD_POINTS = 1;

    private static final int ONE_MINUTE_SEC = 60;

    public static final String KEY_TOTAL_PLAY_MINUTES = "total_play_minutes";
    private static final String KEY_PENDING_SECONDS = "pending_seconds";

    private static final int UNLOCK_BG1_MIN = 1;
    private static final int UNLOCK_BG2_MIN = 10;
    private static final int UNLOCK_BG3_MIN = 60;
    private static final int UNLOCK_BG4_MIN = 120;

    public static final String SYSTEM_BACKGROUND_1 = "system_bg_1";
    public static final String SYSTEM_BACKGROUND_2 = "system_bg_2";
    public static final String SYSTEM_BACKGROUND_3 = "system_bg_3";
    public static final String SYSTEM_BACKGROUND_4 = "system_bg_4";

    private final Handler[] mHandler;
    private final Timer mTimer;
    private TimerTask mTimerTask;
    private final int what;

    private boolean mTimerStart = false;
    private long playDuration = 0L;

    private int totalPlayMinutes;

    /**
     * 碎片秒数 0 ~ 59。
     */
    private int pendingSeconds;

    /**
     * 半秒计数器：0=第一次500ms，1=第二次500ms，凑成1秒。
     */
    private int halfTick = 0;

    public MusicTimer(Handler... handler) {
        this.mHandler = handler;
        this.what = REFRESH_PROGRESS_EVENT;
        this.mTimer = new Timer();
        Context context = GlobalContext.get();
        totalPlayMinutes = SPUtils.readInteger(context, KEY_TOTAL_PLAY_MINUTES, 0);
        pendingSeconds = SPUtils.readInteger(context, KEY_PENDING_SECONDS, 0);
    }

    /**
     * 启动定时器。
     */
    public void startTimer() {
        if (mHandler == null || mTimerStart) {
            return;
        }
        mTimerTask = new MusicTimerTask();
        mTimer.schedule(mTimerTask, INTERVAL_TIME, INTERVAL_TIME);
        mTimerStart = true;
    }

    /**
     * 停止定时器。
     */
    public void stopTimer() {
        if (!mTimerStart) {
            return;
        }
        mTimerStart = false;
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        Context context = GlobalContext.get();
        SPUtils.writeInteger(context, KEY_PENDING_SECONDS, pendingSeconds);
    }

    /**
     * 获取累计播放时长（毫秒）。
     */
    public long getPlayDuration() {
        return playDuration;
    }

    /**
     * 重置播放时长。
     */
    public void resetPlayDuration() {
        playDuration = 0L;
    }

    public int getTotalPlayMinutes() {
        return totalPlayMinutes;
    }

    class MusicTimerTask extends TimerTask {
        @Override
        public void run() {
            if (MediaManager.INSTANCE.getPlayState() == AppConfig.MPS_PLAYING) {
                playDuration += INTERVAL_TIME;
                // 每分钟奖励一次音符
                if (playDuration >= REWARD_INTERVAL) {
                    playDuration = 0L;
                    PointsManager.INSTANCE.addPoints(
                            PointsSource.LISTEN_MUSIC.desc,
                            REWARD_POINTS
                    );
                }
                Context context = GlobalContext.get();
                halfTick++;
                if (halfTick >= 2) {
                    halfTick = 0;
                    pendingSeconds++;
                    if (pendingSeconds >= ONE_MINUTE_SEC) {
                        pendingSeconds = 0;
                        totalPlayMinutes++;
                        SPUtils.writeInteger(context, KEY_TOTAL_PLAY_MINUTES, totalPlayMinutes);
                        // 解锁判断
                        if (totalPlayMinutes >= UNLOCK_BG1_MIN && !SPUtils.readBoolean(context, SYSTEM_BACKGROUND_1, false)) {
                            SPUtils.writeBoolean(context, SYSTEM_BACKGROUND_1, true);
                        }
                        if (totalPlayMinutes >= UNLOCK_BG2_MIN && !SPUtils.readBoolean(context, SYSTEM_BACKGROUND_2, false)) {
                            SPUtils.writeBoolean(context, SYSTEM_BACKGROUND_2, true);
                        }
                        if (totalPlayMinutes >= UNLOCK_BG3_MIN && !SPUtils.readBoolean(context, SYSTEM_BACKGROUND_3, false)) {
                            SPUtils.writeBoolean(context, SYSTEM_BACKGROUND_3, true);
                        }
                        if (totalPlayMinutes >= UNLOCK_BG4_MIN && !SPUtils.readBoolean(context, SYSTEM_BACKGROUND_4, false)) {
                            SPUtils.writeBoolean(context, SYSTEM_BACKGROUND_4, true);
                        }
                    }
                    SPUtils.writeInteger(context, KEY_PENDING_SECONDS, pendingSeconds);
                }
            }
            // 刷新UI
            if (mHandler != null) {
                for (Handler handler : mHandler) {
                    Message msg = handler.obtainMessage(what);
                    msg.sendToTarget();
                }
            }
        }
    }
}