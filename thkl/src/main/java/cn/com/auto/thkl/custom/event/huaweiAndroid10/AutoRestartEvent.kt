package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty

@RequiresApi(Build.VERSION_CODES.P)
class AutoRestartEvent(override val task: TaskProperty) :
    EventAction("自动重启", EventController.SYSTEM_EVENT) {
    override var runTime: Int = 60
    override var currentStep = 1
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent{
                    App.service!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                    currentStep++
                }
            }
            2 -> {
                if (event!!.className == "android.widget.RelativeLayout" && event.packageName == "android") {
                    runEvent{
                        val rootNodeInfo: AccessibilityNodeInfo = App.service!!.rootInActiveWindow!!
                        val target =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("androidhwext:id/reboot")[0]
                        if (target != null) {
                            currentStep++
                            type = EventController.TOUCH_EVENT
                            target.getBoundsInScreen(rect)
                            clickPoint(
                                ((rect.right + rect.left) / 2).toFloat(),
                                ((rect.bottom + rect.top) / 2).toFloat(),
                                service = service,
                            )
                        }
                    }
                }
            }
            3->{
                runEvent{
                    val rootNodeInfo: AccessibilityNodeInfo = App.service!!.rootInActiveWindow!!
                    val target =
                        rootNodeInfo.findAccessibilityNodeInfosByViewId("androidhwext:id/icon_frame")[0]
                    if (target != null) {
                        currentStep++
                        target.getBoundsInScreen(rect)
                        clickPoint(
                            ((rect.right + rect.left) / 2).toFloat(),
                            ((rect.bottom + rect.top) / 2).toFloat(),
                            service = service,
                        )
                        runEvent{
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                        }
                    }
                }
            }
        }
    }



}