package cn.com.auto.thkl.custom.event

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import com.stardust.app.permission.DrawOverlaysPermission

@RequiresApi(Build.VERSION_CODES.P)
class AutoOverLayerEvent(override val task: TaskProperty) : EventAction("自动申请上层显示", EventController.SYSTEM_EVENT) {


    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when(currentStep){
            1 ->{
                runEvent({
                   currentStep++
                   val intent = DrawOverlaysPermission.getCanDrawOverlaysIntent(task.packName)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.service.startActivity(intent)
                },2)
            }
            2 ->{
                if (event!!.className == "com.android.settings.Settings\$AppDrawOverlaySettingsActivity" ){
                    runEvent{
                        val target = App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId("android:id/switch_widget")[0]
                        if (target != null){
                            target.getBoundsInScreen(rect)
                            type = EventController.TOUCH_EVENT
                            currentStep++
                            clickPoint(service,event)
                        }
                    }
                }
            }
            3->{
                runEvent{
                    val target = App.service.rootInActiveWindow!!.findAccessibilityNodeInfosByViewId("android:id/switch_widget")[0]
                    if (target != null){
                        if (target.isChecked){
                            currentStep++
                            EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)/*开启下一个任务*/
                        }
                    }
                }
            }
        }
    }



    override var currentStep: Int = 1
    override var runTime: Int = 20
}