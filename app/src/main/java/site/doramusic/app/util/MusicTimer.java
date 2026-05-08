package site.doramusic.app.util;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

import site.doramusic.app.score.PointsManager;
import site.doramusic.app.score.PointsSource;

/**
 * 音乐播放定时器。
 * 功能：
 * 1. 定时刷新播放进度
 * 2. 统计有效播放时长
 * 3. 每分钟自动奖励积分
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
     * 每次奖励积分数量。
     */
    private static final int REWARD_POINTS = 10;

    private final Handler[] mHandler;

    private final Timer mTimer;

    private TimerTask mTimerTask;

    private final int what;

    /**
     * 是否已启动。
     */
    private boolean mTimerStart = false;

    /**
     * 已累计播放时长。
     */
    private long playDuration = 0L;

    /**
     * 是否正在播放。
     */
    private boolean isPlaying = false;

    public MusicTimer(Handler... handler) {
        this.mHandler = handler;
        this.what = REFRESH_PROGRESS_EVENT;
        this.mTimer = new Timer();
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

    class MusicTimerTask extends TimerTask {

        @Override
        public void run() {
            playDuration += INTERVAL_TIME;
            // 每分钟奖励一次积分
            if (playDuration >= REWARD_INTERVAL) {
                playDuration = 0L;
                PointsManager.INSTANCE.addPoints(
                        PointsSource.LISTEN_MUSIC.desc,
                        REWARD_POINTS
                );
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