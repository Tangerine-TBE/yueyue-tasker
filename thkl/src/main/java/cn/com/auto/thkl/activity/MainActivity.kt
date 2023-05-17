package cn.com.auto.thkl.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.dialog.UpTimeDialog
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.service.AccessibilityService
import cn.com.auto.thkl.utils.L
import cn.com.auto.thkl.utils.SP
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.AppUtils
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_main_yueyue.*
import java.text.SimpleDateFormat
import kotlin.system.exitProcess


/**
 * 1.
 * 《----------------------------------------出错直接上报，但无法截图，并提示信息-------------------------------》
 * 《-------------------------------------------心跳包---后台无法进行干预-------------------------------------》
 *  第一次启动 权限列表遍历开启 -》自启动开启 -》清除后台应用 -》回到主页面 -》开启悬浮窗口 -》开启后台录制 -》查询任务
 *  非第一次启动 进入主页 -》开启悬浮窗 -》开启后台录制 -》查询任务
 * 2.
 * 《-------------------------------------------心跳包-------------------------------------------------------》
 * 《------------------------------------------屏幕录制------------------------------------------------------》
 * 《-----------------------------------------等待指令下达----------------------------------------------------》
 * 《信息匹配》=《判断目标包名，版本，应用名称》
 * 《执行脚本》=《曾经安装过，进入对应app打开所有权限，并执行对应的脚本文件》
 * 《心跳包收到指令，如关机等都会停止当前任务，开始执行指令》
 * 《上报》=《上传当前图片与信息》
 *  查询任务 -》收到任务 -》信息匹配 -》执行脚本-》脚本结束（成功）-》上报-》清楚后台应用-》强行停止目标应用-》查询下一次任务》
 *  查询任务 -》收到任务 -》信息不匹配 -》上报 -》查询下一次任务
 *  查询任务 -》收到任务 -》信息匹配 -》执行脚本 -》脚本结束（异常）-》获取即时图片 -》上报 -》清楚后台应用 -》强行停止目标应用 -》查询下一次任务》
 *  查询任务 -》收到任务 -》信息匹配 -》执行脚本 -》脚本结束（停止按键）-》上报 -》清楚后台应用 -》强行停止目标 -》等待恢复
 *  查询任务 -》收到任务 -》信息匹配 -》执行脚本 -》指令下达（暂停）-》脚本结束 -》清除后台应用-》强行停止目标 -》结束（等待恢复）
 *  查询任务 -》收到任务 -》信息匹配 -》执行脚本 -》任务超时 -》上报-》停止脚本 -》清除后台应用 -》强行停止目标应用 -》查询下一次任务
 *  查询任务 -》没有任务 -》结束 -》指令下达（开始）-》查询任务
 * */
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : BaseActivity(), DialogInterface.OnDismissListener {
    private lateinit var handler: Handler
    override fun setStatusBar() {
        ImmersionBar.with(this).statusBarColor(android.R.color.transparent).statusBarDarkFont(true)
            .navigationBarColor(android.R.color.transparent).init()
    }

    override fun initialize(): Any {

        return R.layout.activity_main_yueyue
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ServiceCast")
    override fun initUI() {
        /**
         * 1.持久华获取用户信息
         *  user_id*/
        handler = Handler(Looper.getMainLooper())
        val loginName = SP.getString(Constant.LOGIN_NAME)
        val userName = SP.getString(Constant.USER_NAME)
        val userId = SP.getString(Constant.USER_ID)
        val expiryTime = SP.getString(Constant.EXPIRY_TIME)
        val deviceId = SP.getString(Constant.DEVICE_ID)
        tv_id.text = deviceId
        tv_nick_name.text = userName
        tv_phone.text = loginName
        tv_date.text = expiryTime
        tv_version.text = AppUtils.getAppVersionName()

        /**用户信息获取完毕*/
        /**是否需要提示？只提示一次。标志位的重置在用户退出登录进行*/
        if (AccessibilityViewModel.upTimeTips.value == false) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = dateFormat.parse(expiryTime)
            val time = date.time
            val now = System.currentTimeMillis()
            L.e("$now----------$time")
            if ((time.minus(now)) <= 14 * 24 * 60 * 60 * 1000) {
                UpTimeDialog.showDialog(this@MainActivity, expiryTime, this)
                Handler(Looper.getMainLooper()).postDelayed({
                    UpTimeDialog.dismissDialog()
                }, 10000)
            } else {
                onDismiss(null)
            }
            AccessibilityViewModel.upTimeTips.value = true
        }
        /**登录成功后*/
        val bundle = intent?.extras
        val equipment = bundle?.getString(Constant.FIRST_EQUIPMENT)
        /**这里是是否完了第一次维护，完成了就提示用户进行下一步操作*/
        if (!TextUtils.isEmpty(equipment)) {
            MaterialDialog.Builder(this).title("自动部署完成").contentColor(Color.parseColor("#666666"))
                .content("首次使用建议先退出，手动注册登录各任务APP，并完成各APP首次提现后，再运行[${AppUtils.getAppName()}]进行自动做任务")
                .negativeText("退出${AppUtils.getAppName()}")
                .negativeColor(Color.parseColor("#FF120E")).positiveText("开始任务")
                .positiveColor(Color.parseColor("#09BC21")).onNegative { _, _ ->
                    /*手动部署*/
                   AccessibilityViewModel.exitTask.value = true
                }.onPositive { _, _ ->
                    /*自动部署*/
                    AccessibilityViewModel.settingTask.value = true
                }.show()
        }
    }

    override fun initListener() {
        /**退出登录返回登录界面*/
        btn_logout.setOnClickListener {
            AccessibilityViewModel.logout.value = this@MainActivity
            AccessibilityViewModel.upTimeTips.value = false
        }
        btn_restart.setOnClickListener {
            AccessibilityViewModel.restartTask.value = true
        }
        btn_shutdown.setOnClickListener {
            AccessibilityViewModel.shutDownTask.value = true
        }
        btn_exit.setOnClickListener {
            AccessibilityViewModel.exitTask.value = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val bundle = intent?.extras
        val uptime = bundle?.getString(Constant.UPTIME)
        val equipment = bundle?.getString(Constant.FIRST_EQUIPMENT)
        if (!TextUtils.isEmpty(uptime)) {
            AccessibilityViewModel.logout.postValue(this)
        }
        if (!TextUtils.isEmpty(equipment)) {
            MaterialDialog.Builder(this).title("自动部署完成").contentColor(Color.parseColor("#666666"))
                .content("首次使用建议先退出，手动注册登录各任务APP，并完成各APP首次提现后，再运行[${AppUtils.getAppName()}]进行自动做任务")
                .negativeText("退出${AppUtils.getAppName()}")
                .negativeColor(Color.parseColor("#FF120E")).positiveText("开始任务")
                .positiveColor(Color.parseColor("#09BC21")).onNegative { _, _ ->
                    /*手动部署*/
                    AccessibilityViewModel.exitTask.value = true
                }.onPositive { _, _ ->
                    /*自动部署*/
                    AccessibilityViewModel.settingTask.value = true
                }.show()
        }
    }

    override fun onDismiss(p0: DialogInterface?) {
        if (AccessibilityViewModel.heartBeatTask.value != true) {
            AccessibilityViewModel.heartBeatTask.value = true
        }
        if (AccessibilityViewModel.onDate.value != true) {
            AccessibilityViewModel.onDate.value = true
        }
        if (AccessibilityViewModel.normalStartService.value != true) {
            AccessibilityViewModel.normalStartService.value = true
        }
        if (AccessibilityViewModel.settingTask.value != true) {/*清除所有准备要进行的消息*/
            AccessibilityViewModel.settingTask.value = true
        }
    }
}