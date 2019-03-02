package com.lewis.clear


import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.lewis.clear.events.AppStoppedEvent
import com.lewis.clear.events.CleanServiceStartedEvent
import com.lewis.clear.events.DisableCleanServiceEvent
import com.lewis.clear.events.EnableCleanServiceEvent
import com.lewis.clear.rxbus.RxBus
import io.reactivex.disposables.CompositeDisposable

class ClearService : AccessibilityService() {

    private var handler = Handler()
    private var compositeDisposable = CompositeDisposable()
    private var isDisabled = false
    private var processingEventTime: Long = 0
    private var isStoppable = false

    override fun onCreate() {
        super.onCreate()
        registerEvent()
    }

    private fun registerEvent() {
        compositeDisposable.add(RxBus.register(EnableCleanServiceEvent::class.java).subscribe({
            isDisabled = false
        }, {
            it.printStackTrace()
        }))
        compositeDisposable.add(RxBus.register(DisableCleanServiceEvent::class.java).subscribe({
            isDisabled = true
        }, {
            it.printStackTrace()
        }))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        RxBus.post(CleanServiceStartedEvent())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (isDisabled || null == event || null == event.source) {
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        if (event.packageName != SETTING_PACKAGE_NAME) {
            return
        }
        when (event.className) {
            APP_INSTALL_DETAILS -> {
                val isStoppable = simulationClick(event, TEXT_FORCE_STOP)
                if (!isStoppable) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    handler.postDelayed({
                        RxBus.post(AppStoppedEvent())
                    }, 1000)
                }
            }

            UNINSTALL_ALERT_DIALOG -> {
                simulationClick(event, TEXT_DETERMINE)
            }
        }
    }

    private fun simulationClick(event: AccessibilityEvent, text: String): Boolean {
        var isStoppable = false
        val nodeInfoList = event.source.findAccessibilityNodeInfosByText(text)
        for (node in nodeInfoList) {
            if (node.isClickable && node.isEnabled) {
                Log.i(TAG, "click text: " + text + " class name: " + node.className)
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                isStoppable = true
            }
        }
        return isStoppable
    }

    override fun onInterrupt() {}

    companion object {
        val TAG = ClearService::class.java.simpleName
        private val TEXT_FORCE_STOP = "强行停止"
        private val TEXT_DETERMINE = "确定"
        private val SETTING_PACKAGE_NAME = "com.android.settings"
        private val APP_INSTALL_DETAILS = "com.android.settings.applications.InstalledAppDetailsTop"
        private val UNINSTALL_ALERT_DIALOG = "android.app.AlertDialog"
    }
}
