package Motor

class Motor (_voltage50Hz: Float, _power50Hz: Float, _powerType50Hz: String, _curent: Float){
    private var voltage50Hz: Float = 0f
    private var powerHP50Hz: Float = 0f
    private var powerKVT50Hz: Float = 0f
    var motorCurent: Float = 0f
    private val ratingFreq: Float = 50f
    private var powerType = _powerType50Hz

    init {
        if (_powerType50Hz == "HP" && _power50Hz > 0) {
            powerHP50Hz = _power50Hz
            powerKVT50Hz = _power50Hz * 0.74f
        } else if (_powerType50Hz == "KVT" && _power50Hz > 0) {
            powerHP50Hz = _power50Hz / 0.74f
            powerKVT50Hz = _power50Hz
        }
        if (_voltage50Hz > 0) voltage50Hz = _voltage50Hz
        if (_curent > 0) motorCurent = _curent
    }

    fun getVoltage (operFreq: Float) : Float {
        return (voltage50Hz * operFreq) / ratingFreq
    }

    fun getPower (operFreq: Float, powerType: String) : Float {
        if (powerType == "HP") {
            return (powerHP50Hz * operFreq) / ratingFreq
        } else if (powerType == "KVT") {
            return (powerKVT50Hz * operFreq) / ratingFreq
        }
        return 0f
    }

    fun motorFullPower (frequencyBase: Float) : Float {
        var motorFullPower : Float
        motorFullPower =  (getVoltage(frequencyBase) * motorCurent * 1.73f) / 1000f
        return  motorFullPower
    }

}