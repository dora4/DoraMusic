package site.doramusic.app.util;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicTimer {

    /**
     * 刷新进度条的事件类型。
     */
    public final static int REFRESH_PROGRESS_EVENT = 0x100;
    /**
     * 每隔0.5秒刷新一次歌曲播放进度。
     */
    private static final int INTERVAL_TIME = 500;
    private final Handler[] mHandler;
    private final Timer mTimer;
    private TimerTask mTimerTask;

    private final int what;
    private boolean mTimerStart = false;

    public MusicTimer(Handler... handler) {
        this.mHandler = handler;
        this.what = REFRESH_PROGRESS_EVENT;
        mTimer = new Timer();
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

    class MusicTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mHandler != null) {
                for (Handler handler : mHandler) {
                    Message msg = handler.obtainMessage(what);
                    msg.sendToTarget();
                }
            }
        }
    }
}
