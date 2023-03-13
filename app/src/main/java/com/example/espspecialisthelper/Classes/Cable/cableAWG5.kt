package Cable

class CableAWG5(_cableLengthInMeter: Float): Cable() {
    val cableCrossSection: String = "5AWG(16mm)"
    init {
        cableResistivity = 0.62f
        cableLength = _cableLengthInMeter
    }

    override fun cableDropVoltage(current: Float): Float {
        var fullRes: Float = 0f
        fullRes = (cableLength * cableResistivity * current) / 1000
        return fullRes
    }
}