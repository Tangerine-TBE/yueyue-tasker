package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import cn.com.auto.thkl.utils.L
import kotlin.concurrent.thread


/**当开启无障碍服务时，开启此次事件
 * 1.一般定义在初始化事件时，全局只做一次。*/
@RequiresApi(Build.VERSION_CODES.P)
class FirstStartAccessibilityEvent(
    override val task: TaskProperty
) : EventAction("无障碍服务启动时间", EventController.SYSTEM_EVENT) {

    override var currentStep = 1
    override var runTime: Int = 60
    private var shouldQuick = false

    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    val rootWindow = App.service.rootInActiveWindow
                    if (rootWindow != null) {
                        val targetList = rootWindow.findAccessibilityNodeInfosByText("应用市场")
                        if (targetList!!.isNotEmpty()) {
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                            val intent = Intent(App.service, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            App.service.startActivity(intent)
                            return@runEvent
                        }
                    }
                    back(service)
                    runEvent({
                        back(service)
                    },0.2f)
                }

            }
        }
    }

}


