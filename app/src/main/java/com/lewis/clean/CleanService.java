package com.lewis.clean;


import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class CleanService extends Service {
    private static final String TAG = CleanService.class.getSimpleName();
    private ActivityManager activityManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startClean();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startClean() {
        final PackageManager pm = this.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_META_DATA);
        for (int i = 0; i < apps.size(); i++) {
            PackageInfo packageInfo = apps.get(i);
            killBackageProcess(packageInfo);
        }
        Toast.makeText(this, "All Cleaned", Toast.LENGTH_SHORT).show();
    }

    private void killBackageProcess(PackageInfo packageInfo) {
        if (activityManager == null) {
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        }
        String packageName = packageInfo.applicationInfo.packageName;
        if (packageInfo.versionName != null
                && ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
                && !packageName.equals(this.getPackageName())
                && !packageName.contains("android")) {
            activityManager.killBackgroundProcesses(packageName);
            Log.i(TAG, "kill package name: " + packageName);
        }
    }
}
