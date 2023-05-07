package cn.com.auto.thkl.utils;

import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import cn.com.auto.thkl.App;
import cn.com.auto.thkl.R;

/**
 * 描述:Toast工具
 */
public class ToastUtil {
    public static void showShortToast(int rId) {
        showShortToast(App.Companion.getApp().getString(rId));
    }

    public static void showLongToast(int rId) {
        showLongToast(App.Companion.getApp().getString(rId));
    }

    public static void showShortToast(String content) {
        Toast.makeText(App.Companion.getApp(),content, Toast.LENGTH_SHORT).show();
    }


    public static void showLongToast(String content) {
        Toast.makeText(App.Companion.getApp(),content, Toast.LENGTH_LONG).show();
    }


    public static void showCenterToast(String s){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            try{
                Toast toast = new Toast(App.Companion.getApp());
                View view = LayoutInflater.from(App.Companion.getApp()).inflate(R.layout.toast_,null,false);
                TextView textView = view.findViewById(R.id.tv_toast);
                textView.setText(s);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(App.Companion.getApp(),s,Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(App.Companion.getApp(),s,Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * 防止弹出多次吐司
     * */
    private static String oldMsg;
    protected static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

    public static void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(App.Companion.getApp(), s, Toast.LENGTH_SHORT);
            toast.show();
            oldMsg = s;
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = s;
                toast.setText(s);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

}
