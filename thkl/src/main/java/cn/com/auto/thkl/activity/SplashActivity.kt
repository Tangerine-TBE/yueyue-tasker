package cn.com.auto.thkl.activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import cn.com.auto.thkl.R
import com.gyf.barlibrary.BarHide
import com.gyf.barlibrary.ImmersionBar
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.model.AccessibilityViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun setStatusBar() {
        ImmersionBar.with(this)
            .statusBarColor(android.R.color.transparent)
            .statusBarDarkFont(true)
            .hideBar(BarHide.FLAG_HIDE_BAR)
            .navigationBarColor(android.R.color.transparent)
            .init()
    }

    override fun initialize(): Any {
        return R.layout.activity_splash_yueyue
    }

    override fun initUI() {


    }

    override fun initListener() {
    }

    override fun onResume() {
        super.onResume()


        if (isRunService(this,"cn.com.auto.thkl.service.AccessibilityService")){
            if (AccessibilityViewModel.normalStartService.value == true){
                startActivity(Intent(this,MainActivity::class.java))
                return
            }
        }

        startActivity(Intent(this,CheckUpdateActivity::class.java))
    }
    private fun isRunService(context: Context, serviceName:String) :Boolean{
        val manager =context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(item in manager.getRunningServices(Int.MAX_VALUE)){
            if (serviceName == item.service.className){
                return true
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        finish()
    }




}