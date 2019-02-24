package com.lewis.clear

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ClearActivity : AppCompatActivity() {
    private var activityManager: ActivityManager? = null
    private var accessibilityManager: AccessibilityManager? = null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    override fun onResume() {
        super.onResume()
        ClearService.clearedAppNames.clear()
        clearBackagroundProcess()
        clearRunningApps()
    }

    fun clearBackagroundProcess() {
        val pm = this.packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        for (i in apps.indices) {
            val packageInfo = apps[i]
            killBackgroundProcess(packageInfo)
        }
    }

    private fun killBackgroundProcess(packageInfo: PackageInfo) {
        if (activityManager == null) {
            activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }
        val packageName = packageInfo.applicationInfo.packageName
        if (packageInfo.versionName != null
                && packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1
                && packageName != this.packageName
                && !packageName.contains("android")) {
            activityManager!!.killBackgroundProcesses(packageName)
            Log.i(TAG, "kill package name: $packageName")
        }
    }

    private fun clearRunningApps() {
        if (!checkEnabledAccessibilityService()) {
            return
        }

        val runningAppsPackageName = queryRunningAppsPackageName()
        var clearedApp = 0
        for (packageName in runningAppsPackageName) {
            clearedApp++
            showPackageDetail(packageName, clearedApp)
            Log.i(TAG, "kill package: $packageName")
        }
        handler.postDelayed({
            Toast.makeText(this@ClearActivity, "All cleared", Toast.LENGTH_SHORT).show()
            this@ClearActivity.finish()
        }, (2 * 1000).toLong())
    }

    private fun checkEnabledAccessibilityService(): Boolean {
        val accessibilityServices = accessibilityManager!!.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityServices) {
            if (info.id == SERVICE_NAME) {
                return true
            }
        }
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        return false
    }

    private fun showPackageDetail(packageName: String, clearedApp: Int) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun queryRunningAppsPackageName(): List<String> {
        val packageList = ArrayList<String>()
        val appProcesses = activityManager!!.runningAppProcesses
        for (info in appProcesses) {
            if (verifyPackageName(info.processName)) continue
            val packageName = getPackageName(info.processName)
            if (!packageList.contains(packageName)) {
                packageList.add(packageName)
            }
        }
        val allServices = activityManager!!.getRunningServices(1000)
        for (info in allServices) {
            if (verifyPackageName(info.process)) continue
            val packageName = getPackageName(info.process)
            if (!packageList.contains(packageName)) {
                packageList.add(packageName)
            }
        }
        return packageList
    }

    private fun getPackageName(processName: String): String {
        return if (processName.contains(":")) processName.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0] else processName
    }

    private fun verifyPackageName(process: String): Boolean {
        for (keyName in excludePackages) {
            if (process.contains(keyName)) {
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = ClearService::class.java.simpleName

        private val excludePackages = arrayOf("com.android", "google", "oneplus", "oppo", "system", "qualcomm", "lewis", //当前应用
                "iflytek", //讯飞输入法
                "eyefilter"//护眼应用
        )
        private val SERVICE_NAME = "com.lewis.clear/.ClearService"
        val CLEAR_GAP = 100
    }
}
