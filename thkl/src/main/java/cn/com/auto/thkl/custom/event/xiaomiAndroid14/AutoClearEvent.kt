package cn.com.auto.thkl.custom.event.xiaomiAndroid14

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.utils.L
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.P)
class AutoClearEvent(override val task: TaskProperty) :
    EventAction("自动清理", EventController.SYSTEM_EVENT) {
    override var currentStep = 1
    override var runTime: Int = 1
    override var isWorking: Boolean = false
    private var isChanged:Boolean = false

    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent{
                    currentStep++
                    App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)/*打开后一直等待*/
                    App.handler.postDelayed ({
                        isChanged = true
                        val targetList =
                            App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId("com.huawei.android.launcher:id/clear_all_recents_image_button")
                        if (targetList.isEmpty()) {
                            App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                            Thread.sleep(1500)
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                        } else {
                            val target = targetList[0]
                            target.getBoundsInScreen(rect)
                            clickPoint(
                                ((rect.right + rect.left) / 2).toFloat(),
                                ((rect.bottom + rect.top) / 2).toFloat(),
                                service = service,
                            )
                            Thread.sleep(1500)
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                        }/*开启下一个任务*/
                    },3000)
                }
            }
            2->{

            }
        }
    }


}