package com.lewis.clear.model


import android.graphics.drawable.Drawable

class AppInfo {
    private var id: Int = 0
    var isFilterProcess: Boolean = false
    var packageName: String? = null
    private var icon: Drawable? = null
    private var name: String? = null
    var isSystemProcess: Boolean = false
    var memorySize: Int = 0

    constructor() {}

    constructor(packageName: String) {
        this.packageName = packageName
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun setIcon(icon: Drawable) {
        this.icon = icon
    }

    fun setName(name: String) {
        this.name = name
    }
}
