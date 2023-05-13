package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.media.projection.MediaProjectionManager
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
class AutoCaptureEvent(
    private val activity: Activity,
    private val mediaProjectionManager: MediaProjectionManager,
    override val task: TaskProperty
) :
    EventAction("自动录制授权", EventController.SYSTEM_EVENT) {
    override var currentStep = 1

    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    currentStep++
                    activity.startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(), 1
                    )
                }
            }
            2 -> {
                L.e("2---${event!!.className}")
                if (event.className == "com.android.systemui.media.MediaProjectionPermissionActivity") {
                    runEvent {
                        val rootNodeINfo = App.service!!.rootInActiveWindow
                        val targetList = rootNodeINfo!!.findAccessibilityNodeInfosByText("立即开始")
                        if (targetList.isEmpty()){
                            return@runEvent
                        }
                        val target =targetList[0]
                        target!!.getBoundsInScreen(rect)
                        clickPoint(
                            ((rect.right + rect.left) / 2).toFloat(),
                            ((rect.bottom + rect.top) / 2).toFloat(),
                            service = service,
                            event
                        )
                        runEvent{
                            EventController.INSTANCE.removeEvent(this,MsgType.SUCCESS)
                        }
                    }
                }

            }
        }
    }



    override var runTime: Int = 60

}