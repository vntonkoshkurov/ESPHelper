package Cable

class CableAWG2(_cableLengthInMeter: Float): Cable() {
    val cableCrossSection: String = "2AWG(33mm)"
    init {
        cableResistivity = 0.3f
        cableLength = _cableLengthInMeter
    }

    override fun cableDropVoltage(current: Float): Float {
        var fullRes: Float = 0f
        fullRes = (cableLength * cableResistivity * current) / 1000
        return fullRes
    }
}