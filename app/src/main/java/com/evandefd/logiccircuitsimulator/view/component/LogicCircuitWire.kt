package com.evandefd.logiccircuitsimulator.view.component

import android.graphics.*

open class LogicCircuitWire(
    startNickname: String = "wire_start",
    endNickname: String = "wire_end"
) : Connectible() {
    override val contacts = listOf(
        Contact(0, startNickname, Contact.GENDER_UNSPECIFIED, Float.NaN, Float.NaN),
        Contact(1, endNickname, Contact.GENDER_UNSPECIFIED, Float.NaN, Float.NaN)
    )

    fun makeBitmap(ltrb : Array<Float>) : Bitmap? {
        if(contacts[0].contact == null || contacts[1].contact == null) {
            return null
        } else {
            val x1 = contacts[0].contact!!.actualPositionX
            val x2 = contacts[1].contact!!.actualPositionX
            val y1 = contacts[0].contact!!.actualPositionY
            val y2 = contacts[1].contact!!.actualPositionY

            if(x1 > x2) {
                ltrb[0] = x2
                ltrb[2] = x1
            } else {
                ltrb[0] = x1
                ltrb[2] = x2
            }

            if(y1 > y2) {
                ltrb[1] = y2
                ltrb[3] = y1
            } else {
                ltrb[1] = y1
                ltrb[3] = y2
            }

            val bitmap = Bitmap.createBitmap((ltrb[2] - ltrb[0]).toInt(), (ltrb[3] - ltrb[1]).toInt(), Bitmap.Config.ARGB_8888)
            

            return bitmap
        }
    }
}