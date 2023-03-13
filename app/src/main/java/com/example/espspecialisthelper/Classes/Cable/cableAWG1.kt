package Cable

class CableAWG1(_cableLengthInMeter: Float): Cable() {
    val cableCrossSection: String = "1AWG(42mm)"
    init {
        cableResistivity = 0.2272f
        cableLength = _cableLengthInMeter
    }

    override fun cableDropVoltage(current: Float): Float {
        var fullRes: Float = 0f
        fullRes = (cableLength * cableResistivity * current) / 1000
        return fullRes
    }
}

