package com.stardust.util;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.stardust.R;


/**
 * 描述:Toast工具
 */
public class ToastUtil {

    public static void showCenterToast(Context context, String s){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            try{
                Toast toast = new Toast(context);
                View view = LayoutInflater.from(context).inflate(R.layout.toast_,null,false);
                TextView textView = view.findViewById(R.id.tv_toast);
                textView.setText(s);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
        }

    }
    public static void showCenterToast(Context context,@StringRes  int id){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            try{
                Toast toast = new Toast(context);
                View view = LayoutInflater.from(context).inflate(R.layout.toast_,null,false);
                TextView textView = view.findViewById(R.id.tv_toast);
                textView.setText(context.getString(id));
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context,context.getString(id),Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context,context.getString(id),Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 防止弹出多次吐司
     * */


}
