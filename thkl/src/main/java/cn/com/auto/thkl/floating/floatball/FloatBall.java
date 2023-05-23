package cn.com.auto.thkl.floating.floatball;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import cn.com.auto.thkl.autojs.key.GlobalKeyObserver;
import cn.com.auto.thkl.floating.FloatBallManager;
import cn.com.auto.thkl.floating.FloatBallUtil;
import cn.com.auto.thkl.floating.runner.ICarrier;
import cn.com.auto.thkl.floating.runner.OnceRunnable;
import cn.com.auto.thkl.floating.runner.ScrollRunner;
import cn.com.auto.thkl.floating.utils.MotionVelocityUtil;
import cn.com.auto.thkl.floating.utils.Util;
import cn.com.auto.thkl.weight.RoundTextView;


public class FloatBall extends FrameLayout implements ICarrier, GlobalKeyObserver.OnVolumeDownListener {

    private FloatBallManager floatBallManager;
    private ImageView imageView;
    private RoundTextView textView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager windowManager;
    private boolean isFirst = true;
    private boolean isAdded = false;
    private int mTouchSlop;
    /**
     * flag a touch is click event
     */
    private boolean isClick;
    private int mDownX, mDownY, mLastX, mLastY;
    private int mSize;
    private ScrollRunner mRunner;
    private int mVelocityX, mVelocityY;
    private MotionVelocityUtil mVelocity;
    private boolean sleep = false;
    private FloatBallCfg mConfig;
    private boolean mHideHalfLater = true;
    private boolean mLayoutChanged = false;
    private int mSleepX = -1;
    private Animation animation;

    public FloatBall(Context context, FloatBallManager floatBallManager, FloatBallCfg config) {
        super(context);
        this.floatBallManager = floatBallManager;
        mConfig = config;
        init(context);
    }

    private void init(Context context) {
        imageView = new ImageView(context);
        textView = new RoundTextView(context);
        initState();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mSize-35, mSize-35);
        layoutParams.gravity = Gravity.CENTER;
        addView(textView, layoutParams);
        addView(imageView, new ViewGroup.LayoutParams(mSize,mSize));
        initLayoutParams(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mRunner = new ScrollRunner(this);
        GlobalKeyObserver.getSingleton().addVolumeDownListener(this);
        mVelocity = new MotionVelocityUtil(context);
        animation = new RotateAnimation(360f,   0f,Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setFillAfter(true);  // 设置保持动画最后的状态
        animation.setDuration(10 * 1000);// 设置动画时间
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setInterpolator(new LinearInterpolator());// 设置插入器
    }

    private void initLayoutParams(Context context) {
        mLayoutParams = FloatBallUtil.getLayoutParams(context);
    }

    public final void setNormalState() {
        final Drawable icon = mConfig.mIcon;
        mSize = mConfig.mSize;
        Util.setBackground(imageView, icon);
        Drawable newIcon = icon.mutate();
        newIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        floatBallManager.setState(true);
        imageView.startAnimation(animation);
    }

    public final void initState() {
        final Drawable icon = mConfig.mIcon;
        mSize = mConfig.mSize;
        Util.setBackground(imageView, icon);
        Drawable newIcon = icon.mutate();
        newIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
    }

    public final void setUnNormalState() {
        imageView.clearAnimation();
        final Drawable icon = mConfig.mIcon;
        mSize = mConfig.mSize;
        Util.setBackground(imageView, icon);
        Drawable newIcon = icon.mutate();
        newIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        floatBallManager.setState(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            onConfigurationChanged(null);
        }
    }

    public void attachToWindow(WindowManager windowManager) {
        this.windowManager = windowManager;
        if (!isAdded) {
            windowManager.addView(this, mLayoutParams);
            isAdded = true;
        }
    }

    public void detachFromWindow(WindowManager windowManager) {
        this.windowManager = null;
        if (isAdded) {
//            removeSleepRunnable();
            if (getContext() instanceof Activity) {
                windowManager.removeViewImmediate(this);
            } else {
                windowManager.removeView(this);
            }
            isAdded = false;
            sleep = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int curX = mLayoutParams.x;
        if (sleep && curX != mSleepX && !mRunner.isRunning()) {
            sleep = false;
//            postSleepRunnable();
        }
        if (mRunner.isRunning()) {
            mLayoutChanged = false;
        }
        if (height != 0 && isFirst || mLayoutChanged) {
            if (isFirst && height != 0) {
                location(width, height);
            } else {
                moveToEdge(false, sleep);
            }
            isFirst = false;
            mLayoutChanged = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        floatBallManager.floatBallX = mLayoutParams.x;
        floatBallManager.floatBallY = mLayoutParams.y;
    }

    private void location(int width, int height) {
        FloatBallCfg.Gravity cfgGravity = mConfig.mGravity;
        mHideHalfLater = mConfig.mHideHalfLater;
        int gravity = cfgGravity.getGravity();
        int x;
        int y;
        int topLimit = 0;
        int bottomLimit = floatBallManager.mScreenHeight - height;
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            x = 0;
        } else {
            x = floatBallManager.mScreenWidth - width;
        }
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            y = topLimit;
        } else if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            y = floatBallManager.mScreenHeight - height - statusBarHeight;
        } else {
            y = floatBallManager.mScreenHeight / 2 - height / 2 - statusBarHeight;
        }
        y = mConfig.mOffsetY != 0 ? y + mConfig.mOffsetY : y;
        if (y < 0) y = topLimit;
        if (y > bottomLimit) y = topLimit;
        onLocation(x, y);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutChanged = true;
        floatBallManager.onConfigurationChanged(newConfig);
        moveToEdge(false, false);
//        postSleepRunnable();
    }

