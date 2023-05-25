package cn.com.auto.thkl.custom.event.xiaomiAndroid14

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty

class AutoClearCacheEvent(
    override val task: TaskProperty
) : EventAction("自动清除缓存", EventController.SYSTEM_EVENT), Event {
    private val buttonList =
        arrayListOf("com.android.settings:id/button_2")

    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    val intent = Intent()
                    intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:${task.packName}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    App.service!!.startActivity(intent)
                    currentStep++
                }
            }

            2 -> {
                if (event!!.className == "com.android.settings.applications.InstalledAppDetailsTop" || event.className == "androidx.recyclerview.widget.RecyclerView") {
                    runEvent {
                        val targetList =
                            service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("存储")
                        if (targetList.isEmpty()) {
                            return@runEvent
                        }
                        val target = targetList[0]
                        if (target != null) {
                            target.getBoundsInScreen(rect)
                            currentStep++
                            clickPoint(
                                ((rect.right + rect.left) / 2).toFloat(),
                                ((rect.bottom + rect.top) / 2).toFloat(),
                                service = service,
                            )
                        }
                    }
                } else {
                    if (event.className == "com.android.packageinstaller.permission.ui.ManagePermissionsActivity" && event.packageName == "com.android.permissioncontroller") {
                        runEvent {
                            back(service)
                        }
                    }

                }
            }

            3 -> {
                if (event?.className == "com.android.settings.SubSettings" && event.packageName == "com.android.settings") {
                    runEvent {
                        val rootView = App.service.rootInActiveWindow
                        if (buttonList.size > 0) {
                            rootView?.findAccessibilityNodeInfosByViewId(buttonList[0]).apply {
                                if (this != null) {
                                    if (this.isNotEmpty()) {
                                        buttonList.remove(buttonList[0])
                                        val target = this[0]
                                        if (target.isEnabled){
                                            currentStep++
                                            target.getBoundsInScreen(rect)
                                            clickPoint(service)
                                        }else{
                                            EventController.INSTANCE.removeEvent(this@AutoClearCacheEvent, MsgType.SUCCESS)
                                        }
                                    }
                                }
                            }
                        } else {
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                        }
                    }
                }
            }

            4 -> {
                if (event?.className == "android.app.AlertDialog" && event.packageName == "com.android.settings") {
                    runEvent {
                        val rootView = App.service.rootInActiveWindow
                        rootView?.findAccessibilityNodeInfosByViewId("android:id/button1").apply {
                            if (this != null) {
                                if (this.isNotEmpty()) {
                                    currentStep--
                                    val target = this[0]
                                    target.getBoundsInScreen(rect)
                                    clickPoint(service)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override var currentStep: Int = 1
    override var runTime: Int = 1
}