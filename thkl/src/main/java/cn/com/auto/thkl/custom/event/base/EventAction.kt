package cn.com.auto.thkl.custom.event.base

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.com.auto.thkl.App
import cn.com.auto.thkl.App.Companion.OFFSET_VALUE
import cn.com.auto.thkl.App.Companion.service
import cn.com.auto.thkl.utils.L
import com.blankj.utilcode.util.ScreenUtils


abstract class EventAction(eventName: String, eventType: Set<Int>) : Event {
    override val name: String = eventName //初始化必须要的
    override var type: Set<Int> = eventType //初始化必须要的
    override var isWorking: Boolean = false
    val rect = Rect()
    var eventCompleted: OnEventCompleted? = null

    interface OnEventCompleted {
        fun eventCompleted(name: String)
    }

    fun setOnEventCompleted(eventCompleted: OnEventCompleted?) {
        this.eventCompleted = eventCompleted
    }

    override fun cancel() {
        App.handler.removeCallbacksAndMessages(null)
    }


    fun runEvent(runnable: Runnable) {
        if (task.job != null){
            if (!task.job.isCancelled) {
                runTime++
                App.handler.removeCallbacksAndMessages(null)
                App.handler.postDelayed(runnable, 500)
            }
        }else{
            runTime++
            App.handler.removeCallbacksAndMessages(null)
            App.handler.postDelayed(runnable, 500)
        }

    }

    fun runEvent(runnable: Runnable, delay: Float) {
        if (task.job != null){
            if (!task.job.isCancelled) {
                runTime++
                App.handler.removeCallbacksAndMessages(null)
                App.handler.postDelayed(runnable, (delay * 1000).toLong())
            }
        }else{
            runTime++
            App.handler.removeCallbacksAndMessages(null)
            App.handler.postDelayed(runnable, (delay * 1000).toLong())
        }

    }

    override fun execute(event: AccessibilityEvent?) {
        start(service, event)
    }


    abstract fun start(service: AccessibilityService, event: AccessibilityEvent?)
    fun back(service: AccessibilityService) {
        runTime++
        App.handler.postDelayed({
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }, 1500)
    }


    var x = (ScreenUtils.getAppScreenWidth() / 2).toFloat()//起点x
    var y = 0f//起点y
    var perMoveY = 0f //上下每次滑动的距离
    var perMoveX = 0f //左右每次滑动的距离

    @SuppressLint("NewApi")
    fun scrollDownPoint(
        accessibilityNodeInfo: AccessibilityNodeInfo?,
        service: AccessibilityService,
        event: AccessibilityEvent
    ) {
        runTime++
        if (isWorking) {
            return
        }
        val builder = GestureDescription.Builder()
        val path = Path()
        if (accessibilityNodeInfo == null) {
            return
        }
        if (accessibilityNodeInfo.parent == null) {
            return
        }
        accessibilityNodeInfo.parent.getBoundsInScreen(rect)
        y = rect.bottom.toFloat() - App.OFFSET_VALUE
        if (perMoveY == 0f) {/*设置滑动距离，每一次滑动根据父布局长度的一半来定*/
            perMoveY = rect.top + App.OFFSET_VALUE
        }
        path.moveTo(x, y)
        path.lineTo(x, perMoveY)
        builder.addStroke(GestureDescription.StrokeDescription(path, 500, 500))
        val gesture = builder.build()
        isWorking = true
        service.dispatchGesture(
            gesture,
            @RequiresApi(Build.VERSION_CODES.N) object :
                AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    L.e("滑动完成")
                    isWorking = false
                    super.onCompleted(gestureDescription)
                    runTime++
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.e("滑动失败")
                    isWorking = false
                }
            },
            null
        )

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun scrollRightPoint(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        runTime++
        if (isWorking) {
            return
        }
        val builder = GestureDescription.Builder()
        val path = Path()
        if (accessibilityNodeInfo == null) {
            return
        }
        accessibilityNodeInfo.getBoundsInScreen(rect)
        if (perMoveX == 0f) {/*设置滑动距离，每一次滑动根据父布局长度的一半来定*/
            perMoveX = rect.left + OFFSET_VALUE
        }
        path.moveTo(x, y)
        path.lineTo(perMoveX, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 500, 500))
        val gesture = builder.build()
        isWorking = true
        service.dispatchGesture(
            gesture,
            @RequiresApi(Build.VERSION_CODES.N) object :
                AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    L.e("滑动完成")
                    isWorking = false
                    super.onCompleted(gestureDescription)
                    runTime++
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.e("滑动失败")
                    isWorking = false
                }
            },
            null
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun scrollUpPoint(
        accessibilityNodeInfo: AccessibilityNodeInfo,
        service: AccessibilityService,
        event: AccessibilityEvent
    ) {
        runTime++
        if (isWorking) {
            return
        }
        val builder = GestureDescription.Builder()
        val path = Path()
        accessibilityNodeInfo.parent.getBoundsInScreen(rect)
        y = rect.bottom.toFloat() - OFFSET_VALUE
        if (perMoveY == 0f) {/*设置滑动距离，每一次滑动根据父布局长度的一半来定*/
            perMoveY = rect.top + OFFSET_VALUE
        }
        path.moveTo(x, perMoveY)
        path.lineTo(x, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 500, 500))
        val gesture = builder.build()
        isWorking = true
        service.dispatchGesture(
            gesture,
            @RequiresApi(Build.VERSION_CODES.N) object :
                AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    L.e("滑动完成")
                    isWorking = false
                    super.onCompleted(gestureDescription)
                    runTime++
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.e("滑动失败")
                    isWorking = false
                }
            },
            null
        )


    }

    @SuppressLint("NewApi")
    fun clickPoint(x: Float, y: Float, service: AccessibilityService) {
        runTime++
        if (isWorking) {
            return
        }
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 500, 100))
        val gesture = builder.build()
        isWorking = true
        service.dispatchGesture(
            gesture,
            @RequiresApi(Build.VERSION_CODES.N) object :
                AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    L.e("点击完成")
                    runTime++
                    isWorking = false/*当点击完成之后，根据上一个业务进行*/
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.e("点击失败")/*出现失败，可能是第三方正在占用，重新进行一次*/
                    isWorking = false
                }
            },
            null
        )

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun clickPoint(service: AccessibilityService) {
        runTime++
        if (isWorking) {
            return
        }
        val builder = GestureDescription.Builder()
        val path = Path()
        val x = ((rect.right + rect.left) / 2).toFloat()
        val y = ((rect.bottom + rect.top) / 2).toFloat()
        path.moveTo(x, y)
        path.lineTo(x, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 500, 100))
        val gesture = builder.build()
        isWorking = true
        service.dispatchGesture(
            gesture,
            @RequiresApi(Build.VERSION_CODES.N) object :
                AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    L.e("点击完成")
                    runTime++
                    isWorking = false/*当点击完成之后，根据上一个业务进行*/
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.e("点击失败")/*出现失败，可能是第三方正在占用，重新进行一次*/
                    isWorking = false
                }
            },
            null
        )
    }


}