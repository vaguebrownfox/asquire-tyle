package aashi.fiaxco.asquiretyle0x0a.providestuff;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import aashi.fiaxco.asquiretyle0x0a.R;


public class Timer {

    private long startTime = 0;
    private long stopTime = 0;
    public boolean running = false;

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    // elapsed time in milliseconds
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return stopTime - startTime;
    }

    // elapsed time in seconds
    public long getElapsedTimeSecs() {
        if (running) {
            return ((System.currentTimeMillis() - startTime) / 1000);
        }
        return ((stopTime - startTime) / 1000);
    }

    public String getElapsedTimeMinutes() {
        long unitMin = 60L;
        if (running) {
            long totalSeconds = getElapsedTimeSecs();
            long minutes = totalSeconds / unitMin;
            long seconds = totalSeconds - unitMin * minutes;
            long milliseconds = (getElapsedTime() - totalSeconds * 1000) % 1000;

            String minutesS;
            String secondsS;
            String millisecondsS;

            if (minutes < 10) {
                minutesS = "0" + minutes;
            } else {
                minutesS = "" + minutes;
            }
            if (seconds < 10) {
                secondsS = "0" + seconds;
            } else {
                secondsS = "" + seconds;
            }

            if (milliseconds < 10) {
                millisecondsS = "00" + milliseconds;
            } else if(milliseconds < 100) {
                millisecondsS = "0" + milliseconds;
            } else {
                millisecondsS = "" + milliseconds;
            }

            return minutesS + ":" + secondsS + ":" + millisecondsS;
        } else {
            return "00:00:00";
        }

    }

    public interface MessageConstants {
        int MSG_START_TIMER = 6000;
        int MSG_UPDATE_TIMER = 6001;
        int MSG_STOP_TIMER = 6002;
        int MSG_RESET_TIMER = 6003;
    }

    // Record activity timer handler
    public static class TimerHandler extends Handler implements MessageConstants {

        Context mContext;
        Timer mTimer;
        TextView mTextViewRecordTimer;
        MediaRecorder mRecorder;
        public TimerHandler(Context context, Timer timer) {
            mContext = context;
            mTimer = timer;
            mTextViewRecordTimer =
                    ((Activity) mContext).findViewById(R.id.record_timer_tv);

        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int REFRESH_RATE = 100;
            switch (msg.what) {
                case MSG_START_TIMER :
                    mTimer.start();
                    sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;
                case MSG_UPDATE_TIMER :
                    mTextViewRecordTimer.setText(mTimer.getElapsedTimeMinutes());
                    sendEmptyMessageDelayed(MSG_UPDATE_TIMER, REFRESH_RATE);

//                    int maxAmplitude = mRecorder.getMaxAmplitude();
//                    if (maxAmplitude != 0) {
//                        waveView.setWidth(maxAmplitude);
//                        Log.d("Record LOL", "run LOL: " + maxAmplitude);
//                    }
                    break;
                case MSG_STOP_TIMER :
                    removeMessages(MSG_UPDATE_TIMER);
                    mTimer.stop();
                    break;
                case MSG_RESET_TIMER :
                    removeMessages(MSG_UPDATE_TIMER);
                    mTextViewRecordTimer.setText(R.string.timer_init);
                    mTimer.stop();
                    break;
                default:
                    break;
            }
        }
    }

}
