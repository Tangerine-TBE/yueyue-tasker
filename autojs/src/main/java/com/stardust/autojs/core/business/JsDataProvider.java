package com.stardust.autojs.core.business;

import android.app.Application;
import android.util.Log;

import com.stardust.app.AppOpsKt;
import com.stardust.autojs.core.pref.Pref;
import com.stardust.autojs.event.BusinessEvent;
import com.stardust.autojs.event.ReportProfitEvent;
import com.stardust.autojs.event.StopSelfEvent;
import com.stardust.autojs.runtime.api.AppUtils;

import org.greenrobot.eventbus.EventBus;

import androidx.preference.PreferenceManager;

public class JsDataProvider {
    public JsDataProvider() {

    }

    public final void requestFeedBackFromJs(String json) {
        EventBus.getDefault().post(new BusinessEvent(json));
    }

    public final void requestDeviceReportProfit(String json) {
        EventBus.getDefault().post(new ReportProfitEvent(json));
    }

    public final void requestStopSelf() {
        EventBus.getDefault().post(new StopSelfEvent(1));
    }
}