    public void onLayoutChange() {
        mLayoutChanged = true;
        requestLayout();
    }

    private void touchDown(int x, int y) {
        mDownX = x;
        mDownY = y;
        mLastX = mDownX;
        mLastY = mDownY;
        isClick = true;
//        removeSleepRunnable();
    }

    private void touchDown() {
        isClick = true;
    }

    private void touchMove(int x, int y) {
        int totalDeltaX = x - mDownX;
        int totalDeltaY = y - mDownY;
        int deltaX = x - mLastX;
        int deltaY = y - mLastY;
        if (Math.abs(totalDeltaX) > mTouchSlop || Math.abs(totalDeltaY) > mTouchSlop) {
            isClick = false;
        }
        mLastX = x;
        mLastY = y;
        if (!isClick) {
            onMove(deltaX, deltaY);
        }
    }

    private void touchUp() {
        if (sleep) {
            wakeUp();
        } else {
            if (isClick) {
                onClick();
            } else {
                moveToEdge(true, false);
            }
        }
    }

    private void moveToX(boolean smooth, int destX) {
        int statusBarHeight = floatBallManager.getStatusBarHeight();
        final int screenHeight = floatBallManager.mScreenHeight - statusBarHeight;
        int height = getHeight();
        int destY = 0;
        if (mLayoutParams.y < 0) {
            destY = 0 - mLayoutParams.y;
        } else if (mLayoutParams.y > screenHeight - height) {
            destY = screenHeight - height - mLayoutParams.y;
        }
        if (smooth) {
            int dx = destX - mLayoutParams.x;
            int duration = getScrollDuration(Math.abs(dx));
            mRunner.start(dx, destY, duration);
        } else {
            onMove(destX - mLayoutParams.x, destY);
//            postSleepRunnable();
        }
    }

    private void wakeUp() {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        destX = mLayoutParams.x < centerX ? 0 : screenWidth - width;
        sleep = false;
        moveToX(true, destX);
    }

    private void moveToEdge(boolean smooth, boolean forceSleep) {
        final int screenWidth = floatBallManager.mScreenWidth;
        int width = getWidth();
        int halfWidth = width / 2;
        int centerX = (screenWidth / 2 - halfWidth);
        int destX;
        final int minVelocity = mVelocity.getMinVelocity();
        if (mLayoutParams.x < centerX) {
            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX < 0 || mLayoutParams.x < 0;
            destX = sleep ? -halfWidth : 0;
        } else {
            sleep = forceSleep || Math.abs(mVelocityX) > minVelocity && mVelocityX > 0 || mLayoutParams.x > screenWidth - width;
            destX = sleep ? screenWidth - halfWidth : screenWidth - width;
        }
        if (sleep) {
            mSleepX = destX;
        }
        moveToX(smooth, destX);
    }

    private int getScrollDuration(int distance) {
        return (int) (250 * (1.0f * distance / 800));
    }

    private void onMove(int deltaX, int deltaY) {
        mLayoutParams.x += deltaX;
        mLayoutParams.y += deltaY;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onLocation(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public void onMove(int lastX, int lastY, int curX, int curY) {
        onMove(curX - lastX, curY - lastY);
    }

    @Override
    public void onDone() {
//        postSleepRunnable();
    }

    private void moveTo(int x, int y) {
        mLayoutParams.x += x - mLayoutParams.x;
        mLayoutParams.y += y - mLayoutParams.y;
        if (windowManager != null) {
            windowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    public int getSize() {
        return mSize;
    }

    private void onClick() {
        floatBallManager.floatBallX = mLayoutParams.x;
        floatBallManager.floatBallY = mLayoutParams.y;
        floatBallManager.onFloatBallClick();
    }

    @Override
    public void onVolumeDown() {
        if (floatBallManager.isCanOpen()) {
            getHandler().post(() -> {
                touchDown();
                touchUp();
            });
        }


    }

//    private void removeSleepRunnable() {
//        mSleepRunnable.removeSelf(this);
//    }
//
//    public void postSleepRunnable() {
//        if (mHideHalfLater && !sleep && isAdded) {
//            mSleepRunnable.postDelaySelf(this, 3000);
//        }
//    }
}
