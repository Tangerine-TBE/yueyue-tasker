package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.autojs.AutoJs
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.utils.SP
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.stardust.autojs.core.activity.ActivityInfoProvider
import kotlin.concurrent.thread


/**
 * 1.com.android.settings/com.android.settings.HWSettings
 * 2.com.android.settings/com.android.settings.Settings$AppAndNotificationDashboardActivity
 * 3.com.huawei.systemmanager/com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity*/
@RequiresApi(Build.VERSION_CODES.P)
class AutoStartSetEvent(override val task: TaskProperty) :
    EventAction("自动自启动授权", EventController.SYSTEM_EVENT) {
    override var currentStep = 1
    private var beginFind = false
    override var runTime: Int = 500
    override var isWorking: Boolean = false
    private var index = 0


    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    if (AutoJs.getInstance().infoProvider.latestActivity == "com.android.settings.HWSettings"){
                        currentStep++
                        event!!.className = "com.android.settings.HWSettings"
                        event.packageName = "com.android.settings"
                        start(service,event)
                    }else{
                        val componentName =
                            ComponentName("com.android.settings", "com.android.settings.HWSettings")
                        val intent = Intent()
                        intent.component = componentName
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        currentStep++
                    }

                }

            }
            2 -> {

                if ((event!!.className == "com.android.settings.HWSettings" && event.packageName == "com.android.settings") ||(event.className == "android.widget.FrameLayout" &&event.packageName == "cn.com.auto.thkl")) {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val dashboardContainer =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/dashboard_container")
                        if (dashboardContainer.isEmpty()) {
                            return@runEvent
                        }
                        val rvNodeInfo = dashboardContainer[0]
                        val list = rvNodeInfo.findAccessibilityNodeInfosByText("应用")
                        /*可能出现找不到的情况*/
                        if (!beginFind) {
                            index++
                            currentStep++
                            type = EventController.TOUCH_EVENT
                            scrollUpPoint(rvNodeInfo, service, event)
                            thread {
                                Thread.sleep(3000)
                                if (index != 0) {
                                    beginFind = true
                                    if (list.isEmpty()) {
                                        scrollDownPoint(rvNodeInfo, service, event)
                                    } else {
                                        val target = list[0]
                                        target!!.getBoundsInScreen(rect)
                                        currentStep = 4
                                        clickPoint(
                                            ((rect.right + rect.left) / 2).toFloat(),
                                            ((rect.bottom + rect.top) / 2).toFloat(),
                                            service = service,
                                            event
                                        )
                                    }
                                }

                            }
                        } else {
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
                                    event
                                )
                            }
                        }


                    }
                }
            }

            3 -> {
                if (event!!.className == "androidx.recyclerview.widget.RecyclerView") {
                    runEvent {
                        if (index > 0) {
                            index = 0
                        }
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val dashboardContainer =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/dashboard_container")
                        if (dashboardContainer.isEmpty()) {
                            return@runEvent
                        }
                        val rvNodeInfo = dashboardContainer[0]
                        val list = rvNodeInfo.findAccessibilityNodeInfosByText("应用")
                        if (!beginFind) {
                            scrollUpPoint(rvNodeInfo, service, event)
                            thread {
                                Thread.sleep(3000)
                                beginFind = true
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
                                        event
                                    )
                                }
                            }
                        } else {
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
                                    service = service!!,
                                    event
                                )
                            }
                        }


                    }
                }
            }

            4 -> {
                if (event!!.className == "com.android.settings.Settings\$AppAndNotificationDashboardActivity" && event.packageName == "com.android.settings") {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val lists = rootNodeInfo.findAccessibilityNodeInfosByText("应用启动管理")
                        if (lists.isEmpty()) {
                            return@runEvent
                        }
                        val target =
                            lists[0]
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

            5 -> {
                /*需要优化*/
                if (event!!.className == "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity" && event.packageName == "com.huawei.systemmanager") {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val targetList =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("android:id/search_src_text")
                        if (targetList.isEmpty()){
                            return@runEvent
                        }
                       val target =  targetList[0]
                        target!!.getBoundsInScreen(rect)
                        clickPoint(
                            ((rect.right + rect.left) / 2).toFloat(),
                            ((rect.bottom + rect.top) / 2).toFloat(),
                            service = service,
                            event
                        )
                        App.handler.postDelayed({
                            val arguments = Bundle()
                            arguments.putCharSequence(
                                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                service.getString(R.string.app_name)
                            )
                            type = EventController.TOUCH_EVENT
                            currentStep++
                            target!!.performAction(
                                AccessibilityNodeInfo.ACTION_SET_TEXT, arguments
                            )
                        }, 1000)
                    }

                }
            }

            6 -> {
                if (event!!.className == "android.widget.ListView") {
                    runEvent {
                        val rootNodeInfo = App.service!!.rootInActiveWindow!!
                        val targetList =
                            rootNodeInfo.findAccessibilityNodeInfosByViewId("com.huawei.systemmanager:id/text_layout_id")
                        if (targetList.isEmpty()){
                            return@runEvent
                        }
                        val target = targetList[0]
                        type = EventController.SYSTEM_EVENT
                        target.getBoundsInScreen(rect)
                        currentStep++
                        clickPoint(
                            ((rect.right + rect.left) / 2).toFloat(),
                            ((rect.bottom + rect.top) / 2).toFloat(),
                            service = service,
                            event
                        )
                        runEvent {
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

            7 -> {
                if (event!!.className == "android.app.AlertDialog" && event!!.packageName == "com.huawei.systemmanager") {
                    runEvent {
                        val rootNode = App.service!!.rootInActiveWindow!!
                        val switchList =
                            rootNode.findAccessibilityNodeInfosByViewId("com.huawei.systemmanager:id/switcher")
                        if (switchList.isEmpty()) {
                            return@runEvent
                        }
                        val switchAutoStart = switchList[0]
                        if (switchList.size != 3) {
                            return@runEvent
                        }
                        val switchBackStart = switchList[2]
                        val btnConfirm =
                            rootNode.findAccessibilityNodeInfosByViewId("android:id/button1")[0]
                        runEvent {
                            if (!switchAutoStart.isChecked) {
                                switchAutoStart.getBoundsInScreen(rect)
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                    event
                                )
                            }
                            runEvent {
                                if (!switchBackStart.isChecked) {
                                    switchBackStart.getBoundsInScreen(rect)
                                    clickPoint(
                                        ((rect.right + rect.left) / 2).toFloat(),
                                        ((rect.bottom + rect.top) / 2).toFloat(),
                                        service = service,
                                        event
                                    )
                                }
                                runEvent {
                                    btnConfirm.getBoundsInScreen(rect)
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
            }

            8 -> {
                SP.putBoolean(Constant.AUTO_START, true)
                EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
            }
        }
    }

}