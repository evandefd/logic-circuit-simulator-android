package com.evandefd.logiccircuitsimulator

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.evandefd.logiccircuitsimulator.view.component.LogicCircuitComponent

class DummyLogicCircuit(override val context: Context, override val tag: String) : LogicCircuitComponent(context, tag) {
    override val name: String = "Dummy"
    override val actualWidth: Float = 8f
    override val actualHeight: Float = 2f
    override val image: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)!!
    override val contacts: List<Contact> = listOf(
        Contact(1, "In", Contact.GENDER_FEMALE, 0.5f, 0.5f, 1f),
        Contact(2, "Out", Contact.GENDER_FEMALE, 0.5f, 7.5f, 1f)
    )
}