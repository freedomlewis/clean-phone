package com.lewis.clear


import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import java.util.ArrayList

class ClearService : AccessibilityService() {

    private var isAppDetail: Boolean = false

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null == event || null == event.source) {
            return
        }
        val installAppName = event.source.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text1")
        for (nodeInfo in installAppName) {
            val charSequence = nodeInfo.text ?: continue
            val title = charSequence.toString()
            if (clearedAppNames.contains(title)) {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                Log.i(TAG, "cleared app name: $title")
                return
            }

            clearedAppNames.add(title)
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName == SETTING_PACKAGE_NAME) {
            val className = event.className
            if (className == APP_INSTALL_DETAILS) {
                simulationClick(event, TEXT_FORCE_STOP)
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                isAppDetail = true
            }
            if (isAppDetail && className == UNINSTALL_ALERT_DIALOG) {
                simulationClick(event, TEXT_DETERMINE)
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                isAppDetail = false
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun simulationClick(event: AccessibilityEvent, text: String) {
        val nodeInfoList = event.source.findAccessibilityNodeInfosByText(text)
        for (node in nodeInfoList) {
            if (node.isClickable && node.isEnabled) {
                Log.i(TAG, "click text: " + text + " class name: " + node.className)
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    override fun onInterrupt() {}

    companion object {
        val TAG = ClearService::class.java.simpleName
        var clearedAppNames: MutableList<String> = ArrayList()

        private val TEXT_FORCE_STOP = "强行停止"
        private val TEXT_DETERMINE = "确定"
        private val SETTING_PACKAGE_NAME = "com.android.settings"
        private val APP_INSTALL_DETAILS = "com.android.settings.applications.InstalledAppDetailsTop"
        private val UNINSTALL_ALERT_DIALOG = "android.app.AlertDialog"
    }
}
