package com.lewis.clear

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStatsManager.INTERVAL_BEST
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lewis.clear.events.AppStoppedEvent
import com.lewis.clear.events.CleanServiceStartedEvent
import com.lewis.clear.events.DisableCleanServiceEvent
import com.lewis.clear.events.EnableCleanServiceEvent
import com.lewis.clear.model.AppInfo
import com.lewis.clear.rxbus.RxBus
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class ClearActivity : AppCompatActivity() {
    private var activityManager: ActivityManager? = null
    private lateinit var appInfos: MutableList<AppInfo>
    private var disposable: CompositeDisposable = CompositeDisposable()
    private var processIndex = 0
    private lateinit var accessibilityManager: AccessibilityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkUsageStatsPermission()
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        initRecycleView()
        initCleanListener()
        registerEvent()
    }

    private fun initCleanListener() {
        clearButton.setOnClickListener {
            if (appInfos.isEmpty()) {
                Toast.makeText(this, "No Apps to clean", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isEnabledAccessibilityService()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityForResult(intent, PERMISSIONS_REQUEST_ACCESSIBILITY)
                return@setOnClickListener
            }
            RxBus.post(EnableCleanServiceEvent())

            startCleanApps()
        }
    }

    private fun isEnabledAccessibilityService(): Boolean {
        val accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityServices) {
            if (info.id.contains(ClearService::class.java.simpleName)) {
                return true
            }
        }
        return false
    }

    private fun registerEvent() {
        disposable.add(RxBus.register(AppStoppedEvent::class.java).subscribe(
                {
                    onAppClosed()
                },
                {
                    it.printStackTrace()
                }))
        disposable.add(RxBus.register(CleanServiceStartedEvent::class.java).subscribe({
            startCleanApps()
        }, {
            it.printStackTrace()
        }))
    }

    private fun onAppClosed() {
        processIndex++
        if (processIndex < appInfos.size) {
            showPackageDetail(this, appInfos[processIndex].packageName)
        } else {
            RxBus.post(DisableCleanServiceEvent())
        }
    }

    private fun initRecycleView() {
        appInfos = queryRunningAppInfos()
        rl_running_apps.layoutManager = LinearLayoutManager(this)
        rl_running_apps.adapter = RunningAppsAdapter(this, appInfos)
    }

    private fun checkUsageStatsPermission() {
        if (!hasPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivityForResult(intent, PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
        }
    }

    private fun hasPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        var mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS -> {
                if (!hasPermission()) {
                    Toast.makeText(this, "未能开启查看使用状态的权限", Toast.LENGTH_SHORT).show();
                } else {
                    appInfos.clear()
                    appInfos.addAll(queryRunningAppInfos())
                    rl_running_apps.adapter?.notifyDataSetChanged()
                }
            }

            PERMISSIONS_REQUEST_ACCESSIBILITY -> {
                if (isEnabledAccessibilityService()) {
                    startCleanApps()
                }
            }
        }
    }

    private fun startCleanApps() {
        processIndex = 0
        showPackageDetail(this, appInfos[0].packageName)
    }

    private fun queryRunningAppInfos(): MutableList<AppInfo> {
        val appInfos = ArrayList<AppInfo>()
        val usageStatus = getUsageStatus()
        for (info in usageStatus) {
            val packageName = info.packageName
            if (packageName == this.packageName) {
                continue
            }

            if (!isBlackList(packageName) && isSystemApp(packageName)) {
                continue
            }

            Log.d("Lewis", packageName)

            try {
                val name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
                appInfos.add(AppInfo(name, packageName, packageManager.getApplicationIcon(packageName)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return appInfos
    }

    private fun isBlackList(packageName: String): Boolean {
        return listOf("com.android.chrome", "com.google.android.apps.docs", "com.google.android.gms",
                "com.google.android.gsf", "com.google.android.tts", "com.google.android.apps.tachyon",
                "com.android.vending", "com.android.providers.downloads",
                "net.oneplus.weather")
                .contains(packageName)
    }

    private fun getUsageStatus(): List<UsageStats> {
        getSystemService(Context.USAGE_STATS_SERVICE)?.let {
            it as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val usageStats = it.queryUsageStats(INTERVAL_BEST, endTime - TIME_RANGE, endTime)
            usageStats.sortWith(Comparator { o1, o2 -> (o1.lastTimeUsed - o2.lastTimeUsed).toInt() })
            return usageStats.reversed()
        }
        return ArrayList()
    }


    private fun isSystemApp(packageName: String): Boolean {
        packageName.let {
            try {
                val info = packageManager.getApplicationInfo(it, 0)
                if (info.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                    Log.d("System App", packageName)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1000
        private const val PERMISSIONS_REQUEST_ACCESSIBILITY = 1001
        private const val TIME_RANGE = 10 * 1000

        fun showPackageDetail(context: Context, packageName: String) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

    }
}
