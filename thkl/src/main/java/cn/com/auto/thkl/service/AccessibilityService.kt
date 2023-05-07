package cn.com.auto.thkl.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import cn.com.auto.thkl.App
import cn.com.auto.thkl.BuildConfig
import cn.com.auto.thkl.Constant
import cn.com.auto.thkl.R
import cn.com.auto.thkl.activity.LoginActivity
import cn.com.auto.thkl.activity.MainActivity
import cn.com.auto.thkl.autojs.AutoJs
import cn.com.auto.thkl.custom.event.*
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.event.base.SuspendEventManager
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.firstStartEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoCheckAliPayEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoCheckWXEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoClearEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoInstallPackEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoOverLayerEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoPermissionAppsEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoPermissionEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoRestartEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoShutDownEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoStartTaskEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoStopEvent
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import cn.com.auto.thkl.floating.FloatBallManager
import cn.com.auto.thkl.floating.floatball.FloatBallCfg
import cn.com.auto.thkl.floating.menu.FloatMenuCfg
import cn.com.auto.thkl.floating.menu.MenuItem
import cn.com.auto.thkl.floating.utils.BackGroudSeletor
import cn.com.auto.thkl.floating.utils.DensityUtil
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.net.Api
import cn.com.auto.thkl.script.Scripts
import cn.com.auto.thkl.utils.*
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ScreenUtils
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.stardust.autojs.event.BusinessEvent
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.autojs.execution.ScriptExecutionListener
import com.stardust.autojs.runtime.api.Device
import com.stardust.autojs.runtime.api.Images.saveBitmapToFile
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.toast_.view.*
import kotlinx.android.synthetic.main.view_top_msg.view.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val NOTIFICATION_ID = 1
private const val CHANEL_ID = "cn.com.auto.service.AccessibilityService.foreground"

@Suppress("UNCHECKED_CAST")
@DelicateCoroutinesApi
@SuppressLint("NewApi")
/**Task结尾的方法为任务集
 * Event结尾的为步数集合
 * 1.一个任务里由一个或多个事件集合组成，如：清理后台内存事件，卸载目标事件
 * 2.每个事件亦由一个或多个步伐组成，如:打开最近任务列表，点击清理按钮
 * 3.每个任务统一由AccessibilityService的真身用协程进行约束与管理：正在进行的任务除非强制任务以外都可以被其他由指令下发的任务打断，
 * 由协程的cancel，join或launcher进行
 * 4.每个事件统一由EventController进行约束与管理：每一个存在于EventController的事件都代表着将被执行或以监听的形式执行，
 * 同一时刻只存在一个事件进行，通过回调的方式进行通知
 * 5.每个步伐统一由EventAction和Event两个抽象进行约束与管理，每个步伐由计数器进行控制，被动响应或主动进行都需要更新步伐，
 * 并且确定下一次一定发生的事件，对应上一次步伐的变化而做出对应响应，使得步伐能按照开发者的意图滚动。
 * 6.超时事件的发生，因为每一个任务是比较长的不推荐在任务里继续，而每一个事件由多个步伐组成也有可能是比较长
 * 所以，事件的超时，任务的超时由定义的最小单位步伐决定，在步伐最小单位中定义一个除步伐控制计数器以外的无限自增计数器（观察计数器）
 * 观察现在步伐到下一个步伐是否可以使得观察计数器得到改变，当在规定时间，该步伐并未向下到下一个步伐或向上回滚到上一个步伐等，
 * 观察计数器没有发生变化，则代表该步伐出现超时现象进而得出任务超时。
 * 为什么不是事件超时？因为每个事件的前后并不被最小单位步伐知道，所以从策略上来讲，重置整个任务，使得任务重新执行是最佳方案
 * */
