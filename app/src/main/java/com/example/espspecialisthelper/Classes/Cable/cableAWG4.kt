package Cable

class CableAWG4(_cableLengthInMeter: Float): Cable() {
    val cableCrossSection: String = "4AWG(21mm)"
    init {
        cableResistivity = 0.475f
        cableLength = _cableLengthInMeter
    }

    override fun cableDropVoltage(current: Float): Float {
        var fullRes: Float = 0f
        fullRes = (cableLength * cableResistivity * current) / 1000
        return fullRes
    }
}