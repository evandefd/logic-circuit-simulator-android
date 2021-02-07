package com.evandefd.logiccircuitsimulator.view.component

import android.content.Context
import android.graphics.drawable.Drawable

abstract class LogicCircuitComponent(open val context: Context, open val tag : String) :
    Connectible() {

    /**
     * The name of this circuit (e.g. Resistance, Conductor, ...)
     */
    abstract val name: String

    /**
     * The actual width of this circuit, unit is mm(millimeter)
     */
    abstract val actualWidth: Float

    /**
     * The actual height of this circuit, unit is mm(millimeter)
     */
    abstract val actualHeight: Float

    /**
     * The image drawable of this circuit
     */
    abstract val image: Drawable

    /**
     * The actual x position of left-top vertex of this circuit.
     * It will collocated in LogicCircuitViewGroup by actual position and actual width.
     * unit is mm(millimeter)
     */
    var actualPositionX = 0f

    /**
     * The actual y position of left-top vertex of this circuit.
     * It will collocated in LogicCircuitViewGroup by actual position and actual height.
     * unit is mm(millimeter)
     */
    var actualPositionY = 0f
}