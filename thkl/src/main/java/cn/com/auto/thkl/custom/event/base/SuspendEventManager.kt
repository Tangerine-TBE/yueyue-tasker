package cn.com.auto.thkl.custom.event.base

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.custom.event.*
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.P)
object SuspendEventManager {

    suspend fun suspendAutoShutDownEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoShutDownEvent(scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoStartTaskEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoStartSetEvent(scope))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }

    }

    suspend fun suspendAutoOverLayerEvent(job: Job,context:Context) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                AutoOverLayerEvent(
                    TaskProperty(
                        TaskType.AUTO_OVER_LAYER_TASK,
                        context.packageName,
                        "",
                        "",
                        false,
                        job
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


    suspend fun suspendAutoPermissionEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoRequestPermissionEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }


    }

    suspend fun suspendAutoClearEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoClearEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoCheckWXEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoCheckWXLoginEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoCheckAliPayEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoCheckAlpayEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoRestartEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                AutoRestartEvent(scope)
            ).execute(object : EventAction.OnEventCompleted {
                override fun eventCompleted(name: String) {
                    continuation.resume(name)
                }
            })

        }
    }

    suspend fun suspendAutoUninstallPackEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoUninstallEvent(taskProperty))
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
            EventController.INSTANCE.addEvent(AutoStopEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoInstallPackEvent(taskProperty: TaskProperty):String {
       return  suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoInstallPackEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                         continuation.resume(name)
                    }
                })
        }
    }

    suspend fun suspendAutoPermissionAppsEvent(taskProperty: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(AutoRequestAppsPermissionEvent(taskProperty))
                .execute(object : EventAction.OnEventCompleted {
                    override fun eventCompleted(name: String) {
                        continuation.resume(name)
                    }
                })
        }
    }

    suspend fun firstStartEvent(scope: TaskProperty) {
        suspendCoroutine<String> { continuation ->
            EventController.INSTANCE.addEvent(
                FirstStartAccessibilityEvent(
                    scope
                )
            ).execute(object : EventAction.OnEventCompleted {
                override fun eventCompleted(name: String) {
                    continuation.resume(name)
                }
            })
        }

    }
}

