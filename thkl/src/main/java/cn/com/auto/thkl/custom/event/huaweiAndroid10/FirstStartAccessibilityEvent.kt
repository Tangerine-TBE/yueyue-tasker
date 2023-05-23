package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.activity.SplashActivity
import cn.com.auto.thkl.activity.VerificationActivity
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty


/**当开启无障碍服务时，开启此次事件
 * 1.一般定义在初始化事件时，全局只做一次。*/
@RequiresApi(Build.VERSION_CODES.P)
class FirstStartAccessibilityEvent(
    override val task: TaskProperty
) : EventAction("无障碍服务启动", EventController.SYSTEM_EVENT) {

    override var currentStep = 1
    override var runTime: Int = 60
    private var shouldQuick = false
    private var index = 2

    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    val rootWindow = App.service.rootInActiveWindow
                    if (rootWindow != null) {
                        val targetList = rootWindow.findAccessibilityNodeInfosByViewId("com.huawei.android.launcher:id/launcher_root_view")
                        if (targetList!!.isNotEmpty()) {
                            val intent = Intent(App.service, VerificationActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            App.service.startActivity(intent)
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                            return@runEvent
                        }
                    }
                    if (index == 0){
                        /*理论上是要进行应用市场逻辑跳转*/
                        App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                    }else{
                        back(service)
                        index--
                    }

                }
//                runEvent{
//                    val intent = Intent()
//                    intent.setClass(App.service,VerificationActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    App.service.startActivity(intent)
//                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
//                }
            }
        }
    }

}


