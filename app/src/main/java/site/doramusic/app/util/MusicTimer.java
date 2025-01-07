package site.doramusic.app.util;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicTimer {

    public final static int REFRESH_PROGRESS_EVENT = 0x100;
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

    public void startTimer() {
        if (mHandler == null || mTimerStart) {
            return;
        }
        mTimerTask = new MusicTimerTask();
        mTimer.schedule(mTimerTask, INTERVAL_TIME, INTERVAL_TIME);
        mTimerStart = true;
    }

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
