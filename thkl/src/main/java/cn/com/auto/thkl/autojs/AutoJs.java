package cn.com.auto.thkl.autojs;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

import com.stardust.app.GlobalAppContext;
import com.stardust.autojs.core.console.GlobalConsole;
import com.stardust.autojs.runtime.ScriptRuntime;
import com.stardust.autojs.runtime.accessibility.AccessibilityConfig;
import com.stardust.autojs.runtime.api.AppUtils;
import com.stardust.autojs.runtime.exception.ScriptException;
import com.stardust.autojs.runtime.exception.ScriptInterruptedException;
import com.stardust.view.accessibility.AccessibilityService;
import com.stardust.view.accessibility.LayoutInspector;
import com.stardust.view.accessibility.NodeInfo;

import cn.com.auto.thkl.devplugin.DevPlugin;
import cn.com.auto.thkl.custom.SystemTransactionManager;
import cn.com.auto.thkl.Pref;
import cn.com.auto.thkl.R;
import cn.com.auto.thkl.fileprovider.AppFileProvider;
import cn.com.auto.thkl.utils.AccessibilityServiceTool;


/**
 * Created by Stardust on 2017/4/2.
 */

public class AutoJs extends com.stardust.autojs.AutoJs {

    private static AutoJs instance;

    public static AutoJs getInstance() {
        return instance;
    }

    private boolean enableDebugLog = false;

    public synchronized static void initInstance(Application application) {
        if (instance != null) {
            return;
        }
        instance = new AutoJs(application);
    }


    private AutoJs(final Application application) {
        super(application);
        AccessibilityService.Companion.addDelegate(400,new SystemTransactionManager());
        getScriptEngineService().registerGlobalScriptExecutionListener(new ScriptExecutionGlobalListener());
    }


    @Override
    protected AppUtils createAppUtils(Context context) {
        return new AppUtils(context, AppFileProvider.AUTHORITY);
    }

    @Override
    protected GlobalConsole createGlobalConsole() {
        return new GlobalConsole(getUiHandler()) {
            @Override
            public String println(int level, CharSequence charSequence) {
                String log = super.println(level, charSequence);
                DevPlugin.INSTANCE.log(log);
                return log;
            }
        };
    }

    public void ensureAccessibilityServiceEnabled() {
        if (AccessibilityService.Companion.getInstance() != null) {
            return;
        }
        String errorMessage = null;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(GlobalAppContext.get())) {
            errorMessage = GlobalAppContext.getString(R.string.text_auto_operate_service_enabled_but_not_running);
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = GlobalAppContext.getString(R.string.text_enable_accessibility_service_by_root_timeout);
                }
            } else {
                errorMessage = GlobalAppContext.getString(R.string.text_no_accessibility_permission);
            }
        }
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            throw new ScriptException(errorMessage);
        }
    }

    @Override
    public void waitForAccessibilityServiceEnabled() {
        if (AccessibilityService.Companion.getInstance() != null) {
            return;
        }
        String errorMessage = null;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(GlobalAppContext.get())) {
            errorMessage = GlobalAppContext.getString(R.string.text_auto_operate_service_enabled_but_not_running);
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = GlobalAppContext.getString(R.string.text_enable_accessibility_service_by_root_timeout);
                }
            } else {
                errorMessage = GlobalAppContext.getString(R.string.text_no_accessibility_permission);
            }
        }
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            if (!AccessibilityService.Companion.waitForEnabled(-1)) {
                throw new ScriptInterruptedException();
            }
        }
    }

    @Override
    protected AccessibilityConfig createAccessibilityConfig() {
        AccessibilityConfig config = super.createAccessibilityConfig();
        return config;
    }

    @Override
    protected ScriptRuntime createRuntime() {
        return super.createRuntime();
    }

    public void debugInfo(String content) {
        if (this.enableDebugLog) {
            AutoJs.getInstance().getGlobalConsole().println(Log.VERBOSE, content);
        }
    }

    public void setDebugEnabled(boolean enableDebugLog) {
        this.enableDebugLog = enableDebugLog;
    }
}
