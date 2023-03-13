package com.example.espspecialisthelper.Classes.Values.DownholeValues

open class ValueDownhole {
    //свойства насоса
    var pumpHead: Float? = null
    var pumpFlowRate: Float? = null
    var pumpPower: Float? = null
    var pumpPowerType: String? = null
    var pumpStageQty: Float? = null
    var operFreq: Float? = null

    //свойства скважины
    var fluidLevel_meter: Float? = null
    var fluidLevel_Pa: Float? = null
    var totalHeadPressure_Pa: Float? = null
    var casigPressure_Pa: Float? = null
    var intakePressure_Pa: Float? = null
    var fluidDensity: Float? = null
    var liftDepth_meter: Float? = null
    var casingLength_meter: Float? = null

    companion object {
        //типы потоков
        val TURBULENT1 = "turbulent1"
        val TURBULENT2 = "turbulent2"
        val TURBULENT3 = "turbulent3"
        val LAMINAR = "laminar"
        val TRANSITIONAL = "transitional"
    }

    fun getStatusReady(): Boolean {
        if (pumpHead != null && pumpFlowRate != null &&
            pumpStageQty != null && operFreq != null && fluidLevel_meter != null &&
            totalHeadPressure_Pa != null
        ) return true
        return false
    }
}