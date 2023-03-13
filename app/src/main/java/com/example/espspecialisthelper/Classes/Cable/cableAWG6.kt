package Cable

class CableAWG6(_cableLengthInMeter: Float): Cable() {
    val cableCrossSection: String = "6AWG(13mm)"
    init {
        cableResistivity = 0.75f
        cableLength = _cableLengthInMeter
    }

    override fun cableDropVoltage(current: Float): Float {
        var fullRes: Float = 0f
        fullRes = (cableLength * cableResistivity * current) / 1000
        return fullRes
    }
}