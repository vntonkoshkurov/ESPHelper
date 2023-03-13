package Transformer

class Transformer (_powerKVT : Float, _voltageInput : Float, _impedance : Float) {
    var powerKVT : Float = 0f
       set(value) {
           if (value > 0) {
               field = value
           }
       }
        get() = field
    var voltageInput : Float = 0f
        set(value) {
            if (value > 0) {
                field = value
            }
        }
        get() = field
    var impedance : Float = 0f
        set(value) {
            if (value > 0) {
                field = value
            }
        }
        get() = field

    init {
        if (_powerKVT > 0) powerKVT = _powerKVT
        if (_voltageInput > 0) voltageInput = _voltageInput
        if (_impedance > 0 ) impedance = _impedance
    }

    fun tapRecommendedValue (motorVoltage: Float, motorFullPower: Float, cableVoltageDrop: Float, voltageBaseOutput: Float) : Float {
        var transformerTap : Float

        transformerTap = (motorVoltage + cableVoltageDrop + (motorFullPower / powerKVT)
                * motorVoltage * (impedance / 100f)) * (voltageInput / voltageBaseOutput)
        return transformerTap
    }

    fun powerKVARecommendedValue (transformerTap: Float, motorCurrent: Float, transformerReserveValue: Float) : Float {
        var powerRec : Float

        powerRec = ((transformerTap * motorCurrent * 1.73f) / 1000f) * (100f + transformerReserveValue) / 100

        return powerRec
    }
}