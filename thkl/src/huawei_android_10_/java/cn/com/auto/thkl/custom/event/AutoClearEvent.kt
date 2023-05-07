package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import cn.com.auto.thkl.utils.L

@RequiresApi(Build.VERSION_CODES.P)
class AutoClearEvent(override val task: TaskProperty) : EventAction("自动清理", EventController.SYSTEM_EVENT) {
    override var currentStep = 1
    override var runTime: Int = 1
    override var isWorking: Boolean = false


    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    currentStep++
                    App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                    runEvent({
                        /**出现最近无任务的处理方式*/
                        currentStep++
                        back(service)
                    },3)
                }
            }

            2 -> {
                if (event!!.className == "com.huawei.android.launcher.unihome.UniHomeLauncher" && event.packageName == "com.huawei.android.launcher") {
                    runEvent {
                        val targetList =  service.rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.huawei.android.launcher:id/overview_panel2")
                        if (targetList.isEmpty()){
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                            return@runEvent
                        }
                        var target =targetList[0]
                        L.e("${target!!.childCount}")
                        if (target.childCount > 0) {
                            target = service.rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.huawei.android.launcher:id/clear_all_recents_image_button")[0]
                            if (target != null) {
                                target.getBoundsInScreen(rect)
                                currentStep++
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                    event
                                )
                            }

                        }
                    }
                }
            }
            3 -> {
                if (event!!.className == "com.huawei.android.launcher.unihome.UniHomeLauncher") {
                    runEvent {
                        EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                    }
                }
            }
        }
    }


}