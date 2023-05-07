package com.stardust.autojs.core.business;

import android.util.Log;

import com.stardust.autojs.event.BusinessEvent;

import org.greenrobot.eventbus.EventBus;

public class JsDataProvider {
    public JsDataProvider() {

    }
    public final void requestFeedBackFromJs(String json) {
        EventBus.getDefault().post(new BusinessEvent(json));
    }
}
