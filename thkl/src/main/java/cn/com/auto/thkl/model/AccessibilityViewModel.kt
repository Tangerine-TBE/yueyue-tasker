package cn.com.auto.thkl.model

import android.app.Activity
import android.media.projection.MediaProjection
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.com.auto.thkl.custom.task.TaskProperty

object AccessibilityViewModel :ViewModel(){
    var heartBeatTask = MutableLiveData(false) /*登录成功后，开启心跳*/
    var equipmentMaintenanceTask = MutableLiveData(false)
    var queryTask = MutableLiveData(0)/*查询脚本任务*/
    var executeTask = MutableLiveData(false)
    var settingTask = MutableLiveData(false)/*设置任务开启*/
    var exitTask = MutableLiveData(false)
    var shutDownTask = MutableLiveData(false)
    var restartTask = MutableLiveData(false)
    var stopTask = MutableLiveData(false)
    var overLayerTask = MutableLiveData(false)
    var report = MutableLiveData("")/*上报信息*/
    var normalStartService = MutableLiveData(false) /*进程应该要进入哪个界面*/
    var capture = MutableLiveData<MediaProjection>() /*后台截屏*/
    var window = MutableLiveData(false)
    var upTimeTips = MutableLiveData(false)
    var retry = MutableLiveData<TaskProperty>()
    var logout = MutableLiveData<Activity>()
    var showBottomToast = MutableLiveData<String>()
    var showTopToast = MutableLiveData<String>()
    var onDate = MutableLiveData(false)

}