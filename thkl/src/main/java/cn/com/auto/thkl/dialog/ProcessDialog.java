package cn.com.auto.thkl.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;


import cn.com.auto.thkl.utils.MResource;

public class ProcessDialog extends AppCompatDialog {
    private final Context context;
    private TextView mTvMessage;

    public ProcessDialog(Context context) {
        super(context,  MResource.getIdByName(context, "style", "loading_dialog"));
        this.context=context;
        init();
    }
    private void init() {
        setContentView(MResource.getIdByName(context, "layout", "dialog_process_yueyue"));
        mTvMessage=(TextView)findViewById(MResource.getIdByName(context, "id", "tv_message"));
        setCanceledOnTouchOutside(false);
    }
    public void setType(ProgressType type){
        switch (type){
            case loading:
                mTvMessage.setText(context.getString(MResource.getIdByName(context, "string", "loading")));
                break;
            case submitting:
                mTvMessage.setText(context.getString(MResource.getIdByName(context, "string", "submitting")));
                break;
        }
        mTvMessage.setVisibility(View.VISIBLE);
    }
    public void setMessage(String message){
        if(message!=null){
            mTvMessage.setText(message);
            mTvMessage.setVisibility(View.VISIBLE);
        }else{
            mTvMessage.setVisibility(View.GONE);
        }
    }
    public enum ProgressType{
        loading,submitting
    }
}
