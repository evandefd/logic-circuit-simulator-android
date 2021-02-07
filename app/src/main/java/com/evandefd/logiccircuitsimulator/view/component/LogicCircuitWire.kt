package com.evandefd.logiccircuitsimulator.view.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory

open class LogicCircuitWire(
    startNickname: String = "wire_start",
    endNickname: String = "wire_end"
) : Connectible() {
    override val contacts = listOf(
        Contact(0, startNickname, Contact.GENDER_UNSPECIFIED, Float.NaN, Float.NaN),
        Contact(1, endNickname, Contact.GENDER_UNSPECIFIED, Float.NaN, Float.NaN)
    )

    fun makeBitmap() {
        
    }
}