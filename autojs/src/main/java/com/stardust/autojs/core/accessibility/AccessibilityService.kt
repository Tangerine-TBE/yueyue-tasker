package com.stardust.autojs.core.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.annotation.RequiresApi
import com.stardust.autojs.core.pref.Pref
import com.stardust.view.accessibility.AccessibilityService

open class AccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        val serviceInfo = serviceInfo
        if (Pref.isStableModeEnabled) {
            serviceInfo.flags =
                serviceInfo.flags and AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS.inv()
        } else {
            serviceInfo.flags =
                serviceInfo.flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Pref.isGestureObservingEnabled) {
                serviceInfo.flags =
                    serviceInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            } else {
                serviceInfo.flags =
                    serviceInfo.flags and AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE.inv()
            }
        }
        serviceInfo.flags =
            serviceInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        setServiceInfo(serviceInfo)
        super.onServiceConnected()
        /*系统设置时*/
    }

    /*进行一次基础遍历*/



}