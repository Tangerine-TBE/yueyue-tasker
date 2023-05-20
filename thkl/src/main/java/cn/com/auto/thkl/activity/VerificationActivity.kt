package cn.com.auto.thkl.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.utils.L
import cn.com.auto.thkl.utils.SP

class VerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
    }

    override fun onStart() {
        super.onStart()
        if (Intent.ACTION_MAIN == intent.action
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        ) {
            L.e("launcher")
            if (AccessibilityViewModel.settingTask.value == true){
                finish()
                return
            }
            if (AccessibilityViewModel.logout.value != null) {
                AccessibilityViewModel.logout.value = null
            }
            if (SP.getBoolean(Constant.EXIT)) {
                L.e("launcher 重置")
                SP.putBoolean(Constant.EXIT, false)
            }
        } else {
            L.e("非launcher")
            if (SP.getBoolean(Constant.EXIT)) {
                L.e("非launcher 需要退出")
                finish()
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startActivity(Intent(this, SplashActivity::class.java))
    }
}