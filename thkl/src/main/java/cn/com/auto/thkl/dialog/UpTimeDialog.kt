package cn.com.auto.thkl.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import cn.com.auto.thkl.R

object UpTimeDialog {

    private var dialog: AppCompatDialog? = null

    @SuppressLint("SetTextI18n")
    fun showDialog(context: Context, date: String, listener: DialogInterface.OnDismissListener) {
        dialog = AppCompatDialog(context, R.style.DialogStyle)
        dialog!!.setContentView(R.layout.dialog_up_time)
        val textView = dialog!!.findViewById<TextView>(R.id.tv_date)
        val ivCancel = dialog!!.findViewById<ImageView>(R.id.iv_cancel)
        textView!!.text = "有效期时间:$date"
        ivCancel!!.setOnClickListener{
            dismissDialog()
        }
        dialog!!.show()
        dialog!!.setOnDismissListener(listener)
    }

    fun dismissDialog() {
        if (dialog != null) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
    }


}