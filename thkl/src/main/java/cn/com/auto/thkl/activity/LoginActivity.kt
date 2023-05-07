package cn.com.auto.thkl.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.custom.event.AutoCaptureEvent
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import cn.com.auto.thkl.devplugin.DevPlugin
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.net.Api
import cn.com.auto.thkl.utils.SP
import cn.com.auto.thkl.utils.ToastUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.alibaba.fastjson.JSONObject
import com.gyf.barlibrary.ImmersionBar
import com.stardust.app.permission.DrawOverlaysPermission
import kotlinx.android.synthetic.main.activity_login_yueyue.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginActivity : BaseActivity() {
    companion object {
        const val MODEL = "YueYueLoginActivity"
        const val LOG_OUT = "log_out"
        const val LOG_IN = "log_in"
    }

    private var model: String? = null


    override fun setStatusBar() {
        ImmersionBar.with(this).statusBarColor("#00000000").statusBarDarkFont(true).init()
    }

    /*退出登录*//*第一次登录*//*二次登录*/
    override fun initialize() = R.layout.activity_login_yueyue


    @SuppressLint("CommitPrefEdits")
    override fun initUI() {
        model = intent.getStringExtra(MODEL)
        mMediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun initListener() {
        btn_login.setOnClickListener {
            updateLogin()
        }
        btn_login.setOnLongClickListener(View.OnLongClickListener {
            MaterialDialog.Builder(this).title("DEBUG模式")
                .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL).input(
                    "ip地址", null
                ) { _, input -> GlobalScope.launch { DevPlugin.connect(getUrl(input.toString())) } }
                .positiveText("确定").neutralText("取消").show()

            return@OnLongClickListener true
        })
        if (DrawOverlaysPermission.isCanDrawOverlays(this)) {
            if (AccessibilityViewModel.window.value != true) {
                AccessibilityViewModel.window.value = true
            }
            if (AccessibilityViewModel.capture.value == null) {
                GlobalScope.launch {
                    autoRequestScreenEvent()
                }
            } else {
                checkLogin()
            }
        } else {
            AccessibilityViewModel.overLayerTask.value = true
        }

    }

    private var mMediaProjectionManager: MediaProjectionManager? = null

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun autoRequestScreenEvent() {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                AutoCaptureEvent(
                    this,
                    mMediaProjectionManager!!,
                    TaskProperty(TaskType.AUTO_CAPTURE_TASK, "", "", "", false, null)
                )
            ).execute(object : EventAction.OnEventCompleted {
                override fun eventCompleted(name: String) {
                    continuation.resume(name)
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val mediaProjection = mMediaProjectionManager!!.getMediaProjection(resultCode, data)
                if (mediaProjection != null) {
                    AccessibilityViewModel.capture.value = mediaProjection
                    checkLogin()
                }
            } else {
                MaterialDialog.Builder(this).title("错误提示")
                    .content("检测到模拟点击出现错误，出现这些错误可能\r\n需要您进行关机，开机操作才能修复此问题。")
                    .positiveText("确定").show()
            }
        }
    }

    private fun getUrl(host: String): String {
        var url1 = host
        if (!url1.matches(Regex("^(ws|wss)://.*"))) {
            url1 = "ws://${url1}"
        }
        if (!url1.matches(Regex("^.+://.+?:.+$"))) {
            url1 += ":${DevPlugin.SERVER_PORT}"
        }
        return url1
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("CommitPrefEdits")
    private fun checkLogin() {
        if (TextUtils.isEmpty(model)) {/*二次登录*/
            val account = SP.getString(Constant.account)
            val cipher = SP.getString(Constant.cipher)
            if (!account.isNullOrEmpty() && !cipher.isNullOrEmpty()) {
                et_account.setText(account)
                et_cipher.setText(cipher)
                val value = SP.getString(MODEL)
                if (!value.isNullOrEmpty() && value == LOG_IN) {/*校验设备合法性*/
                    start(account, cipher)
                }
            }
        } else {
            val account = SP.getString(Constant.account)
            val cipher = SP.getString(Constant.cipher)
            SP.putString(MODEL, LOG_OUT)
            if (!account.isNullOrEmpty() && !cipher.isNullOrEmpty()) {
                et_account.setText(account)
                et_cipher.setText(cipher)
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("CommitPrefEdits")
    private fun updateLogin() {
        val accountString = et_account.text.toString()
        val cipherString = et_cipher.text.toString()
        if (!TextUtils.isEmpty(accountString) && !TextUtils.isEmpty(cipherString)) {
            SP.putString(Constant.account, accountString)
            SP.putString(Constant.cipher, cipherString)
            start(accountString, cipherString)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("CommitPrefEdits")
    private fun start(account: String, cipher: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                showProcessDialog()
            }
            kotlin.runCatching {
                Api.getApiService().taskSignIn(cipher, account, 0)
            }.onFailure { it.printStackTrace() }.onSuccess {
                if (it.success) {
                    SP.putString(MODEL, LOG_IN)
                    val string = JSONObject.toJSONString(it.obj)
                    JSONObject.parseObject(string).getString("accessToken").let {
                        SP.putString(Constant.TOKEN, it.toString())
                    }
                    JSONObject.parseObject(string).getString("userId").let {
                        SP.putString(Constant.USER_ID, it.toString())
                    }
                    JSONObject.parseObject(string).getString("userName").let {
                        SP.putString(Constant.USER_NAME, it.toString())
                    }
                    JSONObject.parseObject(string).getString("appId").let {
                        SP.putString(Constant.APP_ID, it.toString())
                    }
                    JSONObject.parseObject(string).getInteger("deviceId").let {
                        SP.putString(Constant.DEVICE_ID, it.toString())
                    }

                    withContext(Dispatchers.Main) {
                        hideProcessDialog()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        hideProcessDialog()
                        if (!TextUtils.isEmpty(it.msg)) {
                            ToastUtil.showToast(it.msg)
                        } else {
                            ToastUtil.showToast(it.msgDetail)
                        }
                    }

                }
            }

        }

    }
}