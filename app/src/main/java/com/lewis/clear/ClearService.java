package com.lewis.clear;


import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class ClearService extends AccessibilityService {
    private static final String TAG = ClearService.class.getSimpleName();

    private static final String TEXT_FORCE_STOP = "强行停止";
    private static final String TEXT_DETERMINE = "确定";
    private static final CharSequence SETTING_PACKAGE_NAME = "com.android.settings";
    private static final CharSequence APP_INSTALL_DETAILS = "com.android.settings.applications.InstalledAppDetailsTop";
    private static final CharSequence UNINSTALL_ALERT_DIALOG = "android.app.AlertDialog";

    private boolean isAppDetail;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (null == event || null == event.getSource()) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getPackageName().equals(SETTING_PACKAGE_NAME)) {
            final CharSequence className = event.getClassName();
            if (className.equals(APP_INSTALL_DETAILS)) {
                simulationClick(event, TEXT_FORCE_STOP);
                performGlobalAction(GLOBAL_ACTION_BACK);
                isAppDetail = true;
            }
            if (isAppDetail && className.equals(UNINSTALL_ALERT_DIALOG)) {
                simulationClick(event, TEXT_DETERMINE);
                performGlobalAction(GLOBAL_ACTION_BACK);
                isAppDetail = false;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void simulationClick(AccessibilityEvent event, String text) {
        List<AccessibilityNodeInfo> nodeInfoList = event.getSource().findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodeInfoList) {
            if (node.isClickable() && node.isEnabled()) {
                Log.i(TAG, "click text: " + text + " class name: " + node.getClassName());
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
