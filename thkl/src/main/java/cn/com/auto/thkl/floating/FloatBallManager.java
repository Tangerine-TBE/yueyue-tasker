package cn.com.auto.thkl.floating;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;


import java.util.ArrayList;
import java.util.List;

import cn.com.auto.thkl.floating.floatball.FloatBall;
import cn.com.auto.thkl.floating.floatball.FloatBallCfg;
import cn.com.auto.thkl.floating.floatball.StatusBarView;
import cn.com.auto.thkl.floating.menu.FloatMenu;
import cn.com.auto.thkl.floating.menu.FloatMenuCfg;
import cn.com.auto.thkl.floating.menu.MenuItem;


public class FloatBallManager {
    public int mScreenWidth, mScreenHeight;

    private OnFloatBallClickListener mFloatBallClickListener;
    private final WindowManager mWindowManager;
    private final FloatBall floatBall;
    private final FloatMenu floatMenu;
    private final StatusBarView statusBarView;
    public int floatBallX, floatBallY;
    private boolean isShowing = false;
    private List<MenuItem> menuItems = new ArrayList<>();
    private boolean canOpen = true;

    public final boolean isCanOpen() {
        return canOpen;
    }

    public final void setCanOpen(boolean canOpen) {
        this.canOpen = canOpen;
    }

    public FloatBallManager(Context application, FloatBallCfg ballCfg) {
        this(application, ballCfg, null);
    }

    public FloatBallManager(Context application, FloatBallCfg ballCfg, FloatMenuCfg menuCfg) {
        Context mContext = application.getApplicationContext();
        FloatBallUtil.inSingleActivity = false;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        floatBall = new FloatBall(mContext, this, ballCfg);
        floatMenu = new FloatMenu(mContext, this, menuCfg);
        statusBarView = new StatusBarView(mContext, this);
    }

    public FloatBallManager(Activity activity, FloatBallCfg ballCfg) {
        this(activity, ballCfg, null);
    }

    public final void changeState(boolean normal) {
        state = normal;
        if (floatBall != null) {
            if (normal) {
                floatBall.setNormalState();
            } else {
                floatBall.setUnNormalState();
            }
        }
    }

    public final void initState() {
        if (floatBall != null) {
            floatBall.initState();

        }
    }

    private boolean state = true;

    public final boolean getState() {
        return state;
    }

    public final void setState(boolean state) {
        this.state = state;
    }

    public void buildMenu() {
        inflateMenuItem();
    }

    /**
     * 添加一个菜单条目
     *
     * @param item
     */
    public FloatBallManager addMenuItem(MenuItem item) {
        menuItems.add(item);
        return this;
    }

    public int getMenuItemSize() {
        return menuItems != null ? menuItems.size() : 0;
    }

    /**
     * 设置菜单
     *
     * @param items
     */
    public FloatBallManager setMenu(List<MenuItem> items) {
        menuItems = items;
        return this;
    }

    private void inflateMenuItem() {
        floatMenu.removeAllItemViews();
        for (MenuItem item : menuItems) {
            floatMenu.addItem(item);
        }
    }

    public int getBallSize() {
        return floatBall.getSize();
    }

    public void computeScreenSize() {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mScreenHeight = point.y;
    }

    public int getStatusBarHeight() {
        return statusBarView.getStatusBarHeight();
    }

    public void onStatusBarHeightChange() {
        floatBall.onLayoutChange();
    }

    public void show() {
        if (isShowing) return;
        isShowing = true;
        floatBall.setVisibility(View.VISIBLE);
        statusBarView.attachToWindow(mWindowManager);
        floatBall.attachToWindow(mWindowManager);
        floatMenu.detachFromWindow(mWindowManager);
    }

    public void closeMenu() {
        floatMenu.closeMenu();
    }

    public void reset() {
        floatBall.setVisibility(View.VISIBLE);
//        floatBall.postSleepRunnable();
        floatMenu.detachFromWindow(mWindowManager);
    }

    public void onFloatBallClick() {
        if (menuItems != null && menuItems.size() > 0) {
            floatMenu.attachToWindow(mWindowManager);
        } else {
            if (mFloatBallClickListener != null) {
                mFloatBallClickListener.onFloatBallClick();
            }
        }
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        floatBall.detachFromWindow(mWindowManager);
        floatMenu.detachFromWindow(mWindowManager);
        statusBarView.detachFromWindow(mWindowManager);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        computeScreenSize();
        reset();
    }


    public void setOnFloatBallClickListener(OnFloatBallClickListener listener) {
        mFloatBallClickListener = listener;
    }

    public interface OnFloatBallClickListener {
        void onFloatBallClick();
    }

}
