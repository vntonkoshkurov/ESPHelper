package Stantion

class Stantion (_voltageInput: Float = 0f, _voltageOutput: Float, _frequencyBase: Float) {
    var voltageInput : Float = 0f
        set(value) {
            if (value > 0) {
                field = value
            }
        }
        get() = field
    var voltageOutput : Float = 0f
        set(value) {
            if (value > 0) {
                field = value
            }
        }
        get() = field
    var frequencyBase : Float = 0f
        set(value) {
            if (value > 0) {
                field = value
            }
        }
        get() = field

    init {
        if (_voltageInput > 0) voltageInput = _voltageInput
        if (_voltageOutput > 0) voltageOutput = _voltageOutput
        if (_frequencyBase > 0) frequencyBase = _frequencyBase
    }

    fun currentRecommendedValue (transformerTap: Float, transformetVoltageInput: Float, motorCurrent: Float, stantionReserveVal: Float) : Float {
        var powerRec : Float

        powerRec = ((transformerTap / transformetVoltageInput) * motorCurrent) * (100f + stantionReserveVal) / 100

        return powerRec
    }

    fun currentOutValue (transformerTap: Float, transformetVoltageInput: Float, motorCurrent: Float) : Float {
        var powerRec : Float

        powerRec = ((transformerTap / transformetVoltageInput) * motorCurrent)

        return powerRec
    }
}