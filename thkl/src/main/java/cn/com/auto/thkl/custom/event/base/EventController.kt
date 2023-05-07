package cn.com.auto.thkl.custom.event.base

import android.view.accessibility.AccessibilityEvent
import cn.com.auto.thkl.model.AccessibilityViewModel
import cn.com.auto.thkl.utils.L
import kotlin.concurrent.thread


class EventController private constructor() {
    private var currentEvent: Event? = null
    private var eventTimeTask: EventTimeTask? = null

    init {
        if (!flag) {
            flag = true
        } else {
            throw Throwable("SingleTon is being attacked.")
        }
    }


    companion object {
        private var flag = false
        val INSTANCE = CommonSingletonHolder.holder

        val SYSTEM_EVENT = setOf(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.WINDOWS_CHANGE_ACTIVE
        )
        val TOUCH_EVENT = setOf(
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        )
    }

    /**
     * 静态内部类单例
     */
    private object CommonSingletonHolder {
        val holder = EventController()
    }

    /**每次添加事件，记录添加事件的时间*/
    fun addEvent(event: Event): EventController {
        this.currentEvent = event
        if (eventTimeTask != null) {
            eventTimeTask!!.stop = true
        }
        eventTimeTask =
            EventTimeTask(event)
        thread {
            eventTimeTask!!.run()
        }
        return this
    }

    fun execute(eventCompleted: EventAction.OnEventCompleted?) {
        if (currentEvent != null) {
            (currentEvent as EventAction).setOnEventCompleted(eventCompleted)
            currentEvent?.execute(null)
        }

    }

    fun removeEvent(event: Event?, msgType: MsgType) {

        if (eventTimeTask != null) {
            eventTimeTask!!.stop = true
        }
        if (event != null) {
            if (event.task.job != null) {
                if (!event.task.job.isCancelled) {
                    event.runTime++
                    (event as EventAction).eventCompleted!!.eventCompleted(msgType.name)
                    this.currentEvent = null
                }
            } else {
                event.runTime++
                (event as EventAction).eventCompleted!!.eventCompleted(msgType.name)
                this.currentEvent = null
            }
        }


    }


    fun getCurrentEvent(): Event? {
        return currentEvent
    }


    class EventTimeTask(
        private val event: Event,
    ) : Runnable {
        var stop = false
            set(value) {
                if (value) {
                    L.e("${event.name}:事件完成")
                }
                field = value
            }

        override fun run() {
            L.e("${event.name}:事件开始")
            while (!stop) {
                /**运行超时策略*/
                /**runTime不作为一个事件的总体超时，而是作为一个观察对象存在，每一个步伐递进与回退都需要及时的更改其属性
                 * */
                val last = event.runTime
                Thread.sleep(10 * 1000)
                val now = event.runTime
                if (last == now) {
                    /**超时*/
                    /**汇报事件，并重启当前任务*/
                    if (!stop) {
                        L.e("${event.name}-运行超时")
                        INSTANCE.removeEvent(event, MsgType.TIME_OUT)
                        AccessibilityViewModel.retry.postValue(event.task)/*重试任务，非事件！*/
                        AccessibilityViewModel.report.postValue("${event.name}-运行超时")/*超时汇报*/
                        return
                    }
                }
            }
        }

    }


}