package cn.com.auto.thkl.custom.event.base

import android.view.accessibility.AccessibilityEvent
import cn.com.auto.thkl.custom.task.TaskProperty


/**时间，地点，人物*/
interface Event {
    /*超时控制者*/
    val task: TaskProperty

    /*事件名称*/
    val name: String

    /*步伐控制器*/
    var currentStep: Int

    /*事件类型*/
    var type: Set<Int>

    /*是否处于点击或滑动状态*/
    var isWorking: Boolean

    /*步伐观察器*/
    var runTime: Int



    /**执行*/
    fun execute(event: AccessibilityEvent?)
    fun cancel()




}