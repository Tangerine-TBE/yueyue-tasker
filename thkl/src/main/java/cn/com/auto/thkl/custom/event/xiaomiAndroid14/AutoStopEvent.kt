package cn.com.auto.thkl.custom.event.xiaomiAndroid14

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty

@RequiresApi(Build.VERSION_CODES.P)
class AutoStopEvent(override val task: TaskProperty) :
    EventAction("自动停止应用-${task.appName}", EventController.SYSTEM_EVENT) {
    override var currentStep = 1

    override var isWorking: Boolean = false
    override var runTime: Int = 180

    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    if (TextUtils.isEmpty(task.packName)) {
                        EventController.INSTANCE.removeEvent(this, MsgType.FAILURE)
                        return@runEvent
                    }
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts(
                        "package", task.packName, null
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.service!!.startActivity(intent)
                    currentStep++
                    runEvent(Runnable {App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)  },2f)
                }
            }

            2 -> {
                if (event!!.className == "com.android.settings.applications.InstalledAppDetailsTop" && event.packageName == "com.android.settings") {
                    runEvent ({
                        val rootNodeInfo = App.service.rootInActiveWindow
                        val targetList =
                            rootNodeInfo!!.findAccessibilityNodeInfosByViewId("com.android.settings:id/right_button")
                        if (targetList.isEmpty()) {
                            return@runEvent
                        }
                        val target = targetList[0]
                        if (target != null) {
                            if (target.isEnabled) {
                                target.getBoundsInScreen(rect)
                                currentStep++
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                )
                            }else{
                                EventController.INSTANCE.removeEvent(
                                    this, MsgType.SUCCESS
                                )/*开启下一个任务*/
                            }
                        }
                    },1.5f)
                }
            }

            3 -> {
                if (event!!.className == "android.app.AlertDialog" && event.packageName == "com.android.settings") {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow
                        val targetList =
                            rootNodeInfo!!.findAccessibilityNodeInfosByViewId("android:id/button1")
                        if (targetList.isEmpty()) {
                            return@runEvent
                        }
                        val target = targetList[0]
                        if (target != null) {
                            if (target.isClickable) {
                                target.getBoundsInScreen(rect)
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                )
                                runEvent{
                                    EventController.INSTANCE.removeEvent(
                                        this, MsgType.SUCCESS
                                    )/*开启下一个任务*/
                                }

                            }
                        }
                    }
                }
            }
        }
    }


}