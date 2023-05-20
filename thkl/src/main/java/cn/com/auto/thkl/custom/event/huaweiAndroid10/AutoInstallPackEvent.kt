package cn.com.auto.thkl.custom.event.huaweiAndroid10

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import cn.com.auto.thkl.App
import cn.com.auto.thkl.BuildConfig
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.model.AccessibilityViewModel
import java.io.File
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.P)
class AutoInstallPackEvent(
    override val task: TaskProperty
) : EventAction("自动安装:${task.appName}", EventController.SYSTEM_EVENT) {
    override var currentStep = 1
    override var isWorking: Boolean = false
    private var threadController = false
    override var runTime: Int = 180
    override fun start(service: AccessibilityService, event: AccessibilityEvent?) {
        when (currentStep) {
            1 -> {
                runEvent {
                    val intent1 = Intent()
                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val uri: Uri = FileProvider.getUriForFile(
                        App.service,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        File(task.pathName)
                    )
                    intent1.setDataAndType(uri, "application/vnd.android.package-archive")
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.service.startActivity(intent1)
                    currentStep++
                }
            }

            2 -> {
                if (event!!.className == "android.app.Dialog") {
                    runEvent {
                        val target =
                            service!!.rootInActiveWindow.findAccessibilityNodeInfosByText("安装程序")[0]
                        if (target != null) {
                            target.parent.getBoundsInScreen(rect)
                            currentStep++
                            type = EventController.TOUCH_EVENT
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
                if (event!!.className == "android.widget.LinearLayout") {/*已经勾选了*/
                    runEvent {
                        val window = service.rootInActiveWindow
                        val target =
                            window.findAccessibilityNodeInfosByViewId("android:id/button_always")[0]
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

            4 -> {
                if (event!!.className == "androidx.recyclerview.widget.RecyclerView" || event.className == "android.widget.ExpandableListView") {
                    runEvent {
                        val windowNodeInfo = service!!.rootInActiveWindow
                        type = EventController.SYSTEM_EVENT
                        thread {
                            while (!threadController) {
                                runTime++
                                Thread.sleep(1000)
                                val targetList =
                                    windowNodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1")
                                if (targetList.isNotEmpty()) {
                                    val target = targetList[0]
                                    if (target != null) {
                                        if (target.text == "继续安装") {
                                            target.getBoundsInScreen(rect)
                                            currentStep++
                                            type = EventController.SYSTEM_EVENT
                                            threadController = true
                                            clickPoint(
                                                ((rect.right + rect.left) / 2).toFloat(),
                                                ((rect.bottom + rect.top) / 2).toFloat(),
                                                service = service,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                thread {
                    while (threadController) {
                        runTime++
                        Thread.sleep(1000)
                    }
                }
                if (event!!.packageName == "com.android.packageinstaller" && event.className == "com.android.packageinstaller.InstallSuccess") {
                    runEvent {
                        threadController = false
                        EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                    }
                } else if (event.packageName == "com.android.packageinstaller" && event.className == "com.android.packageinstaller.InstallFailed") {
                    runEvent {
                        threadController = false
                        AccessibilityViewModel.report.postValue("安装失败")
                        EventController.INSTANCE.removeEvent(this, MsgType.FAILURE)
                    }
                }else if (event.packageName == "com.huawei.appmarket" && event.className == "com.huawei.appgallery.systeminstalldistservice.adsview.activity.InstallSuccessActivity"){
                    runEvent {
                        threadController = false
                        EventController.INSTANCE.removeEvent(this, MsgType.SUCCESS)
                    }
                }
            }
        }
    }
}
