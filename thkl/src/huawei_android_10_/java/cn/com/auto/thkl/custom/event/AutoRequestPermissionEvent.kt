package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.utils.L
import cn.com.auto.thkl.utils.SP
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.P)
class AutoRequestPermissionEvent(
    override val task: TaskProperty
) : EventAction("自动权限申请-${task.packName}", EventController.SYSTEM_EVENT) {
    override var currentStep = 1
    private var index = 0
    override var runTime: Int = 180
    private val titleStringList = listOf("已允许", "已禁止")
    override var isWorking: Boolean = false
    private var beginFind = false
    private val clickedStringList = arrayListOf<String>()

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
                            service.rootInActiveWindow!!.findAccessibilityNodeInfosByText("权限")
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
                                event
                            )
                        }
                    }
                }
            }

            3 -> {
                if (event!!.className == "com.android.packageinstaller.permission.ui.ManagePermissionsActivity" && event.packageName == "com.android.permissioncontroller") {
                    runEvent {
                        val windowInfo = service.rootInActiveWindow
                        val nodeInfoList =
                            windowInfo.findAccessibilityNodeInfosByViewId("android:id/title")
                        if (nodeInfoList.isEmpty()) {
                            return@runEvent
                        }
                        val rvNodeInfoList =
                            windowInfo.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/list")
                        if (rvNodeInfoList.isEmpty()) {
                            return@runEvent
                        }
                        if (!beginFind) {
                            /*向上滚动，直到没有新目标出现*/
                            type = EventController.TOUCH_EVENT
                            index++
                            scrollUpPoint(rvNodeInfoList[0], service, event)
                            thread {
                                Thread.sleep(3000)
                                if (index != 0) {
                                    beginFind = true
                                    type = EventController.SYSTEM_EVENT
                                    scrollDownAndFind(nodeInfoList, service, event, rvNodeInfoList)
                                }
                            }
                        } else {
                            scrollDownAndFind(nodeInfoList, service, event, rvNodeInfoList)
                        }

                    }
                } else if (event.className == "androidx.recyclerview.widget.RecyclerView" && event.packageName == "com.android.permissioncontroller") {/*滑动发生变化*/
                    runEvent {
                        if (index > 0) {
                            index = 0
                            L.e(/* str = */ "index${index}")

                        }
                        val windowInfo = service.rootInActiveWindow
                        val nodeInfoList =
                            windowInfo.findAccessibilityNodeInfosByViewId("android:id/title")
                        if (nodeInfoList.isEmpty()) {
                            return@runEvent
                        }

                        val rvNodeInfoList =
                            windowInfo.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/list")
                        if (rvNodeInfoList.isEmpty()) {
                            return@runEvent
                        }
                        if (!beginFind) {
                            index++
                            scrollUpPoint(rvNodeInfoList[0], service, event)
                            thread {
                                Thread.sleep(3000)
                                if (index != 0) {
                                    beginFind = true
                                    scrollDownAndFind(nodeInfoList, service, event, rvNodeInfoList)
                                }
                            }
                        } else {
                            currentStep++
                            for (i in 0 until nodeInfoList.size) {
                                val currentString = nodeInfoList[i].text.toString()
                                if (!clickedStringList.contains(currentString) && !titleStringList.contains(
                                        currentString
                                    )
                                ) {
                                    clickedStringList.add(currentString)
                                    nodeInfoList[i].getBoundsInScreen(rect)
                                    type = EventController.SYSTEM_EVENT
                                    clickPoint(
                                        ((rect.right + rect.left) / 2).toFloat(),
                                        ((rect.bottom + rect.top) / 2).toFloat(),
                                        service = service,
                                        event
                                    )
                                    return@runEvent
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                if (event!!.className == "com.android.packageinstaller.permission.ui.AppPermissionActivity" && event.packageName == "com.android.permissioncontroller") {
                    runEvent {
                        val windowInfo = service.rootInActiveWindow
                        val allowRadioButtons =
                            windowInfo.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/allow_radio_button")
                        val onlyRadioButtons =
                            windowInfo.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/foreground_only_radio_button")

                        if (allowRadioButtons.size > 0) {
                            if (!allowRadioButtons[0].isChecked) {
                                allowRadioButtons[0].getBoundsInScreen(rect)
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                    event
                                )
                            }
                        } else if (onlyRadioButtons.size > 0) {
                            if (!onlyRadioButtons[0].isChecked) {
                                onlyRadioButtons[0].getBoundsInScreen(rect)
                                clickPoint(
                                    ((rect.right + rect.left) / 2).toFloat(),
                                    ((rect.bottom + rect.top) / 2).toFloat(),
                                    service = service,
                                    event
                                )
                            }
                        }
                        App.handler.postDelayed({
                            currentStep--
                            back(service)
                        }, 1000)
                    }
                }
            }
        }
    }

    private fun scrollDownAndFind(
        nodeInfoList: MutableList<AccessibilityNodeInfo>,
        service: AccessibilityService,
        event: AccessibilityEvent?,
        rvNodeInfoList: MutableList<AccessibilityNodeInfo>
    ) {
        currentStep++
        for (i in 0 until nodeInfoList.size) {
            val currentString = nodeInfoList[i].text.toString()
            if (!clickedStringList.contains(currentString) && !titleStringList.contains(
                    currentString
                )
            ) {
                clickedStringList.add(currentString)
                nodeInfoList[i].getBoundsInScreen(rect)
                clickPoint(
                    ((rect.right + rect.left) / 2).toFloat(),
                    ((rect.bottom + rect.top) / 2).toFloat(),
                    service = service,
                    event!!
                )
                return
            } else {
                if (i == nodeInfoList.size - 1) {/*滚动*/
                    val rvNodeInfo = rvNodeInfoList[0]
                    currentStep--
                    type = EventController.TOUCH_EVENT
                    index++
                    scrollDownPoint(rvNodeInfo, service, event!!)
                    thread {
                        Thread.sleep(3000)
                        if (index != 0) {
                            L.e(/* str = */ "index${index}")
                            SP.putBoolean(Constant.PERMISSION, true)
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                        }
                    }
                }
            }
        }
    }
}