package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import cn.com.auto.thkl.App
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
                    runTime++
                    val targetList =
                        App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("阅阅赚")
                    if (targetList.isEmpty()) {
                        return@runEvent
                    }
                    runTime++
                    val target = targetList[0]
                    target?.performAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                }

            }

        }
    }

    override var currentStep: Int = 1
    override var runTime: Int = 120
}