package cn.com.auto.thkl.autojs.record;

import android.content.Context;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.stardust.app.DialogUtils;
import com.stardust.app.GlobalAppContext;
import com.stardust.autojs.core.record.Recorder;
import com.stardust.autojs.core.record.inputevent.InputEventRecorder;
import com.stardust.autojs.core.record.inputevent.InputEventToAutoFileRecorder;
import com.stardust.autojs.core.record.inputevent.InputEventToRootAutomatorRecorder;
import com.stardust.autojs.core.record.inputevent.TouchRecorder;
import com.stardust.util.ClipboardUtil;


import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.auto.thkl.Pref;
import cn.com.auto.thkl.R;

/**
 * Created by Stardust on 2017/8/6.
 */

public class GlobalActionRecorder implements Recorder.OnStateChangedListener {

    private static GlobalActionRecorder sSingleton;
    private CopyOnWriteArrayList<Recorder.OnStateChangedListener> mOnStateChangedListeners = new CopyOnWriteArrayList<>();
    private TouchRecorder mTouchRecorder;
    private Context mContext;
    private boolean mDiscard = false;

    public static GlobalActionRecorder getSingleton(Context context) {
        if (sSingleton == null) {
            sSingleton = new GlobalActionRecorder(context);
        }
        return sSingleton;
    }


    public GlobalActionRecorder(Context context) {
        mContext = new ContextThemeWrapper(context.getApplicationContext(),
                R.style.AppTheme);
    }


    public void start() {
        if (mTouchRecorder == null) {
            mTouchRecorder = createTouchRecorder();
        }
        mTouchRecorder.reset();
        mDiscard = false;
        mTouchRecorder.setOnStateChangedListener(this);
        mTouchRecorder.start();
    }

    private TouchRecorder createTouchRecorder() {
        return new TouchRecorder(mContext) {
            @Override
            protected InputEventRecorder createInputEventRecorder() {
                if (Pref.rootRecordGeneratesBinary())
                    return new InputEventToAutoFileRecorder(mContext);
                else
                    return new InputEventToRootAutomatorRecorder();
            }
        };
    }

    public void pause() {
        mTouchRecorder.pause();
    }

    public void resume() {
        mTouchRecorder.resume();
    }

    public void stop() {
        mTouchRecorder.stop();
    }

    public String getCode() {
        return mTouchRecorder.getCode();
    }

    public String getPath() {
        return mTouchRecorder.getPath();
    }

    public int getState() {
        if (mTouchRecorder == null)
            return Recorder.STATE_NOT_START;
        return mTouchRecorder.getState();
    }


    public void addOnStateChangedListener(Recorder.OnStateChangedListener listener) {
        mOnStateChangedListeners.add(listener);
    }

    public boolean removeOnStateChangedListener(Recorder.OnStateChangedListener listener) {
        return mOnStateChangedListeners.remove(listener);
    }

    @Override
    public void onStart() {
        if (Pref.isRecordToastEnabled())
            GlobalAppContext.toast(R.string.text_start_record);
        for (Recorder.OnStateChangedListener listener : mOnStateChangedListeners) {
            listener.onStart();
        }
    }

    @Override
    public void onStop() {
        if (!mDiscard) {
            String code = getCode();
            if (code != null){

            }
            else
                handleRecordedFile(getPath());
        }
        for (Recorder.OnStateChangedListener listener : mOnStateChangedListeners) {
            listener.onStop();
        }
    }

    @Override
    public void onPause() {
        for (Recorder.OnStateChangedListener listener : mOnStateChangedListeners) {
            listener.onPause();
        }
    }

    @Override
    public void onResume() {
        for (Recorder.OnStateChangedListener listener : mOnStateChangedListeners) {
            listener.onResume();
        }
    }

    public void discard() {
        mDiscard = true;
        stop();
    }
    private void handleRecordedFile(final String path) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            GlobalAppContext.post(() -> handleRecordedFile(path));
        }

    }



    private String getString(int res) {
        return mContext.getString(res);
    }

}
