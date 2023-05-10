package cn.com.auto.thkl.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.base.BaseActivity
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.utils.L
import cn.com.auto.thkl.utils.SP
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.DeviceUtils
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_main_yueyue.*

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
class MainActivity : BaseActivity() {
    private lateinit var handler: Handler
    override fun setStatusBar() {
        ImmersionBar.with(this).statusBarColor(android.R.color.transparent).statusBarDarkFont(true)
            .navigationBarColor(android.R.color.transparent).init()
    }

    override fun initialize(): Any {

        return R.layout.activity_main_yueyue
    }

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
        tv_nick_name.text = loginName
        tv_phone.text = userName
        tv_date.text = expiryTime
        tv_version.text = userId
        /**用户信息获取完毕*/
    }

    override fun initListener() {
        /**退出登录返回登录界面*/
        btn_logout.setOnClickListener {
            AccessibilityViewModel.logout.value = this@MainActivity
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
        if (AccessibilityViewModel.hearBeatTask.value != true) {
            AccessibilityViewModel.hearBeatTask.value = true
        }
        if (AccessibilityViewModel.onDate.value != null) {
            AccessibilityViewModel.onDate.value = SP.getString(Constant.EXPIRY_TIME)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()/*判断手机兼容问题*/
//        if(AccessibilityViewModel.normalStartService.value == true){
//            return
//        }
//        MaterialDialog.Builder(this).title("欢迎使用阅阅乐")
//            .contentColor(Color.parseColor("#666666"))
//            .content("你的手机机型[${DeviceUtils.getManufacturer()}:${DeviceUtils.getModel()}]未认证,可尝试使用品牌[5G]默认参数进行自动部署,收益可能受轻微影响。部署时间可能较长(包括系统设置和下载安装任务APP),请耐心等待。")
//            .negativeText("手动部署").negativeColor(Color.parseColor("#FF120E"))
//            .neutralText("已完成部署").neutralColor(Color.parseColor("#5C58BF"))
//            .positiveText("自动部署").positiveColor(Color.parseColor("#09BC21"))
//            .onNegative { _, _ ->
//                /*手动部署*/
//            }.onNeutral { _, _ ->
//                /*已完成部署*/
//                if (AccessibilityViewModel.normalStartService.value != true) {
//                    AccessibilityViewModel.normalStartService.value = true
//                }
//                if (AccessibilityViewModel.settingTask.value != true) {/*清除所有准备要进行的消息*/
//                    AccessibilityViewModel.settingTask.value = true
//                }
//            }.onPositive { _, _ ->
//                /*自动部署*/
//                if (AccessibilityViewModel.normalStartService.value != true) {
//                    AccessibilityViewModel.normalStartService.value = true
//                }
//                if (AccessibilityViewModel.settingTask.value != true) {/*清除所有准备要进行的消息*/
//                    AccessibilityViewModel.settingTask.value = true
//                }
//            }.show()

        if (AccessibilityViewModel.normalStartService.value != true) {
            AccessibilityViewModel.normalStartService.value = true
        }
        if (AccessibilityViewModel.settingTask.value != true) {/*清除所有准备要进行的消息*/
            AccessibilityViewModel.settingTask.value = true
        }
    }
}