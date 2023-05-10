package cn.com.auto.thkl.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import cn.com.auto.thkl.R

object  UpTimeDialog  {
    fun showDialog(context: Context){
        val dialog  = AppCompatDialog(context,R.style.DialogStyle)
        val attributes = dialog.window!!.attributes
        dialog.setContentView(R.layout.dialog_up_time)
        dialog.show()
    }


}