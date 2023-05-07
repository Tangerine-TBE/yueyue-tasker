package cn.com.auto.thkl.utils;

import android.util.Log;


import cn.com.auto.thkl.BuildConfig;

public class L {
    private static final String TAG = "YueYue ";

    public static void e(String tag, String str) {
        Log.e(tag, str);
    }

    public static void e(String str) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, str);
        }
    }

    public static void d(String tag, String str) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, str);
        }
    }

    public static void d(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public static void w(String tag, String str) {
        Log.w(tag, str);
    }

    public static void w(String str) {
        Log.w(TAG, str);
    }

    public static void i(String tag, String str) {
        Log.i(tag, str);
    }

    public static void i(String str) {
        Log.i(TAG, str);
    }
}
