package cn.com.auto.thkl.custom.event.xiaomiAndroid14

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import kotlinx.coroutines.DelicateCoroutinesApi

@RequiresApi(Build.VERSION_CODES.P)
class AutoUninstallEvent(
    override val task: TaskProperty
) : EventAction("自动卸载应用-${task.appName}", EventController.SYSTEM_EVENT) {
    override var currentStep = 1



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
                if (event!!.className == "com.android.settings.applications.InstalledAppDetailsTop" && event.packageName == "com.android.settings") {
                    runEvent {
                      val targetList =App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("卸载")
                        if (targetList.isEmpty()){
                            return@runEvent
                        }
                        val target =targetList[0]
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
                }
            }

            3 -> {
                if (event!!.className == "com.android.packageinstaller.UninstallerActivity" && event.packageName == "com.android.packageinstaller") {
                    runEvent {
                        val targetList =  App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId("android:id/button1")
                        if (targetList.isEmpty()){
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

                }
            }
            4 ->{
                runEvent{
                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                }
            }

        }
    }


    override var runTime: Int = 180
}