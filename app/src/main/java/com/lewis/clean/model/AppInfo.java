package com.lewis.clean.model;


import android.graphics.drawable.Drawable;

public class AppInfo {
    private int id;
    private boolean isFilterProcess;
    private String packageName;
    private Drawable icon;
    private String name;
    private boolean isSystemProcess;
    private int memorySize;

    public AppInfo() {
    }

    public AppInfo(String packageName) {
        this.packageName = packageName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIsFilterProcess(boolean isFilterProcess) {
        this.isFilterProcess = isFilterProcess;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsSystemProcess(boolean isSystemProcess) {
        this.isSystemProcess = isSystemProcess;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    public boolean getIsSystemProcess() {
        return isSystemProcess;
    }

    public boolean getIsFilterProcess() {
        return isFilterProcess;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getMemorySize() {
        return memorySize;
    }
}
