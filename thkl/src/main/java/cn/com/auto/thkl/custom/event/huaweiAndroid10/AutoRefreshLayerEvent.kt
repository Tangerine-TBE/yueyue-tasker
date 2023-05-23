package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import cn.com.auto.thkl.App
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.activity.VerificationActivity
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty

class AutoRefreshLayerEvent(override val task: TaskProperty) :
    EventAction("自动刷新界面", EventController.SYSTEM_EVENT), Event {
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    runTime++
                    currentStep++
                    App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                }

            }

            2 -> {
                runEvent {
                    val intent = Intent()
                    intent.setClass(App.service, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.service.startActivity(intent)
                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                }

            }

        }
    }

    override var currentStep: Int = 1
    override var runTime: Int = 120
}