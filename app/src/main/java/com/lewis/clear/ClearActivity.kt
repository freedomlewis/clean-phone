package com.lewis.clear

import android.app.ActivityManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStatsManager.INTERVAL_BEST
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lewis.clear.model.AppInfo
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class ClearActivity : AppCompatActivity() {
    private var activityManager: ActivityManager? = null
    private var usageStatsManager: UsageStatsManager? = null
    private lateinit var appInfos: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkUsageStatsPermission()
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        initRecycleView()
    }

    private fun initRecycleView() {
        appInfos = queryRunningAppInfos()
        rl_running_apps.layoutManager = LinearLayoutManager(this)
        rl_running_apps.adapter = RunningAppsAdapter(this, appInfos)
    }

    private fun checkUsageStatsPermission() {
        if (!hasPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
        }
    }

    private fun hasPermission(): Boolean {
        try {
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE)?.let { it as UsageStatsManager }
            if (usageStatsManager != null) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (!hasPermission()) {
                Toast.makeText(this, "未能开启查看使用状态的权限", Toast.LENGTH_SHORT).show();
            } else {
                appInfos = queryRunningAppInfos()
                rl_running_apps.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun queryRunningAppInfos(): ArrayList<AppInfo> {
        val appInfos = ArrayList<AppInfo>()
        val usageStatus = getUsageStatus()
        for (info in usageStatus) {
            val packageName = info.packageName
            if (packageName == this.packageName) {
                continue
            }

            if (isSystemApp(packageName)) {
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

    private fun getUsageStatus(): List<UsageStats> {
        usageStatsManager?.let {
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
                if (it.contains(".oneplus.") || info.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1000
        private const val TIME_RANGE = 10 * 1000
    }
}
