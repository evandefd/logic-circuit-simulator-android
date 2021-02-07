package com.evandefd.logiccircuitsimulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evandefd.logiccircuitsimulator.view.checkerboard.LogicCircuitBoard
import com.evandefd.logiccircuitsimulator.view.checkerboard.LogicCircuitBoardScaleView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logicCircuitContainer =
            findViewById<LogicCircuitBoard>(R.id.logic_circuit_container)
        logicCircuitContainer.logicCircuitComponents.add(
            DummyLogicCircuit(this, "TAG1")
        )
        logicCircuitContainer.logicCircuitComponents.add(
            DummyLogicCircuit(this, "TAG2").apply {
                actualPositionX = -40f
            }
        )

        val logicCircuitBoardScaleView = findViewById<LogicCircuitBoardScaleView>(R.id.logic_circuit_scale_view)

        logicCircuitContainer.logicCircuitBoardScaleView = logicCircuitBoardScaleView
    }
}