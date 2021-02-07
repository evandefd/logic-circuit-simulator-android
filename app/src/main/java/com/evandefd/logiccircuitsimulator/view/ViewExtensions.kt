package com.evandefd.logiccircuitsimulator.view

import android.content.Context
import android.view.View

fun Context.millimeter2pxWithXdpi(millimeter: Float): Float {
    val dpi = resources.displayMetrics.xdpi
    val mm2px = dpi / 25.4f * millimeter
    return mm2px
}

fun Context.millimeter2pxWithYdpi(millimeter: Float): Float {
    val dpi = resources.displayMetrics.ydpi
    val mm2px = dpi / 25.4f * millimeter
    return mm2px
}