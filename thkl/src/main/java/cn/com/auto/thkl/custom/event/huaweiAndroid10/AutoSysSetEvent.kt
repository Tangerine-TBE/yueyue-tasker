package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import com.blankj.utilcode.util.ActivityUtils

class AutoSysSetEvent(override val task: TaskProperty) :
    EventAction("改变虚拟按键", EventController.SYSTEM_EVENT), Event {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            0 -> {
                runEvent {
                    currentStep++
                    App.service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                }
            }

            1 -> {
                runEvent {
                    val componentName =
                        ComponentName("com.android.settings", "com.android.settings.HWSettings")
                    val intent = Intent()
                    intent.component = componentName
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ActivityUtils.startActivity(intent)
                    currentStep++
                }
            }

            2 -> {
                if ((event!!.className == "com.android.settings.HWSettings" && event.packageName == "com.android.settings") || (event.className == "android.widget.FrameLayout" && event.packageName == "cn.com.auto.thkl")) {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val dashboardContainer =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/dashboard_container")
                        if (dashboardContainer.isEmpty()) {
                            return@runEvent
                        }
                        val rvNodeInfo = dashboardContainer[0]
                        val list = rvNodeInfo.findAccessibilityNodeInfosByText("系统和更新")
                        if (list.isEmpty()) {
                            currentStep++
                            type = EventController.TOUCH_EVENT
                            scrollDownPoint(rvNodeInfo, service, event)
                        } else {
                            val target = list[0]
                            target!!.getBoundsInScreen(rect)
                            currentStep = 4
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
                if (event!!.className == "androidx.recyclerview.widget.RecyclerView") {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val dashboardContainer =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/dashboard_container")
                        if (dashboardContainer.isEmpty()) {
                            return@runEvent
                        }
                        val rvNodeInfo = dashboardContainer[0]
                        val list = rvNodeInfo.findAccessibilityNodeInfosByText("系统和更新")
                        if (list.isEmpty()) {
                            scrollDownPoint(rvNodeInfo, service, event)
                        } else {
                            val target = list[0]
                            target!!.getBoundsInScreen(rect)
                            currentStep = 4
                            type = EventController.SYSTEM_EVENT
                            clickPoint(
                                ((rect.right + rect.left) / 2).toFloat(),
                                ((rect.bottom + rect.top) / 2).toFloat(),
                                service = service,
                            )
                        }
                    }
                }
            }

            4 -> {
                if (event!!.packageName == "com.android.settings" && event.className == "com.android.settings.Settings\$HwSystemDashboardActivity") {
                    runEvent {
                        val targetList =
                            App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("系统导航方式")
                        if (targetList.isEmpty()) {
                            return@runEvent
                        }
                        val target = targetList[0]
                        if (target != null) {
                            target.getBoundsInScreen(rect)
                            currentStep++
                            clickPoint(service)
                        }
                    }
                }
            }

            5 -> {
                if (event!!.packageName == "com.android.settings" && event.className == "com.android.settings.navigation.NaviTypeChooseActivity") {
                    runEvent {
                        val targetList =
                            App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("屏幕外物理导航键")
                        if (targetList.isEmpty()) {
                            return@runEvent
                        }
                        val target = targetList[0]
                        if (target != null) {
                            target.getBoundsInScreen(rect)
                            clickPoint(service)
                            runEvent {
                                EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                            }
                        }
                    }
                }
            }
        }
    }

    var index = 0
    override var currentStep: Int = 0
    override var runTime: Int = 120
}