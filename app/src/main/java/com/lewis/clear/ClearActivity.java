package com.lewis.clear;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ClearActivity extends AppCompatActivity {
    private static final String TAG = ClearService.class.getSimpleName();

    private static final String[] excludePackages = {"com.android", "google", "oneplus", "oppo", "system", "qualcomm",
            "lewis",//当前应用
            "iflytek",//讯飞输入法
            "eyefilter"//护眼应用
    };
    private static final String SERVICE_NAME = "com.lewis.clear/.ClearService";
    private ActivityManager activityManager;
    private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearBackgroundApp();
    }

    private void clearBackgroundApp() {
        if (!checkEnabledAccessibilityService()) {
            return;
        }

        List<String> backgroundAppPackageNames = queryBackgroundProcesses();
        for (String packageName : backgroundAppPackageNames) {
            showPackageDetail(packageName);
            Log.i(TAG, "kill package: " + packageName);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ClearActivity.this, "All cleared", Toast.LENGTH_SHORT).show();
                ClearActivity.this.finish();
            }
        }, 2 * 1000);
    }

    private boolean checkEnabledAccessibilityService() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(SERVICE_NAME)) {
                return true;
            }
        }
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        return false;
    }

    private void showPackageDetail(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        startActivity(intent);
    }

    private List<String> queryBackgroundProcesses() {
        List<String> packageList = new ArrayList<>();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : appProcesses) {
            if (verifyPackageName(info.processName)) continue;
            final String packageName = getPackageName(info.processName);
            if (!packageList.contains(packageName)) {
                packageList.add(packageName);
            }
        }
        List<ActivityManager.RunningServiceInfo> allServices = activityManager.getRunningServices(1000);
        for (ActivityManager.RunningServiceInfo info : allServices) {
            if (verifyPackageName(info.process)) continue;
            final String packageName = getPackageName(info.process);
            if (!packageList.contains(packageName)) {
                packageList.add(packageName);
            }
        }
        return packageList;
    }

    private String getPackageName(String processName) {
        return processName.contains(":") ? processName.split(":")[0] : processName;
    }

    private boolean verifyPackageName(String process) {
        for (String keyName : excludePackages) {
            if (process.contains(keyName)) {
                return true;
            }
        }
        return false;
    }
}
