package cn.com.auto.thkl.custom.event.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.custom.event.*
import cn.com.auto.thkl.custom.event.huaweiAndroid10.AutoSysSetEvent
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import com.blankj.utilcode.util.AppUtils
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
private const val CLAZZ_PARENT_PACK = "cn.com.auto.thkl.custom.event."
@RequiresApi(Build.VERSION_CODES.P)
object SuspendEventManager {
    private fun getEvent(clazzName: String, scope: TaskProperty): Event {
        val clazz = Class.forName("$CLAZZ_PARENT_PACK${App.EVENT_PACK_NAME}.${clazzName}")
        val constructor = clazz.getConstructor(scope::class.java)
        return constructor.newInstance(scope) as Event

    }
    fun getEvent(clazzName: String, activity: LoginActivity, mediaProjectionManager:MediaProjectionManager, scope: TaskProperty):Event{
        val clazz = Class.forName("$CLAZZ_PARENT_PACK${App.EVENT_PACK_NAME}.${clazzName}")
        val constructor = clazz.getConstructor(activity::class.java,mediaProjectionManager::class.java,scope::class.java,)
        return constructor.newInstance(activity,mediaProjectionManager,scope) as Event
    }

    suspend fun suspendAutoShutDownEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoShutDownEvent", scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoStartTaskEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoStartSetEvent", scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoClearCacheEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoClearCacheEvent", scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoOverLayerEvent(job: Job, context: Context) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                getEvent(
                    "AutoOverLayerEvent", TaskProperty(
                        TaskType.AUTO_OVER_LAYER_TASK,
                        context.packageName,
                        "",
                        "",
                        false,
                        job,
                        AppUtils.getAppName()
                    )
                )
            ).execute(object : EventAction.OnEventCompleted {
                override fun eventCompleted(name: String) {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    continuation.resume(name)
                }
            })
        }
    }

    suspend fun suspendAutoSysEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoSysSetEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }


    suspend fun suspendAutoPermissionEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoRequestPermissionEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoLieBaoEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoLieBaoEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoClearEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoClearEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoCheckWXEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoCheckWXLoginEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoCheckAliPayEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoCheckAlpayEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoRestartEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoRestartEvent", scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })

        }
    }

    suspend fun suspendAutoUninstallPackEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoUninstallEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoStopEvent(
        taskProperty: TaskProperty
    ) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoStopEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoInstallPackEvent(taskProperty: TaskProperty): String {
        return suspendCoroutine { continuation ->
            EventController.INSTANCE.addEvent(getEvent("AutoInstallPackEvent", taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoPermissionAppsEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                getEvent(
                    "AutoRequestAppsPermissionEvent",
                    taskProperty
                )
            )
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun firstStartEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(getEvent("FirstStartAccessibilityEvent", scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }

    }
}

