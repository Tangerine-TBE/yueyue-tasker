package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.model.AccessibilityViewModel

class AutoCheckAlpayEvent(override val task: TaskProperty) :
    EventAction("支付宝登录检测", EventController.SYSTEM_EVENT), Event {


    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                currentStep++
                var packAgeName = "com.eg.android.AlipayGphone"
                val intent = App.service.packageManager.getLaunchIntentForPackage(packAgeName)
                if (intent == null) {
                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
//                    AccessibilityViewModel.report.postValue("没有安装支付宝!")
                    return
                }
                val resolveInfo = App.service.packageManager.resolveActivity(intent!!, 0)
                val activityName = resolveInfo?.activityInfo?.name.toString()
                packAgeName = resolveInfo?.activityInfo?.packageName.toString()
                val intentMain = Intent()
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.component = ComponentName(packAgeName, activityName)
                App.service.startActivity(intentMain)
            }

            2 -> {
                if (event!!.packageName == "com.eg.android.AlipayGphone") {
                    runEvent({
                        val rootNodeInfo = App.service.rootInActiveWindow
                        val targetList = rootNodeInfo?.findAccessibilityNodeInfosByText("我的")
                        if (targetList!!.isEmpty()) {
                            /*没有登录*/
//                            AccessibilityViewModel.report.value = "支付宝没有登录"
                        }
                        EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                    },2f)
                }
            }

        }
    }



    override var currentStep: Int = 1
    override var runTime: Int = 180
}