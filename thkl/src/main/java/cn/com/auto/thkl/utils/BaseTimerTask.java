package cn.com.auto.thkl.utils;

import java.util.TimerTask;

public class BaseTimerTask extends TimerTask {
    private final ITimerListener iTimerListener;
    public BaseTimerTask(ITimerListener iTimerListener){
        this.iTimerListener = iTimerListener;

    }
        @Override
    public void run() {
        if (iTimerListener != null){
            iTimerListener.onTimer();
        }
    }
}
