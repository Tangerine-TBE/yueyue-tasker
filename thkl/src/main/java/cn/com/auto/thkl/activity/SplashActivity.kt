package cn.com.auto.thkl.activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.R
import com.gyf.barlibrary.BarHide
import com.gyf.barlibrary.ImmersionBar
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.model.AccessibilityViewModel
import com.blankj.utilcode.util.ServiceUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun setStatusBar() {
        ImmersionBar.with(this).statusBarColor(android.R.color.transparent).statusBarDarkFont(true)
            .hideBar(BarHide.FLAG_HIDE_BAR).navigationBarColor(android.R.color.transparent).init()
    }

    override fun initialize(): Any {
        return R.layout.activity_splash_yueyue
    }

    override fun initUI() {


    }

    override fun initListener() {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        App.handler.postDelayed({
            if (ServiceUtils.isServiceRunning("cn.com.auto.thkl.service.AccessibilityService") ) {
                if (AccessibilityViewModel.normalStartService.value == true){
                    startActivity(Intent(this, MainActivity::class.java))
                    return@postDelayed
                }else{
                    startActivity(Intent(this,LoginActivity::class.java))
                }
            }else{
                startActivity(Intent(this, CheckUpdateActivity::class.java))
            }
            finish()
        }, 2000)


    }

}