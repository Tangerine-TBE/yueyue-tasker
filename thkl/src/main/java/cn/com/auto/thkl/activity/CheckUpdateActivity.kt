package cn.com.auto.thkl.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import cn.com.auto.thkl.App
import cn.com.auto.thkl.BuildConfig
import cn.com.auto.thkl.R
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.net.Api
import com.alibaba.fastjson.JSONObject
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_check_update.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class CheckUpdateActivity : BaseActivity() {
    private val mApkPath = App.app.applicationContext.filesDir.absolutePath + "/apks/"
    private lateinit var  animator: Animator
    override fun setStatusBar() {
        ImmersionBar.with(this)
            .statusBarColor(android.R.color.transparent)
            .statusBarDarkFont(true)
            .navigationBarColor(android.R.color.transparent)
            .init()
    }

    override fun initialize(): Any = R.layout.activity_check_update

    override fun initUI() {

    }

    override fun initListener() {
         animator = ObjectAnimator.ofFloat(iv_update, "offsetAngle", 360f)
        animator.interpolator = LinearInterpolator()
        animator.duration = 6 * 1000
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val n = iv_update.offsetAngle / 360
                (animator as ObjectAnimator?)!!.setFloatValues(n * 360 + 360)
                animator.start()
            }
        })
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        animator.cancel()
    }

    override fun onResume() {
        super.onResume()
        startActivity(
            Intent(
                this@CheckUpdateActivity, AccessibilityCheckActivity::class.java
            )
        )
        return
        download()
    }
    private fun download(){
        lifecycleScope.launch {
            kotlin.runCatching {
                delay(5000)
                Api.getApiService().getClientInfo("android")
            }.onFailure {
                it.printStackTrace()
                download()
            }.onSuccess {
                if (it.success) {
                    val string = JSONObject.toJSONString(it.obj)
                    val versionCode = JSONObject.parseObject(string).getString("versionNum")
                    val versionlink = JSONObject.parseObject(string).getString("versionlink")
                    if (!versionCode.equals(BuildConfig.VERSION_NAME)) {
                        kotlin.runCatching {
                            Api.getApiService().download(versionlink)
                        }.onFailure {
                            it.printStackTrace()
                        }.onSuccess {
                            val path = mApkPath + "悦悦赚.apk"
                            writeResponseToDisk(path, it)

                        }
                    }
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(
                                this@CheckUpdateActivity, AccessibilityCheckActivity::class.java
                            )
                        )
                        finish()
                    }
                }else{
                    download()
                }
            }
        }

    }

    private fun writeResponseToDisk(path: String, response: Response<ResponseBody>) {
        if (response.body() != null) {
            writeFileFromIs(File(path), response.body()!!.byteStream())
        }
    }

    private fun writeFileFromIs(file: File, `is`: InputStream) {
        if (!file.exists()) {
            if (!file.parentFile?.exists()!!) {
                file.parentFile?.mkdir()
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        var os: OutputStream? = null
        try {
            os = FileOutputStream(file)
            val data = ByteArray(1024)
            var len: Int
            while (`is`.read(data).also { len = it } != -1) {
                os.write(data, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}