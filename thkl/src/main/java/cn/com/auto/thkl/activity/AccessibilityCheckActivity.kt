package cn.com.auto.thkl.activity

import android.content.Intent
import android.provider.Settings
import cn.com.auto.thkl.R
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.utils.AccessibilityServiceTool
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_accessibility_check.*

class AccessibilityCheckActivity : BaseActivity() {
    override fun setStatusBar() {
        ImmersionBar.with(this)
            .statusBarColor("#00000000")
            .statusBarDarkFont(true)
            .init()
    }

    override fun initialize(): Any {
        return R.layout.activity_accessibility_check
    }

    override fun initUI() {
        startLoginActivity()
    }
    private fun startLoginActivity(){
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)){
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun initListener() {
        btn_accessibility.setOnClickListener{
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }


}