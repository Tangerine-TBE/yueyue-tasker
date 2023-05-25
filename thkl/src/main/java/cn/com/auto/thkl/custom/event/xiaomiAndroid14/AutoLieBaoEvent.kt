package cn.com.auto.thkl.custom.event.xiaomiAndroid14

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.EventController.Companion.ALL_EVENT
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.utils.L
import cn.com.auto.thkl.utils.SP
import kotlin.concurrent.thread

class AutoLieBaoEvent(override val task: TaskProperty) :
    EventAction("猎豹清理", EventController.SYSTEM_EVENT), Event {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                currentStep++
                var packAgeName = "com.cleanmaster.mguard_cn"
                val intent = App.service.packageManager.getLaunchIntentForPackage(packAgeName)
                if (intent == null) {
                    EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                    return
                }
                val resolveInfo = App.service.packageManager.resolveActivity(intent!!, 0)
                val activityName = resolveInfo?.activityInfo?.name.toString()
                packAgeName = resolveInfo?.activityInfo?.packageName.toString()
                val intentMain = Intent()
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.component = ComponentName(packAgeName, activityName)
                App.service.startActivity(intentMain)
            }

            2 -> {
                if (event?.className == "com.keniu.security.main.MainActivity") {
                    runEvent {
                        val rootView = App.service.rootInActiveWindow
                        val list =
                            rootView?.findAccessibilityNodeInfosByViewId("com.cleanmaster.mguard_cn:id/a7g")
                        if (list?.isEmpty() == true) {
                            return@runEvent
                        } else {
                            if (list!![0].childCount == 1) {
                                var index = 0
                                while (true) {
                                    Thread.sleep(2000)
                                    runTime++
                                    val target = findViewById("com.cleanmaster.mguard_cn:id/a_2")
                                    val childTarget = target?.getChild(0)
                                    val childText = childTarget?.text
                                    if (childText.toString() == "立即清理") {
                                        target?.getBoundsInScreen(rect)
                                        currentStep++
                                        clickPoint(service)
                                        return@runEvent
                                    }
                                    L.e(childText.toString())
                                    if (index == 5) {
                                        val cancel = findViewById("com.cleanmaster.mguard_cn:id/u7")
                                        cancel?.getBoundsInScreen(rect)
                                        clickPoint(service)
                                    } else if (index == 10) {
                                        EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                                        return@runEvent
                                    }
                                    index++
                                }
                            } else {
                                EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                                return@runEvent
                            }

                        }

                    }
                } else if (event?.className == "com.keniu.security.splash.SplashActivity") {
                    runEvent {
                        val target = findViewById("com.cleanmaster.mguard_cn:id/ami")
                        target?.getBoundsInScreen(rect)
                        clickPoint(service)
                    }
                }

            }

            3 -> {
                if (event?.className == "com.cleanmaster.junk.ui.activity.JunkManagerActivity") {
                    runEvent {
                        while (true) {
                            Thread.sleep(5000)
                            val target = findViewById("com.cm.plugin.core:id/rq")
                            val childTarget = target?.getChild(0)
                            val childText = childTarget?.text
                            if (childText.toString().contains("清理")) {
                                target?.getBoundsInScreen(rect)
                                clickPoint(service)
                                Thread.sleep(5000)
                                EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                                return@runEvent
                            }
                            L.e(childText.toString())
                        }
                    }

                }
            }

        }
    }

    private fun findViewById(id: String): AccessibilityNodeInfo? {
        val targetList = App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId(id)
        return if (targetList.isEmpty()) {
            null
        } else {
            targetList[0]
        }
    }

    private fun findViewByText(text: String): AccessibilityNodeInfo? {
        val targetList = App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId(text)
        return if (targetList.isEmpty()) {
            null
        } else {
            targetList[0]
        }
    }

    override var currentStep: Int = 1
    override var runTime: Int = 120
}