open class AccessibilityService : com.stardust.autojs.core.accessibility.AccessibilityService(),
    LifecycleOwner {
    private val mDisposable = mutableListOf<Disposable>()
    private var level: Int = 0
    private var keep: Boolean = true
    private var targetPackAgeName: String? = null /*目标app*/
    private var scriptId: String? = null /*目标脚本id*/
    private var runRecordId: String? = null /*执行任务id*/
    private var timer: Timer? = null
    private var bitmap: Bitmap? = null
    private var mApkPath = App.app.applicationContext.filesDir.absolutePath + "/apks/"
    private var mBitmapPath = App.app.applicationContext.filesDir.absolutePath + "/capture/img.jpeg"
    private val mLifecycleRegistry = LifecycleRegistry(this)
    private val equipmentPackList = ArrayList<String>()
    private var floatBallManager: FloatBallManager? = null
    private var runtimeDuration: Long = 0
    private var heartBeatJob: Job? = null
    private var currentJob: Job? = null
    private var currentType: TaskType? = null
    private var taskType: TaskType? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        App.service = this
         GlobalScope.launch {
            firstStartEvent(TaskProperty(TaskType.AUTO_START_TASK, "", "", "", false, null))
        }
    }

    private val listenerBattery: BatteryUtils.OnBatteryStatusChangedListener =
        BatteryUtils.OnBatteryStatusChangedListener {
            level = it.level
        }
    private val listener: ScriptExecutionListener = object : ScriptExecutionListener {
        override fun onStart(execution: ScriptExecution?) {/*接收开始信息*/
            GlobalScope.launch {
                showBottomToast("开始执行脚本")
            }
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    /*脚本执行超时*/
                    AccessibilityViewModel.report.postValue("$scriptId: 执行超时!")/*这里的主动停止要和用户按钮的主动停止做区分*/
                    /*停止脚本*/
                    AutoJs.getInstance().scriptEngineService.stopAll()
                    GlobalScope.launch {
                        suspendListenerStop()
                    }
                    /*查询下一个任务*/
                    AccessibilityViewModel.queryTask.value = 1
                }
            }, runtimeDuration * 1000)
        }


        override fun onSuccess(execution: ScriptExecution?, result: Any?) {/*接收成功信息*/
            GlobalScope.launch {
                showBottomToast("正在进行下一个脚本")
            }
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
            /**脚本执行完毕，继续查询脚本*/
            GlobalScope.launch {
                AccessibilityViewModel.queryTask.value = 1
            }
        }

        override fun onException(execution: ScriptExecution?, e: Throwable?) {/*接收错误信息*/
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
            if (e != null) {
                val msg = e.toString()
                if (msg.contains("com.stardust.autojs.runtime.exception.ScriptInterruptedException")) {/*主动停止 ---- 只要是调用到AutoJs StopALl方法的都会走这里*/
                    GlobalScope.launch {
                        Api.getApiService().scriptExecutionFeedback(
                            StringUtils.replaceAutoJsValue(
                                PreferenceManager.getDefaultSharedPreferences(this@AccessibilityService)
                                    .getString(Constant.TOKEN, "")
                            ),
                            "2",
                            runRecordId!!.toInt(),
                            "",
                            runRecordId!!.toInt(),
                            6,
                            "接口上报",
                            getBitmapString(
                                2, bitmap!!
                            )
                        ).subscribeOn(Schedulers.io()).subscribe({
                        }, { it.printStackTrace() }).apply { mDisposable.add(this) }
                        showBottomToast("脚本执行暂停")
                        delay(2000)
                        onScriptStopListener?.onStop("stop")
                    }
                }
            }
        }
    }
    private var onScriptStopListener: OnScriptStopListener? = null

    interface OnScriptStopListener {
        fun onStop(name: String)
    }

    private suspend fun suspendListenerStop() {
        suspendCoroutine<String> { continuation ->
            onScriptStopListener = object : OnScriptStopListener {
                override fun onStop(name: String) {
                    continuation.resume(name)
                }
            }
        }
    }


    private lateinit var imageReader: ImageReader
    private suspend fun queryTask(taskType: TaskType, job: Job) {/*开始查询任务*/
        if (!TextUtils.isEmpty(targetPackAgeName)) {
            suspendAutoStopEvent(TaskProperty(taskType, targetPackAgeName, "", "", false, job))
        }
        suspendAutoClearEvent(TaskProperty(taskType, "", "", "", false, job))
        kotlin.runCatching {
            showBottomToast("正在查询任务列表")
            Api.getApiService().queryAppTask(SP.getString(Constant.TOKEN), !BuildConfig.DEBUG)
        }.onFailure {
            showBottomToast("正在查询任务失败，正在重试")
            delay(5000)
            AccessibilityViewModel.queryTask.value = 1
        }.onSuccess {
            if (it.success) {
                showBottomToast("正在查询任务成功")
                val jsonArray = JSONArray.parseArray(JSONArray.toJSONString(it.obj))
                if (jsonArray.size == 0) {
                    showBottomToast("无任务存在，下一次询问将在10秒后")
                    delay(10000)
                    AccessibilityViewModel.queryTask.value = 1
                    return@onSuccess
                }
                val jsonObject = jsonArray.getJSONObject(0)
                val scriptId: Int = jsonObject.getInteger("scriptId")
                val packageApp: String = jsonObject.getString("uniqueCode")
                val runRecordId = jsonObject.getInteger("runRecordId")
                val versionCode = jsonObject.getString("appVersion")
                this@AccessibilityService.targetPackAgeName = packageApp
                this@AccessibilityService.runRecordId = runRecordId.toString()
                this@AccessibilityService.scriptId = scriptId.toString()
                val boolean = hasPackageName(packageApp, versionCode.trim().toInt())
                showBottomToast("查询目标app:${boolean}存在")
                if (!boolean) {
                    AccessibilityViewModel.executeTask.value = true
                } else {
                    AccessibilityViewModel.queryTask.value = 1
                }
            }
        }
    }


    private fun writeResponseToDisk(path: String, response: Response<ResponseBody>) {
        if (response.body() != null) {
            writeFileFromIs(File(path), response.body()!!.byteStream())
        }
    }

    private fun writeFileFromIs(file: File, `is`: InputStream) {
        if (!file.exists()) {
            if (!file.parentFile?.exists()!!) {
                file.parentFile?.mkdir()
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        var os: OutputStream? = null
        try {
            os = FileOutputStream(file)
            val data = ByteArray(1024)
            var len: Int
            while (`is`.read(data).also { len = it } != -1) {
                os.write(data, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun equipmentMaintenanceTask(
        taskType: TaskType, type: String, job: Job
    ) {
        kotlin.runCatching {
            showBottomToast("正在维护")
            Api.getApiService().queryMaintainInfo(SP.getString(Constant.TOKEN))
        }.onFailure {
            showBottomToast("维护失败")
        }.onSuccess {
            if (it.success) {
                val jsonObject = JSONObject.parseObject(JSONObject.toJSONString(it.obj))
                val jsonArray = jsonObject.getJSONArray("appList")
                if (jsonArray.size > 0) {
                    val gonnaInstall = mutableListOf<JSONObject>()
                    val installMatcher = mutableListOf<JSONObject>()
                    val gonnaUnInstall = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.size) {
                        val targetObject = jsonArray.getJSONObject(i);
                        val enable = targetObject.getBoolean("enable")
                        if (enable) {/*需要安装*/
                            gonnaInstall.add(targetObject)
                            installMatcher.add(targetObject)
                        } else {/*需要卸载*/
                            gonnaUnInstall.add(targetObject)
                        }
                    }
                    if (type == Constant.SCRIPT_APP) {
                        if (gonnaInstall.isNotEmpty()) {
                            for (item in gonnaInstall) {
                                L.e(
                                    item.toString()
                                )
                            }
                            equipmentInstallTask(taskType, gonnaInstall, job)
                        }
                    } else {
                        if (gonnaInstall.isNotEmpty()) {
                            equipmentInstallTask(taskType, gonnaInstall, job)
                        }/*不在安装列表的应用进行卸载*/
                        val uninstallApps = mutableListOf<JSONObject>()
                        uninstallApps.addAll(gonnaUnInstall)
                        val apps: List<PackageInfo> = packageManager.getInstalledPackages(0)
                        for (localApp in apps) {/*查找所有已经安装的app*/
                            if ((localApp.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                                var target = ""
                                for (remote in installMatcher) {
                                    val remotePkName = remote.getString("uniqueCode")
                                    if (localApp.packageName.equals(remotePkName) || localApp.packageName.equals(
                                            packageName
                                        )
                                    ) {
                                        target = "111"
                                    }
                                }
                                if (target.isEmpty()) {
                                    val targetObject = JSONObject()
                                    targetObject["uniqueCode"] = localApp.packageName
                                    targetObject["appVersion"] = "0"
                                    uninstallApps.add(targetObject)
                                }
                            }


                        }/*用户不关心的应用进行卸载*/
                        if (uninstallApps.isNotEmpty()) {
                            equipmentUnInstallTask(taskType, uninstallApps, job)
                        }
                    }
                }
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    suspend fun showWindow() {
        return withContext(Dispatchers.Main) {
            val ballSize: Int = DensityUtil.dip2px(this@AccessibilityService, 45f) /*定义悬浮框大小*/
            val ballIcon: Drawable =
                BackGroudSeletor.getdrawble("ic_floatball", this@AccessibilityService) /*定义初始图片*/
            val ballCfg =
                FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER) /*初始化*/
            val menuSize: Int = DensityUtil.dip2px(this@AccessibilityService, 180f)
            val menuItemSize: Int = DensityUtil.dip2px(this@AccessibilityService, 40f)
            val menuCfg = FloatMenuCfg(menuSize, menuItemSize)
            floatBallManager = FloatBallManager(this@AccessibilityService, ballCfg, menuCfg)
            addFloatMenuItem()
            floatBallManager?.show()
        }

    }

    private suspend fun showBottomToast(msg: String) {
        withContext(Dispatchers.Main) {
            if (floatBallManager != null) {
                if (EasyFloat.isShow("1")) {
                    EasyFloat.dismiss("1")
                }
                EasyFloat.with(this@AccessibilityService).setLayout(R.layout.toast_) {
                    it.tv_toast.text = msg
                    it.postDelayed({ EasyFloat.dismiss("1") }, 4000)
                }.setTag("1").setMatchParent(true).setGravity(Gravity.BOTTOM, 0, -120)
                    .setShowPattern(ShowPattern.ALL_TIME).setDragEnable(false).setAnimator(null)
                    .show()
            }

        }
    }

    private suspend fun showTopToast(msg: String) {
        withContext(Dispatchers.Main) {
            if (EasyFloat.isShow("2")) {
                EasyFloat.dismiss("2")
            }
            if (floatBallManager != null) {
                EasyFloat.with(this@AccessibilityService).setLayout(R.layout.toast_) {
                    it.tv_toast.text = msg
                    it.tv_toast.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#ff9f9f"))
                }.setTag("2").setMatchParent(true).setGravity(Gravity.TOP, 0, 80)
                    .setShowPattern(ShowPattern.ALL_TIME).setDragEnable(false).setAnimator(null)
                    .show()
            }

        }
    }

    private suspend fun stopTask(scope: CoroutineScope, taskType: TaskType) {
        if (floatBallManager!!.state) {
            currentJob?.cancel()/*协程的取消并不可靠*/
            currentType = taskType
            val i = AutoJs.getInstance().scriptEngineService.stopAll()
            if (i != 0) {
                suspendListenerStop()
            }
            suspendAutoStopEvent(
                TaskProperty(
                    taskType,
                    targetPackAgeName,
                    "",
                    "",
                    false,
                    currentJob
                )
            )
            suspendAutoClearEvent(TaskProperty(taskType, "", "", "", false, currentJob))
            floatBallManager!!.changeState(false)
        }
    }

    /*重新开始任务，脚本任务会先走下载*/
    private fun resumeTask() {
        if (!floatBallManager!!.state) {
            when (currentType) {
                TaskType.AUTO_EXECUTE_TASK -> {
                    AccessibilityViewModel.executeTask.value = true
                }
                TaskType.AUTO_SHUT_DOWN_TASK -> {
                    AccessibilityViewModel.shutDownTask.value = true
                }
                TaskType.AUTO_RESTART_TASK -> {
                    AccessibilityViewModel.restartTask.value = true
                }
                TaskType.AUTO_EXIT_TASK -> {
                    AccessibilityViewModel.exitTask.value = true
                }
                TaskType.AUTO_SETTING_TASK -> {
                    AccessibilityViewModel.settingTask.value = true
                }
                TaskType.AUTO_QUERY_TASK -> {
                    AccessibilityViewModel.queryTask.value = 1
                }
                else -> {

                }
            }
            floatBallManager!!.changeState(true)
        }


    }

    private fun addFloatMenuItem() {
        val stopItem: MenuItem = object : MenuItem(BackGroudSeletor.getdrawble("ic_weixin", this)) {
            override fun action() {
                if (floatBallManager!!.state) {
                    val event = EventController.INSTANCE.getCurrentEvent()
                    if (event != null) {
                        GlobalScope.launch {
                            AccessibilityViewModel.stopTask.value = true
                        }
                    }
                } else {
                    GlobalScope.launch {
                        resumeTask()
                    }
                }
                floatBallManager!!.closeMenu()
            }
        }
        floatBallManager!!.addMenuItem(stopItem).buildMenu()
    }

    private fun initObserver() {
        AccessibilityViewModel.shutDownTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_SHUT_DOWN_TASK
                    suspendAutoShutDownEvent(
                        TaskProperty(
                            taskType,
                            "",
                            "",
                            "",
                            false,
                            currentJob
                        )
                    )
                }
            }
        })
        AccessibilityViewModel.restartTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_RESTART_TASK
                    suspendAutoRestartEvent(TaskProperty(taskType, "", "", "", false, currentJob))
                }
            }
        })
        AccessibilityViewModel.queryTask.observe(this, Observer<Int> {
            if (it != 0) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_QUERY_TASK
                    queryTask(taskType!!, currentJob!!)
                }
            }

        })
        AccessibilityViewModel.exitTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_EXIT_TASK
                    autoExitTask(taskType!!, currentJob!!)
                }
            }
        })
        AccessibilityViewModel.executeTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_EXECUTE_TASK
                    downLoadScriptTask()
                }
            }
        })
        AccessibilityViewModel.settingTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_SETTING_TASK
                    settingTask(
                        TaskProperty(
                            taskType, packageName, "", "", false, currentJob
                        )
                    )
                }
            }
        })
        AccessibilityViewModel.overLayerTask.observe(this, Observer {
            if (it){
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_OVER_LAYER_TASK
                    overLayerTask(TaskProperty(taskType,"","","",false,currentJob))
                }
            }
        })
        AccessibilityViewModel.equipmentMaintenanceTask.observe(this, Observer {
            if (it) {
                currentJob = GlobalScope.launch {
                    taskType = TaskType.AUTO_MAINTENANCE_TASK
                    equipmentMaintenanceTask(taskType!!, "", currentJob!!)
                }
            }
        })
        AccessibilityViewModel.stopTask.observe(this, Observer {
            if (it) {
                GlobalScope.launch {
                    delay(2000)
                    stopTask(this, TaskType.AUTO_STOP_TASK)
                    onScriptStopListener?.onStop("targetStop")
                }
            }

        })
        AccessibilityViewModel.retry.observe(this, Observer {
            if (it != null) {
                GlobalScope.launch {
                    if (it.job == null) {
                        return@launch
                    }
                    it.job.cancel()
                    showBottomToast("超时重试中!")
                    when (it.taskType) {
                        TaskType.AUTO_SETTING_TASK -> {
                            AccessibilityViewModel.settingTask.value = true
                        }
                        TaskType.AUTO_QUERY_TASK -> {
                            AccessibilityViewModel.queryTask.value = 1
                        }

                        TaskType.AUTO_RESTART_TASK -> {
                            AccessibilityViewModel.shutDownTask.value = true
                        }

                        TaskType.AUTO_SHUT_DOWN_TASK -> {
                            AccessibilityViewModel.shutDownTask.value = true
                        }

                        TaskType.AUTO_MAINTENANCE_TASK -> {
                            AccessibilityViewModel.equipmentMaintenanceTask.value = true
                        }

                        TaskType.AUTO_STOP_SCRIPT_TASK -> {

                        }

                        TaskType.AUTO_OVER_LAYER_TASK -> {
                            AccessibilityViewModel.overLayerTask.value = true
                        }
                        TaskType.AUTO_STOP_TASK -> {
                            AccessibilityViewModel.stopTask.value = true
                        }

                        else -> {
                            L.e("没有拦截的错误信息")
                        }
                    }
                }
            }
        })

        AccessibilityViewModel.hearBeatTask.observe(this, Observer {
            if (it) {
                heartBeatJob = GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        while (true) {
                            hearBeatTask(this)
                            delay(15000)
                        }
                    }
                }
            }
        })
        AccessibilityViewModel.logout.observe(this, Observer {
            if (it) {
                GlobalScope.launch {
                    AccessibilityViewModel.stopTask.value = true
                    AccessibilityViewModel.normalStartService.value = false
                    AccessibilityViewModel.settingTask.value = false
                    suspendListenerStop()
                    heartBeatJob?.cancel()
                    if (EasyFloat.isShow("1")) {
                        EasyFloat.dismiss("1")
                    }
                    if (EasyFloat.isShow("2")) {
                        EasyFloat.dismiss("2")
                    }
                    startActivity(Intent(
                        this@AccessibilityService, LoginActivity::class.java
                    ).also {
                        it.putExtra(LoginActivity.MODEL, "login_out")
                    })
                }
            }
        })
        AccessibilityViewModel.window.observe(this, Observer {
            if (it) {
                GlobalScope.launch {
                    showWindow()
                }
            }
        })
        AccessibilityViewModel.report.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) {
                appExecutionFeedBack(it)
            }
        })
        AccessibilityViewModel.capture.observe(this, Observer {
            if (it != null) {
                captureTask(mediaProjection = it)
            }
        })
    }

    /*维护任务，卸载*/
    private suspend fun equipmentUnInstallTask(
        scope: TaskType, it: MutableList<JSONObject>, job: Job
    ) {
        val jsonObject = it[0].apply { it.removeAt(0) }
        val appVersion = jsonObject.getString("appVersion")
        val uniqueCode = jsonObject.getString("uniqueCode")
        val boolean = hasPackageName(uniqueCode, appVersion.toInt())
        if (!boolean) {
            SuspendEventManager.suspendAutoUninstallPackEvent(
                TaskProperty(
                    scope, uniqueCode, "", "", false, job
                )
            )
            suspendAutoClearEvent(TaskProperty(scope, uniqueCode, "", "", false, job))

        }
        if (it.isNotEmpty()) {
            equipmentUnInstallTask(scope, it, job)
        }
    }

    private suspend fun autoExitTask(
        taskType: TaskType, job: Job
    ) {
        suspendAutoStopEvent(TaskProperty(taskType, packageName, "", "", false, job))
        suspendAutoClearEvent(TaskProperty(taskType, "", "", "", false, job))

    }

    private suspend fun equipmentInstallTask(
        scope: TaskType, lists: MutableList<JSONObject>, job: Job
    ) {
        val jsonObject = lists[0].apply { lists.removeAt(0) }
        val appDownloadLink = jsonObject.getString("appDownloadLink")
        val appName = jsonObject.getString("appName")
        val appVersion = jsonObject.getString("appVersion")
        val uniqueCode = jsonObject.getString("uniqueCode")
        val path = mApkPath + "${appName}.apk"/*转存任务*/
        val boolean = hasPackageName(uniqueCode, appVersion.toInt())
        if (boolean) {
            kotlin.runCatching {
                showBottomToast("${appName}正在下载..")
                Api.getApiService().download(appDownloadLink)
            }.onFailure { it.printStackTrace() }.onSuccess {
                writeResponseToDisk(path, it)
                showBottomToast("${appName}正在安装..")
                val msgType = suspendAutoInstallPackEvent(
                    TaskProperty(
                        scope, "", path, "", false, job
                    )
                )
                if (MsgType.SUCCESS.name == msgType) {
                    showBottomToast("权限申请")
                    suspendAutoPermissionAppsEvent(
                        TaskProperty(
                            scope, uniqueCode, "", "", false, job
                        )
                    )
                    showBottomToast("清理后台中")
                    suspendAutoClearEvent(TaskProperty(scope, "", "", "", false, job))
                }

                if (lists.isNotEmpty()) {
                    equipmentInstallTask(scope, lists, job)
                    L.e("剩余App${lists.size}")
                    equipmentPackList.add(uniqueCode)
                }
            }
        } else {
            if (lists.isNotEmpty()) {
                equipmentInstallTask(scope, lists, job)
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun captureTask(mediaProjection: MediaProjection) {
        mediaProjection.createVirtualDisplay(
            "screen_shot",
            Device.width,
            Device.height,
            ScreenUtils.getScreenDensityDpi(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image: Image = reader.acquireNextImage()
            val width: Int = image.width
            val height: Int = image.height
            val planes: Array<Image.Plane> = image.planes
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride: Int = planes[0].pixelStride
            val rowStride: Int = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            this.bitmap = bitmap
            image.close()
        }, null)
    }

    private suspend fun overLayerTask(scope: TaskProperty){
        suspendAutoOverLayerEvent(scope.job, this@AccessibilityService)
    }
    private suspend fun settingTask(scope: TaskProperty) {
        /**特殊任务执行中，不允许被中断，收到任何中断指令需要上报，并提示*/
        if (!SP.getBoolean(Constant.AUTO_START)) {
            showTopToast("正在进行每日维护，请勿干扰！")
            showBottomToast("自启动授权中")
            suspendAutoStartTaskEvent(scope)
        }
        if (!SP.getBoolean(Constant.PERMISSION)) {
            showTopToast("正在进行每日维护，请勿干扰！")
            showBottomToast("权限授权中")
            suspendAutoPermissionEvent(scope)
        }
        showBottomToast("清理后台中")
        suspendAutoClearEvent(scope)
        if (!SP.getBoolean(Constant.FIRST_LOGIN)) {
            showTopToast("正在进行每日维护，请勿干扰！")
            showBottomToast("维护执行中")
            equipmentMaintenanceTask(scope.taskType, Constant.SCRIPT_APP, scope.job)
            SP.putBoolean(Constant.FIRST_LOGIN, true)
        }/*这个任务只在每一次登录的时候Service第一次启动时开启*/
        showTopToast("正在进行执行前检测")
        showBottomToast("查询微信状态")
        suspendAutoCheckWXEvent(scope)
        showBottomToast("查询支付宝状态")
        suspendAutoCheckAliPayEvent(scope)
        showBottomToast("查询任务中")
        AccessibilityViewModel.queryTask.postValue(1)
    }

    @SuppressLint("CheckResult")
    private suspend fun hearBeatTask(job: CoroutineScope) {/*同步*/
        kotlin.runCatching {
            Api.getApiService().deviceReport(
                SP.getString(Constant.TOKEN),
                if (SP.getString(Constant.DEVICE_ID) == "") 0 else SP.getString(Constant.DEVICE_ID)
                    .toInt(),
                level,
                keep,
                getSignal(),
                true
            )
        }.onFailure { it.printStackTrace() }.onSuccess {
            val jsonArray = JSONArray.parseArray(JSONObject.toJSONString(it.obj))
            val item: List<String> = jsonArray.toList() as List<String>
            if (item.isEmpty()) {
                return@onSuccess
            } else {
                for (i in item) {
                    when (i) {
                        "DEVICE_RESTARTS" -> {
                            showBottomToast("停止脚本，正在进行重启指令")
                            AccessibilityViewModel.stopTask.value = true
                            suspendListenerStop()
                            AccessibilityViewModel.restartTask.value = true
                        }

                        "DEVICE_SHUTS_DOWN" -> {
                            showBottomToast("停止脚本，正在进行关机指令")
                            AccessibilityViewModel.stopTask.value = true
                            suspendListenerStop()
                            AccessibilityViewModel.shutDownTask.value = true
                        }

                        "RUN_PAUSED" -> {
                            showBottomToast("停止脚本，正在进行停止指令")
                            AccessibilityViewModel.stopTask.value = true
                        }

                        "RUN_CONTINUES" -> {
                            showBottomToast("开始脚本，正在进行开始指令")/*执行完插队任务后，继续恢复上一个任务*/
                            resumeTask()
                        }
                        "RUN_EXITS" -> {
                            showBottomToast("停止脚本，正在进行退出指令")
                            AccessibilityViewModel.stopTask.value = true
                            suspendListenerStop()
                            AccessibilityViewModel.exitTask.value = true
                        }
                        "DEVICE_MAINTENANCE" -> {
                            showTopToast("正在进行每日维护，请勿干扰！")
                            showBottomToast("停止脚本，正在进行维护指令")
                            AccessibilityViewModel.stopTask.value = true
                            suspendListenerStop()
                            AccessibilityViewModel.equipmentMaintenanceTask.value = true
                            resumeTask()
                        }
                    }
                }

            }
        }


    }


    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addDataScheme("package")
        BatteryUtils.registerBatteryStatusChangedListener(listenerBattery)
        startForeground(NOTIFICATION_ID, buildNotification())
        initObserver()
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND)
        imageReader = ImageReader.newInstance(
            Device.width, Device.height, PixelFormat.RGBA_8888, 15
        )
    }

    private fun buildNotification(): Notification {
        createNotificationChannel()
        val flags = PendingIntent.FLAG_IMMUTABLE
        PendingIntent.getActivity(
            this, 0, Intent(
                this, MainActivity::class.java
            ), flags
        )
        return NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("${getString(R.string.app_name)}保持运行中")
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(R.drawable.autojs_logo).setWhen(System.currentTimeMillis())
            .setContentIntent(null).setChannelId(CHANEL_ID).setVibrate(LongArray(0)).build()
    }

    private fun hasPackageName(appPackageName: String, versionCode: Int): Boolean {
        // get all apps
        val apps: List<PackageInfo> = packageManager.getInstalledPackages(0)
        for (item in apps) {
            val name = item.packageName
            val version = item.longVersionCode
            L.e("current $name --------$version--------- target $appPackageName$versionCode")
            if (name.equals(appPackageName) && version.toInt() >= versionCode) {/*不需要安装*/
                return false
            }
        }
        return true

    }

    private fun getSignal(): Int {
        return if (NetworkUtils.isWifiConnected()) {
            0
        } else if (NetworkUtils.is4G()) {
            1
        } else {
            2
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val name: CharSequence = "${getString(R.string.app_name)}服务"
        val description = "${getString(R.string.app_name)}无障碍守护正在运行"
        val channel = NotificationChannel(
            CHANEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = description
        channel.enableLights(false)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        for (mDisposable in mDisposable) {
            if (!mDisposable.isDisposed) {
                mDisposable.dispose()
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        BatteryUtils.unregisterBatteryStatusChangedListener(listenerBattery)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun reportEvent(event: BusinessEvent) {
        L.e(event.jsonValue)
        val jsonObject = JSONObject.parseObject(event.jsonValue)
        val type = jsonObject.getInteger("contentType")
        val status = jsonObject.getString("status")
        val result = jsonObject.getString("result")
        Api.getApiService().scriptExecutionFeedback(
            StringUtils.replaceAutoJsValue(
                PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.TOKEN, "")
            ),
            type.toString(),
            runRecordId!!.toInt(),
            result,
            runRecordId!!.toInt(),
            status.toInt(),
            "接口上报",
            getBitmapString(
                type, bitmap!!
            )
        ).subscribeOn(Schedulers.io()).subscribe({
        }, { it.printStackTrace() }).apply { mDisposable.add(this) }
    }

    private fun getBitmapString(type: Int, bitmap: Bitmap): String {
        return if (type == 1) {
            ""
        } else {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        }
    }

    private fun appExecutionFeedBack(content: String) {
        val multipartBody = MultipartBody.Builder()
        if (bitmap != null) {
            val file = saveBitmapToFile(bitmap!!, mBitmapPath)
            val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            multipartBody.addPart(
                MultipartBody.Part.Companion.createFormData(
                    "files", file.name, requestBody
                )
            )
        }
        multipartBody.addFormDataPart("access_token", SP.getString(Constant.TOKEN))
        multipartBody.addFormDataPart("content", content)
        multipartBody.addFormDataPart("title", getString(R.string.app_name))
        Api.getApiService().appExecutionFeedback(
            StringUtils.replaceAutoJsValue(
                PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.TOKEN, "")
            ), multipartBody.build()
        ).subscribeOn(Schedulers.io()).subscribe({
            L.e(it.toString())
        }, { it.printStackTrace() }).apply { mDisposable.add(this) }
    }


    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downLoadScriptTask() {
        kotlin.runCatching {
            showBottomToast("正在下载脚本")
            Api.getApiService().scriptDetail(SP.getString(Constant.TOKEN), scriptId!!)
        }.onFailure {
            /*继续查询任务*/
            AccessibilityViewModel.queryTask.value = 1
        }.onSuccess {
            if (it.success) {
                SP.putString(Constant.SCRIPT_ID, scriptId)
                val scriptObject = JSONObject.parseObject(JSONObject.toJSONString(it.obj))
                val aesString = scriptObject.getString("aesKey")
                val content = scriptObject.getString("content")
                runtimeDuration = scriptObject.getLong("runtimeDuration")
                val javaScript = String(
                    Base64.decode(
                        CipherUtils.decodeBase64(content, aesString), Base64.DEFAULT
                    ), Charsets.UTF_8
                )
                Scripts.run(javaScript, listener)
            } else {
                /*继续查询任务*/
                AccessibilityViewModel.queryTask.value = 1
            }

        }

    }


}
