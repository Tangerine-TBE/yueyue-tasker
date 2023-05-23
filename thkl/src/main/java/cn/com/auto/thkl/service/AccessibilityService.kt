package cn.com.auto.thkl.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
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
import android.os.Handler
import android.os.Looper
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
import cn.com.auto.thkl.custom.event.base.EventAction
import cn.com.auto.thkl.custom.event.base.EventController
import cn.com.auto.thkl.custom.event.base.MsgType
import cn.com.auto.thkl.custom.event.base.SuspendEventManager
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.firstStartEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoCheckAliPayEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoCheckWXEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoClearCacheEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoClearEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoInstallPackEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoLieBaoEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoOverLayerEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoPermissionAppsEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoPermissionEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoRestartEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoShutDownEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoStartTaskEvent
import cn.com.auto.thkl.custom.event.base.SuspendEventManager.suspendAutoStopEvent
import cn.com.auto.thkl.custom.event.huaweiAndroid10.AutoRefreshLayerEvent
import cn.com.auto.thkl.custom.task.TaskProperty
import cn.com.auto.thkl.custom.task.TaskType
import cn.com.auto.thkl.db.DaoTool
import cn.com.auto.thkl.db.DaoTool.findLastLoginInfo
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
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ScreenUtils
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.stardust.autojs.event.BusinessEvent
import com.stardust.autojs.event.ReportProfitEvent
import com.stardust.autojs.event.StopSelfEvent
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.autojs.execution.ScriptExecutionListener
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
import java.text.SimpleDateFormat
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
 * */ class AccessibilityService : com.stardust.autojs.core.accessibility.AccessibilityService(),
    LifecycleOwner {
    private val mDisposable = mutableListOf<Disposable>()
    private var level: Int = 0
    private var keep: Boolean = true
    private var targetPackAgeName: String? = null /*执行任务app的包名*/
    private var scriptId: String? = null /*目标脚本id*/
    private var runRecordId: String? = null /*执行任务id*/
    private var appName: String? = null /*执行脚本的app名称*/
    private var versionCode: Int? = null /*执行脚本的app版本好*/
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
    private var targetExit = false
    private var onFinishListener: OnFinishListener? = null
    private var cmdWork: String = ""
    private var backToMain = true
    private lateinit var imageReader: ImageReader
    private var stateExtra = false
    private var cmdDo = false

    @SuppressLint("WrongConstant")
    override fun onServiceConnected() {
        super.onServiceConnected()
        imageReader = ImageReader.newInstance(
            ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), PixelFormat.RGBA_8888, 1
        )
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        App.service = this
        App.app.launch {
            firstStartEvent(
                TaskProperty(
                    TaskType.AUTO_START_TASK, "", "", "", false, null, AppUtils.getAppName()
                )
            )
        }
    }

    private val listenerBattery: BatteryUtils.OnBatteryStatusChangedListener =
        BatteryUtils.OnBatteryStatusChangedListener {
            level = it.level
        }
    private val listener: ScriptExecutionListener = object : ScriptExecutionListener {
        override fun onStart(execution: ScriptExecution?) {/*接收开始信息*/
            if (AccessibilityViewModel.normalStartService.value == true) {
                App.app.launch {
                    showBottomToast("开始执行脚本")
                }
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {/*脚本执行超时*/
                        App.app.launch {
                            AccessibilityViewModel.showTopToast.postValue("当前脚本执行时间到")
                            delay(2500)
                            scriptReport("脚本执行成功", 2, appName!!)/*查询下一个任务*/
                            AccessibilityViewModel.report.postValue("$appName: 执行完毕,本次执行时长:${runtimeDuration}分钟。")/*这里的主动停止要和用户按钮的主动停止做区分*//*停止脚本*/
                            AutoJs.getInstance().scriptEngineService.stopAll()
                            suspendListenerStop()
                            onScriptStopListener = null
                            AccessibilityViewModel.queryTask.postValue(1)
                        }
                    }
                }, runtimeDuration * 1000)
            }
        }


        override fun onSuccess(execution: ScriptExecution?, result: Any?) {/*接收成功信息*/
            if (AccessibilityViewModel.normalStartService.value == true) {

                App.app.launch {
                    showBottomToast("正在进行下一个脚本")
                }
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                /**脚本执行完毕，继续查询脚本*/
                App.app.launch {
                    AccessibilityViewModel.queryTask.postValue(1)
                }
            }
        }

        override fun onException(execution: ScriptExecution?, e: Throwable?) {/*接收错误信息*/
            if (AccessibilityViewModel.normalStartService.value == true) {

                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                if (e != null) {
                    val msg = e.toString()
                    if (msg.contains("com.stardust.autojs.runtime.exception.ScriptInterruptedException")) {/*主动停止 ---- 只要是调用到AutoJs StopALl方法的都会走这里*/
                        App.app.launch {
                            showTopToast("正在暂停任务，请稍后!")
                            delay(2500)
                            scriptReport("脚本执行暂停", 6, appName!!)
                            delay(2500)
                            if (onScriptStopListener != null) {
                                onScriptStopListener?.onStop("stop")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scriptReport(result: String, status: Int, title: String) {
        L.e("title:${title}")
        App.app.launch {
            withContext(Dispatchers.IO) {
                val jsonObject = JSONObject()
                jsonObject["fileName"] = System.currentTimeMillis().toString()
                jsonObject["result"] = result
                jsonObject["resultBase64"] = getBitmapString()
                jsonObject["runRecordId"] = runRecordId!!
                jsonObject["status"] = status
                jsonObject["title"] = title
                val requestBody = RequestBody.create(
                    "application/json;charset=UTF-8".toMediaTypeOrNull(), jsonObject.toJSONString()
                )
                kotlin.runCatching {
                    Api.getApiService().scriptExecutionFeedback(
                        SP.getString(Constant.TOKEN), requestBody
                    )
                }.onSuccess {
                    if (it.success) {
                        showBottomToast("脚本上报成功")
                    }
                }.onFailure { it.printStackTrace() }
            }

        }
    }

    private var onScriptStopListener: OnScriptStopListener? = null
    private var onPauseListener: OnPauseListener? = null
    private var onTaskFinishedListener: OnTaskFinishedListener? = null

    /**释放挂起方法*/
    interface OnPauseListener {
        fun onScriptPause(name: String)
    }

    /**释放挂起方法*/
    interface OnFinishListener {
        fun onFinish(name: String)
    }

    /**释放挂起方法*/
    interface OnScriptStopListener {
        fun onStop(name: String)
    }

    /**释放挂起方法*/
    interface OnTaskFinishedListener {
        fun taskFinish()
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

    private suspend fun suspendListenerFinish() {
        suspendCoroutine<String> { continuation ->
            onFinishListener = object : OnFinishListener {
                override fun onFinish(name: String) {
                    continuation.resume(name)
                }
            }
        }
    }

    private suspend fun suspendListenerPause() {
        suspendCoroutine<String> { continuation ->
            onPauseListener = object : OnPauseListener {
                override fun onScriptPause(name: String) {
                    continuation.resume(name)
                }
            }
        }
    }

    private suspend fun suspendOnTaskFinished() {
        suspendCoroutine<String> { continuation ->
            onTaskFinishedListener = object : OnTaskFinishedListener {
                override fun taskFinish() {
                    continuation.resume("name")
                }
            }
        }
    }


    private suspend fun queryTask(taskType: TaskType, job: Job) {/*开始查询任务*/
        if (!TextUtils.isEmpty(targetPackAgeName) && targetExit) {
            suspendAutoStopEvent(
                TaskProperty(
                    taskType, targetPackAgeName, "", "", false, job, AppUtils.getAppName()
                )
            )
        }
        if (targetExit) {
            suspendAutoClearEvent(
                TaskProperty(
                    taskType, "", "", "", false, job, AppUtils.getAppName()
                )
            )
        }
        kotlin.runCatching {
            showBottomToast("查询任务列表,请稍后")
            Api.getApiService().queryAppTask(SP.getString(Constant.TOKEN), !BuildConfig.DEBUG)
        }.onFailure {
            showBottomToast("查询失败，正在重新查询")
            targetExit = false
            delay(5000)
            queryTask(taskType, job)
        }.onSuccess {
            if (it.success) {
                val jsonArray = JSONArray.parseArray(JSONArray.toJSONString(it.obj))
                if (jsonArray.size == 0) {
                    showBottomToast("无任务存在，下次查询将在10秒后")
                    targetExit = false
                    delay(10000)
                    queryTask(taskType, job)
                    return@onSuccess
                }
                showBottomToast("任务查询成功")
                val jsonObject = jsonArray.getJSONObject(0)
                val scriptId: Int = jsonObject.getInteger("scriptId")
                val packageApp: String = jsonObject.getString("uniqueCode")
                val runRecordId = jsonObject.getInteger("runRecordId")
                val versionCode = jsonObject.getString("appVersion")
                val appName = jsonObject.getString("appName")
                this@AccessibilityService.appName = appName
                this@AccessibilityService.targetPackAgeName = packageApp
                this@AccessibilityService.runRecordId = runRecordId.toString()
                this@AccessibilityService.scriptId = scriptId.toString()
                this@AccessibilityService.versionCode = versionCode.trim().toInt()
                SP.putStringN(Constant.SCRIPT_APP, JSONObject.toJSONString(it))
                val boolean = hasPackageName(packageApp, versionCode.trim().toInt())
                if (!boolean) {
                    targetExit = true
                    AccessibilityViewModel.executeTask.postValue(true)
                } else {
                    targetExit = false
                    scriptReport("${appName}不存在!", 3, appName)
                    showBottomToast("${appName}不存在!,下次查询将在10秒后。")
                    delay(10000)
                    queryTask(taskType, job)
                }
            } else {
                delay(10000)
                targetExit = false
                showBottomToast("请求失败，下次查询将在10秒后")
                queryTask(taskType, job)
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
//            showBottomToast("正在维护")
            floatBallManager?.isCanOpen = false
            Api.getApiService().queryMaintainInfo(SP.getString(Constant.TOKEN))
        }.onFailure {
            floatBallManager?.isCanOpen = true
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
                                if (localApp.packageName.equals(remotePkName)) {
                                    L.e("单个App${remotePkName}")
                                    target = "111"
                                }
                            }
                            if (localApp.packageName != packageName) {
                                if (target.isEmpty()) {
                                    val targetObject = JSONObject()
                                    targetObject["uniqueCode"] = localApp.packageName
                                    targetObject["appVersion"] = "0"
                                    uninstallApps.add(targetObject)
                                }
                            }
                        }
                    }/*用户不关心的应用进行卸载*/
                    if (uninstallApps.isNotEmpty()) {
                        equipmentUnInstallTask(taskType, uninstallApps, job)
                    }
                    suspendAutoClearEvent(
                        TaskProperty(
                            taskType, "", "", "", false, job, packageName
                        )
                    )
                    suspendAutoLieBaoEvent(
                        TaskProperty(
                            taskType, "", "", "", false, job, packageName
                        )
                    )
                    suspendAutoClearEvent(
                        TaskProperty(
                            taskType, "", "", "", false, job, packageName
                        )
                    )
                }
                DaoTool.addStatusWithLogin(SP.getString(Constant.LOGIN_NAME))
            }

        }

    }

    @SuppressLint("ClickableViewAccessibility")
    suspend fun showWindow() {
        return withContext(Dispatchers.Main) {
            val ballSize: Int = DensityUtil.dip2px(this@AccessibilityService, 45f) /*定义悬浮框大小*/
            val ballIcon: Drawable =
                BackGroudSeletor.getdrawble("icon_float", this@AccessibilityService) /*定义初始图片*/
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
                    it.postDelayed({ EasyFloat.dismiss("1") }, 60*1000)
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

    private suspend fun stopTask() {
        if (floatBallManager!!.state) {
            floatBallManager!!.isCanOpen = false
            currentJob?.cancel()/*协程的取消并不可靠*/
            currentType = taskType!!
            val i = AutoJs.getInstance().scriptEngineService.stopAll()
            if (i != 0) {
                suspendListenerStop()
                onScriptStopListener = null
            }
            floatBallManager!!.isCanOpen = true
        }
    }

    private suspend fun stopTask(scope: Job?, type: TaskType) {
        if (AccessibilityViewModel.normalStartService.value == true) {
            if (floatBallManager!!.state) {
                floatBallManager!!.isCanOpen = false
                floatBallManager!!.changeState(false)
                currentJob?.cancel()/*协程的取消并不可靠*/
                currentType = taskType!!
                val i = AutoJs.getInstance().scriptEngineService.stopAll()
                L.e("需要停止的脚本数量:--${i}")
                if (i != 0) {
                    showBottomToast("正在停止脚本")
                    suspendListenerStop()
                    onScriptStopListener = null
                }
                performGlobalAction(GLOBAL_ACTION_HOME)
                if (!TextUtils.isEmpty(targetPackAgeName) && targetExit) {
                    showBottomToast("正在停止${appName}")
                    delay(2000)
                    suspendAutoStopEvent(
                        TaskProperty(
                            type, targetPackAgeName, "", "", false, scope, appName
                        )
                    )
                }
                delay(2000)
                showBottomToast("正在清理后台")
                suspendAutoClearEvent(
                    TaskProperty(
                        type, "", "", "", false, scope, AppUtils.getAppName()
                    )
                )
                delay(2000)
                showTopToast("任务暂停")
                if (backToMain) {
                    val intent = Intent(this@AccessibilityService, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    backToMain = true
                }
                floatBallManager!!.isCanOpen = true
                if (onPauseListener != null) {
                    onPauseListener?.onScriptPause("onStop")
                }
            } else {
                delay(2500)
                if (onPauseListener != null) {
                    onPauseListener?.onScriptPause("onStop")
                }
            }
        }
    }

    /*重新开始任务，脚本任务会先走下载*/
    private fun resumeTask() {
        if (AccessibilityViewModel.normalStartService.value == true) {
            if (!floatBallManager!!.state) {
                floatBallManager!!.changeState(true)
                when (currentType) {
                    TaskType.AUTO_EXECUTE_TASK -> {
                        AccessibilityViewModel.executeTask.postValue(true)
                    }

                    TaskType.AUTO_SHUT_DOWN_TASK -> {
                        AccessibilityViewModel.shutDownTask.postValue(true)
                    }

                    TaskType.AUTO_RESTART_TASK -> {
                        AccessibilityViewModel.restartTask.postValue(true)
                    }

                    TaskType.AUTO_EXIT_TASK -> {
                        AccessibilityViewModel.exitTask.postValue(true)
                    }

                    TaskType.AUTO_SETTING_TASK -> {
                        AccessibilityViewModel.settingTask.postValue(true)
                    }

                    TaskType.AUTO_QUERY_TASK -> {
                        AccessibilityViewModel.queryTask.postValue(1)
                    }

                    else -> {

                    }
                }
            }
        }


    }

    private fun addFloatMenuItem() {
        val stopItem: MenuItem = object : MenuItem(BackGroudSeletor.getdrawble("icon_stop", this)) {
            override fun action() {
                if (AccessibilityViewModel.normalStartService.value == true) {
                    if (floatBallManager!!.state) {
                        App.app.launch {
                            AccessibilityViewModel.stopTask.postValue(true)
                            stateExtra = true
                        }
                    } else {
                        App.app.launch {
                            showBottomToast("任务已经暂停了，请勿反复操作!!")
                        }
                    }
                }
                floatBallManager!!.closeMenu()
            }
        }
        val beginItem: MenuItem =
            object : MenuItem(BackGroudSeletor.getdrawble("icon_begain", this)) {
                override fun action() {
                    if (AccessibilityViewModel.normalStartService.value == true) {
                        if (!floatBallManager!!.state) {
                            App.app.launch {
                                resumeTask()
                                stateExtra = false
                            }
                        } else {
                            App.app.launch {
                                showBottomToast("任务正在执行中，请勿反复操作!!")
                            }
                        }
                    }
                    floatBallManager!!.closeMenu()
                }
            }
        floatBallManager!!.addMenuItem(stopItem).addMenuItem(beginItem).buildMenu()
    }


    private fun initObserver() {
        AccessibilityViewModel.onDate.observe(this, Observer {
            if (it) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = dateFormat.parse(SP.getString(Constant.EXPIRY_TIME))
                val upTime = date.time
                val now = System.currentTimeMillis()
                val delayTime = upTime - now
                App.app.launch {
                    delay(delayTime)
                    /**到期了么？*/
                    kotlin.runCatching {
                        Api.getApiService().queryAppTask(SP.getString(Constant.TOKEN), true)
                    }.onFailure {
                        it.printStackTrace()
                    }.onSuccess {
                        if (it.success) {
                            val jsonObject = JSONObject.parseObject(JSONObject.toJSONString(it.obj))
                            val stringDate = jsonObject.getString("expiryTime")
                            val upDate = dateFormat.parse(stringDate)
                            val upDateTime = upDate.time
                            if (upDateTime > upTime) {/*更新了!*/
                                SP.putString(Constant.EXPIRY_TIME, stringDate)
                                AccessibilityViewModel.onDate.value = true
                                return@launch
                            } else {
                                AccessibilityViewModel.stopTask.postValue(true)
                                suspendListenerPause()
                                onPauseListener = null
                                val intent =
                                    Intent(this@AccessibilityService, MainActivity::class.java)
                                intent.putExtra(Constant.UPTIME, "up_time")
                                startActivity(intent)
                            }
                        }
                    }

                }
            }


        })
        AccessibilityViewModel.shutDownTask.observe(this, Observer {
            if (it) {
                if (floatBallManager?.state != true) {
                    currentJob = App.app.launch {
                        showTopToast("执行指令中")
                        showBottomToast("关机")
                        taskType = TaskType.AUTO_SHUT_DOWN_TASK
                        suspendAutoShutDownEvent(
                            TaskProperty(
                                taskType, "", "", "", false, currentJob, AppUtils.getAppName()
                            )
                        )
                        if (onTaskFinishedListener != null) {
                            onTaskFinishedListener!!.taskFinish()
                        }
                    }
                } else {
                    App.app.launch {
                        showBottomToast("请先暂停当前任务")
                    }
                }
            }
        })
        AccessibilityViewModel.normalStartService.observe(this, Observer {
            if (it){
                floatBallManager?.changeState(false)
            }
        })
        AccessibilityViewModel.restartTask.observe(this, Observer {
            if (it) {
                if (floatBallManager?.state != true) {
                    currentJob = App.app.launch {
                        showTopToast("执行指令中")
                        showBottomToast("重启开始")
                        taskType = TaskType.AUTO_RESTART_TASK
                        suspendAutoRestartEvent(
                            TaskProperty(
                                taskType, "", "", "", false, currentJob, AppUtils.getAppName()
                            )
                        )
                        if (onTaskFinishedListener != null) {
                            onTaskFinishedListener!!.taskFinish()
                        }
                    }
                } else {
                    App.app.launch {
                        showBottomToast("请先暂停当前任务")
                    }
                }
            }
        })
        AccessibilityViewModel.queryTask.observe(this, Observer<Int> {
            if (it != 0) {
                cmdDo = true
                currentJob = App.app.launch {
                    showTopToast("查询任务中")
                    taskType = TaskType.AUTO_QUERY_TASK
                    queryTask(taskType!!, currentJob!!)
                }
            }
        })
        AccessibilityViewModel.showBottomToast.observe(this, Observer<String> {
            if (!TextUtils.isEmpty(it)) {
                App.app.launch {
                    showBottomToast(it)
                }
            }
        })
        AccessibilityViewModel.showTopToast.observe(this, Observer<String> {
            if (!TextUtils.isEmpty(it)) {
                App.app.launch {
                    showTopToast(it)


                }
            }
        })
        AccessibilityViewModel.exitTask.observe(this, Observer {
            if (it) {
                if (floatBallManager?.state != true) {
                    currentJob = App.app.launch {
                        showTopToast("执行指令中")
                        showBottomToast("正在退出")
                        taskType = TaskType.AUTO_EXIT_TASK
                        SP.putBoolean(Constant.EXIT, true)
                        Handler(Looper.getMainLooper()).postDelayed({
                            android.os.Process.killProcess(android.os.Process.myPid())
                        }, 2000)
                    }
                } else {
                    App.app.launch {
                        showBottomToast("请先暂停当前任务")
                    }
                }

            }
        })
        AccessibilityViewModel.executeTask.observe(this, Observer {
            if (it) {
                currentJob = App.app.launch {
                    showTopToast("正在执行任务")
                    taskType = TaskType.AUTO_EXECUTE_TASK
                    downLoadScriptTask()
                }
            }
        })
        AccessibilityViewModel.settingTask.observe(this, Observer {
            if (it) {
                currentJob = App.app.launch {
                    taskType = TaskType.AUTO_SETTING_TASK
                    settingTask(
                        TaskProperty(
                            taskType, packageName, "", "", false, currentJob, AppUtils.getAppName()
                        )
                    )
                }
            }
        })
        AccessibilityViewModel.overLayerTask.observe(this, Observer {
            if (it) {
                currentJob = App.app.launch {
                    taskType = TaskType.AUTO_OVER_LAYER_TASK
                    overLayerTask(
                        TaskProperty(
                            taskType, "", "", "", false, currentJob, AppUtils.getAppName()
                        )
                    )
                }
            }
        })
        AccessibilityViewModel.equipmentMaintenanceTask.observe(this, Observer {
            if (AccessibilityViewModel.normalStartService.value == true) {
                currentJob = App.app.launch {
                    if (it) {
                        showTopToast("正在进行每日维护，请勿干扰！")
                        taskType = TaskType.AUTO_MAINTENANCE_TASK

                        equipmentMaintenanceTask(taskType!!, "", currentJob!!)
                        onFinishListener?.onFinish("onFinish")
                        onFinishListener = null
                    } else {
                        showTopToast("正在进行系统维护，请勿干扰！")
                        taskType = TaskType.AUTO_MAINTENANCE_TASK
                        if (!SP.getBoolean(Constant.AUTO_START)) {
                            showTopToast("正在进行系统维护，请勿干扰！")
                            showBottomToast("自启动授权中")
                            suspendAutoStartTaskEvent(
                                TaskProperty(
                                    taskType!!, "", "", "", false, currentJob, ""
                                )
                            )
                        }
                        if (!SP.getBoolean(Constant.PERMISSION)) {
                            showTopToast("正在进行系统维护，请勿干扰！")
                            showBottomToast("权限授权中")
                            suspendAutoPermissionEvent(
                                TaskProperty(
                                    taskType!!, packageName, "", "", false, currentJob, ""
                                )
                            )
                        }
                        suspendAutoClearEvent(  TaskProperty(
                            taskType!!, packageName, "", "", false, currentJob, ""
                        ))
                        equipmentMaintenanceTask(taskType!!, "", currentJob!!)/**/
                        val intent = Intent(this@AccessibilityService,MainActivity::class.java)
                        intent.putExtra(Constant.SCRIPT_APP,Constant.SCRIPT_APP)
                        startActivity(intent)
                    }
                }
            }

        })
        AccessibilityViewModel.stopTask.observe(this, Observer {
            floatBallManager?.closeMenu()
            if (it) {
                App.app.launch {
                    showTopToast("任务暂停")
                    stopTask(null, TaskType.AUTO_STOP_TASK)
                }
            } else {
                if (AccessibilityViewModel.normalStartService.value == true) {
                    App.app.launch {
                        showTopToast("任务暂停")
                        stopTask()
                    }
                }
            }

        })
        AccessibilityViewModel.retry.observe(this, Observer {
            if (it != null) {
                App.app.launch {
                    if (it.job == null) {
                        return@launch
                    }

                    when (it.taskType) {

                        TaskType.AUTO_SETTING_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.settingTask.postValue(true)
                        }

                        TaskType.AUTO_QUERY_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.queryTask.postValue(1)
                        }


                        TaskType.AUTO_RESTART_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.shutDownTask.postValue(true)
                        }

                        TaskType.AUTO_SHUT_DOWN_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.shutDownTask.postValue(true)
                        }

                        TaskType.AUTO_MAINTENANCE_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.equipmentMaintenanceTask.postValue(true)
                        }

                        TaskType.AUTO_STOP_SCRIPT_TASK -> {

                        }

                        TaskType.AUTO_OVER_LAYER_TASK -> {
                            it.job.cancel()
                            showBottomToast("超时重试中!")
                            AccessibilityViewModel.overLayerTask.postValue(true)
                        }

                        else -> {
                            L.e("没有拦截的错误信息")
                        }
                    }
                }
            }
        })
        AccessibilityViewModel.heartBeatTask.observe(this, Observer {
            if (it) {
                heartBeatJob = App.app.launch {
                    withContext(Dispatchers.IO) {
                        while (true) {
                            hearBeatTask(true)
                            delay(15000)
                        }
                    }
                }
            } else {
                if (heartBeatJob != null) {
                    heartBeatJob!!.cancel()
                    App.app.launch {
                        hearBeatTask(false)
                    }
                }
            }
        })
        AccessibilityViewModel.logout.observe(this, Observer {
            if (it != null) {
                if (floatBallManager?.state != true) {
                    App.app.launch {
                        AccessibilityViewModel.normalStartService.postValue(false)
                        AccessibilityViewModel.settingTask.postValue(false)
                        AccessibilityViewModel.heartBeatTask.postValue(false)
                        AccessibilityViewModel.stopTask.postValue(false)
                        if (EasyFloat.isShow("1")) {
                            EasyFloat.dismiss("1")
                        }
                        if (EasyFloat.isShow("2")) {
                            EasyFloat.dismiss("2")
                        }
                        startActivity(
                            Intent(
                                this@AccessibilityService, LoginActivity::class.java
                            )
                        )
                        floatBallManager?.initState()
                        it.finish()
                    }
                } else {
                    App.app.launch {
                        showBottomToast("请先暂停当前任务")
                    }
                }
            }
        })
        AccessibilityViewModel.window.observe(this, Observer {
            if (it) {
                App.app.launch {
                    showWindow()
                    EventController.INSTANCE.addEvent(
                        AutoRefreshLayerEvent(
                            TaskProperty(
                                TaskType.AUTO_CAPTURE_TASK,
                                "",
                                "",
                                "",
                                false,
                                null,
                                AppUtils.getAppName()
                            )
                        )
                    ).execute(object : EventAction.OnEventCompleted {
                        override fun eventCompleted(name: String) {
                        }
                    })
                }
            }
        })
        AccessibilityViewModel.report.observe(this, Observer {
            if (!TextUtils.isEmpty(it)) {
                App.app.launch {
                    showBottomToast("正在上报")
                }
                appExecutionFeedBack(it)
            }
        })
        AccessibilityViewModel.capture.observe(this, Observer {
            if (it != null) {
                captureTask(mediaProjection = it)
            }
        })
        AccessibilityViewModel.cmdTask.observe(this, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    App.app.launch {
                        when (it[0]) {
                            "DEVICE_RESTARTS" -> {
                                App.app.launch {
                                    cmdWork = it[0]
                                    showBottomToast("停止脚本，正在进行重启指令")
                                    AccessibilityViewModel.stopTask.postValue(true)
                                    suspendListenerPause()
                                    onPauseListener = null
                                    floatBallManager!!.isCanOpen = false
                                    AccessibilityViewModel.restartTask.postValue(true)
                                    floatBallManager!!.isCanOpen = true
                                    suspendOnTaskFinished()
                                    onTaskFinishedListener = null
                                    cmdWork = ""
                                }
                            }

                            "DEVICE_SHUTS_DOWN" -> {
                                cmdWork = it[0]
                                showBottomToast("停止脚本，正在进行关机指令")
                                AccessibilityViewModel.stopTask.postValue(true)
                                suspendListenerPause()
                                onPauseListener = null
                                AccessibilityViewModel.shutDownTask.postValue(true)
                                floatBallManager!!.isCanOpen = false
                                suspendOnTaskFinished()
                                floatBallManager!!.isCanOpen = true
                                onTaskFinishedListener = null
                                cmdWork = ""
                            }

                            "RUN_PAUSED" -> {
                                cmdWork = it[0]
                                stateExtra = true
                                showBottomToast("停止脚本，正在进行停止指令")
                                AccessibilityViewModel.stopTask.postValue(true)
                                suspendListenerPause()
                                onPauseListener = null
                                cmdWork = ""
                            }

                            "RUN_CONTINUES" -> {
                                stateExtra = false
                                cmdWork = it[0]
                                showBottomToast("开始脚本，正在进行开始指令")/*执行完插队任务后，继续恢复上一个任务*/
                                resumeTask()
                                cmdWork = ""
                            }

                            "RUN_EXITS" -> {
                                cmdWork = it[0]
                                showBottomToast("停止脚本，正在进行退出指令")
                                AccessibilityViewModel.stopTask.postValue(true)
                                suspendListenerPause()
                                onPauseListener = null
                                floatBallManager!!.isCanOpen = false
                                AccessibilityViewModel.exitTask.postValue(true)
                                cmdWork = ""
                            }

                            "DEVICE_MAINTENANCE" -> {
                                cmdWork = it[0]
                                showBottomToast("停止脚本，正在进行维护指令")
                                AccessibilityViewModel.stopTask.postValue(true)
                                suspendListenerPause()
                                onPauseListener = null
                                floatBallManager!!.isCanOpen = false
                                AccessibilityViewModel.equipmentMaintenanceTask.postValue(true)
                                suspendListenerFinish()
                                onFinishListener = null
                                floatBallManager!!.isCanOpen = true
                                resumeTask()
                                cmdWork = ""
                            }
                        }
                    }
                }
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
        val appName = jsonObject.getString("appName")
        val boolean = hasPackageName(uniqueCode)
        if (!boolean) {
            SuspendEventManager.suspendAutoUninstallPackEvent(
                TaskProperty(
                    scope, uniqueCode, "", "", false, job, appName
                )
            )
            suspendAutoClearEvent(
                TaskProperty(
                    scope, uniqueCode, "", "", false, job, AppUtils.getAppName()
                )
            )

        }
        if (it.isNotEmpty()) {
            equipmentUnInstallTask(scope, it, job)
        }
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
                        scope, "", path, "", false, job, appName
                    )
                )
                if (MsgType.SUCCESS.name == msgType) {
                    showBottomToast("权限申请")
                    if (appName == "猎豹清理") {
                        suspendAutoPermissionEvent(
                            TaskProperty(
                                scope, uniqueCode, "", "", false, job, appName
                            )
                        )
                    } else {
                        suspendAutoPermissionAppsEvent(
                            TaskProperty(
                                scope, uniqueCode, "", "", false, job, appName
                            )
                        )
                    }

                    showBottomToast("清理后台中")
                    suspendAutoClearEvent(
                        TaskProperty(
                            scope, "", "", "", false, job, AppUtils.getAppName()
                        )
                    )
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
            ScreenUtils.getScreenWidth(),
            ScreenUtils.getScreenHeight(),
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

    private suspend fun overLayerTask(scope: TaskProperty) {
        suspendAutoOverLayerEvent(scope.job, this@AccessibilityService)
    }

    private suspend fun clearCacheTask(taskProperty: TaskProperty) {
        when (findLastLoginInfo()) {
            2 -> {
                showTopToast("正在清除缓存!")
                kotlin.runCatching {
                    Api.getApiService().queryMaintainInfo(SP.getString(Constant.TOKEN))
                }.onSuccess {
                    if (it.success) {
                        val jsonObject = JSONObject.parseObject(JSONObject.toJSONString(it.obj))
                        val jsonArray = jsonObject.getJSONArray("appList")
                        if (jsonArray.size > 0) {
                            L.e("need refresh cache size :${jsonArray.size}")
                            for (i in 0 until jsonArray.size) {
                                val targetObject = jsonArray.getJSONObject(i)
                                val remotePkName = targetObject.getString("uniqueCode")
                                val appName = targetObject.getString("appName")
                                L.e("refreshing cache app:${appName}")
                                val enable = targetObject.getBoolean("enable")
                                if (enable) {
                                    val apps: List<PackageInfo> =
                                        packageManager.getInstalledPackages(0)
                                    for (localApp in apps) {/*查找所有已经安装的app*/
                                        if ((localApp.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                                            if (localApp.packageName.equals(remotePkName)) {/*清除缓存*/
                                                showBottomToast("清除缓存")
                                                suspendAutoClearCacheEvent(
                                                    TaskProperty(
                                                        taskProperty.taskType,
                                                        remotePkName,
                                                        "",
                                                        "",
                                                        false,
                                                        taskProperty.job,
                                                        appName
                                                    )
                                                )
                                                showBottomToast("清除后台")
                                                suspendAutoClearEvent(taskProperty)
                                                showBottomToast("重新授权")
                                                if (appName == "猎豹清理") {
                                                    suspendAutoPermissionEvent(
                                                        TaskProperty(
                                                            taskProperty.taskType,
                                                            remotePkName,
                                                            "",
                                                            "",
                                                            false,
                                                            taskProperty.job,
                                                            appName
                                                        )
                                                    )
                                                } else {
                                                    suspendAutoPermissionAppsEvent(
                                                        TaskProperty(
                                                            taskProperty.taskType,
                                                            remotePkName,
                                                            "",
                                                            "",
                                                            false,
                                                            taskProperty.job,
                                                            appName
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }

                }.onFailure { it.printStackTrace() }
            }
        }
    }

    private suspend fun settingTask(scope: TaskProperty) {
        /**特殊任务执行中，不允许被中断，收到任何中断指令需要上报，并提示*/
        floatBallManager!!.changeState(true)
        floatBallManager!!.isCanOpen = false
        if (AccessibilityViewModel.logout.value != null) {
            AccessibilityViewModel.logout.postValue(null)
        }
        if (!SP.getBoolean(Constant.AUTO_START)) {
            showTopToast("正在进行系统维护，请勿干扰！")
            showBottomToast("自启动授权中")
            suspendAutoStartTaskEvent(scope)
        }
        if (!SP.getBoolean(Constant.PERMISSION)) {
            showTopToast("正在进行系统维护，请勿干扰")
            showBottomToast("权限授权中")
            suspendAutoPermissionEvent(scope)
        }
        showBottomToast("清理后台中")
        suspendAutoClearEvent(scope)
        DaoTool.addAccount(SP.getString(Constant.LOGIN_NAME))
        clearCacheTask(scope)
        showTopToast("正在进行执行前检测")
        showBottomToast("查询微信状态")
        suspendAutoCheckWXEvent(scope)
        showBottomToast("查询支付宝状态")
        suspendAutoCheckAliPayEvent(scope)
        suspendAutoClearEvent(scope)
        floatBallManager!!.isCanOpen = true
        if (!scope.job.isCancelled) {
            AccessibilityViewModel.queryTask.postValue(1)
        }
    }


    @SuppressLint("CheckResult")
    private suspend fun hearBeatTask(state: Boolean) {/*同步*/
        kotlin.runCatching {
            Api.getApiService().deviceReport(
                SP.getString(Constant.TOKEN),
                if (SP.getString(Constant.DEVICE_ID) == "") 0 else SP.getString(Constant.DEVICE_ID)
                    .toInt(),
                level,
                keep,
                getSignal(),
                state,
                stateExtra
            )
        }.onFailure { it.printStackTrace() }.onSuccess {
            if (state) {
                val jsonArray = JSONArray.parseArray(JSONObject.toJSONString(it.obj))
                if (jsonArray != null) {
                    if (jsonArray.isEmpty()) {
                        return@onSuccess
                    }
                    val item: List<String> = jsonArray.toList() as List<String>
                    if (item.isEmpty()) {
                        return@onSuccess
                    } else {/*接收指令*/
                        if (TextUtils.isEmpty(cmdWork)) {
                            if (cmdDo){
                                AccessibilityViewModel.cmdTask.postValue(item)
                            }
                        }
                        return@onSuccess
                    }
                }
            }
        }


    }


    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        L.e("Service onCreate")
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
    }

    private fun buildNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("${getString(R.string.app_name)}保持运行中")
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(R.mipmap.logo).setWhen(System.currentTimeMillis()).setContentIntent(null)
            .setChannelId(CHANEL_ID).setVibrate(LongArray(0)).build()
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

    private fun hasPackageName(appPackageName: String): Boolean {
        // get all apps
        val apps: List<PackageInfo> = packageManager.getInstalledPackages(0)
        for (item in apps) {
            val name = item.packageName
            if (name.equals(appPackageName)) {/*不需要安装*/
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
        L.e("Service onDestroy")
        EventBus.getDefault().unregister(this)
        heartBeatJob?.cancel()
        currentJob?.cancel()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        for (mDisposable in mDisposable) {
            if (!mDisposable.isDisposed) {
                mDisposable.dispose()
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        imageReader.close()
        disableSelf()
        BatteryUtils.unregisterBatteryStatusChangedListener(listenerBattery)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun reportEvent(event: BusinessEvent) {
        L.e(event.jsonValue)
        val jsonObject = JSONObject.parseObject(event.jsonValue)
        val status = jsonObject.getInteger("status")
        val result = jsonObject.getString("result")
        val title = jsonObject.getString("title")
        App.app.launch {
            showTopToast(result)
            delay(2000)
            scriptReport(result, status, title)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun scriptStopSelf(event: StopSelfEvent) {
        App.app.launch {
            backToMain = false
            AccessibilityViewModel.stopTask.postValue(true)
            suspendListenerPause()
            onPauseListener = null
            if (!floatBallManager!!.state) {
                floatBallManager!!.changeState(true)
                AccessibilityViewModel.executeTask.postValue(true)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun reportProfit(event: ReportProfitEvent) {
        L.e(event.json)
        val jsonObject = JSONObject.parseObject(event.json)
        val money = jsonObject.getString("money")
        App.app.launch {
            kotlin.runCatching {
                Api.getApiService().deviceReportProfit(
                    SP.getString(Constant.TOKEN), money.toDouble(), runRecordId!!.toInt()
                )
            }.onSuccess { L.e("收益上报成功!") }.onFailure {
                L.e(it.message)
            }

        }
    }

    private fun getBitmapString(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
        L.e("onUnBind!!!")
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)

    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        L.e("bindService:${conn.toString()}")
        return super.bindService(service, conn, flags)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
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
            AccessibilityViewModel.queryTask.postValue(1)
        }.onSuccess {
            if (it.success) {
                if (!TextUtils.isEmpty(targetPackAgeName)) {
                    if (!hasPackageName(targetPackAgeName!!, versionCode!!)) {
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
                        return@onSuccess
                    } else {
                        AccessibilityViewModel.report.postValue("${appName}不存在!")
                    }
                } else {
                    AccessibilityViewModel.report.postValue("执行脚本失败，可能没有进行查询任务!")
                }
            } else {
                AccessibilityViewModel.report.postValue("下载脚本失败，参数为false")
            }
            targetExit = false
            AccessibilityViewModel.queryTask.postValue(1)
        }

    }


}
