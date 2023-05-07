package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.Event
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.task.TaskProperty

class AutoLieBaoEvent(override val task: TaskProperty) :
    EventAction("猎豹清理执行", EventController.SYSTEM_EVENT), Event {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                currentStep++
                var packAgeName = "com.cleanmaster.mguard_cn"
                val intent = App.service.packageManager.getLaunchIntentForPackage(packAgeName)
                val resolveInfo = App.service.packageManager.resolveActivity(intent!!, 0)
                val activityName = resolveInfo?.activityInfo?.name.toString()
                packAgeName = resolveInfo?.activityInfo?.packageName.toString()
                val intentMain = Intent()
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentMain.component = ComponentName(packAgeName, activityName)
                App.service.startActivity(intentMain)
            }

            2 -> {
                if (event!!.className == "com.keniu.security.splash.SplashActivity") {
                    findViewById("com.cleanmaster.mguard_cn:id/ami")?.getBoundsInScreen(rect)
                    clickPoint(service,event)
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