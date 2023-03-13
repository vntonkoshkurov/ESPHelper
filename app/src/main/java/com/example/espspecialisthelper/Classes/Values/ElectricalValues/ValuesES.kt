package com.example.espspecialisthelper.Classes.Values.ElectricalValues

import com.example.espspecialisthelper.databinding.FragmentElectricalCalcBinding

class ValuesES(binding: FragmentElectricalCalcBinding): Value() {

    init {
        if (binding.motorVoltage.length() != 0) motorVoltage = binding.motorVoltage.text.toString().toFloat() else null
        if (binding.motorPower.length() != 0) motorPower = binding.motorPower.text.toString().toFloat() else null
        if (binding.motorCurent.length() != 0) motorCurrent = binding.motorCurent.text.toString().toFloat() else null
        if (binding.cableLength.length() != 0) cable1Length = binding.cableLength.text.toString().toFloat() else null
        if (binding.cableLength2.length() != 0) cable2Length = binding.cableLength2.text.toString().toFloat() else null
        if (binding.cableLength3.length() != 0) cable3Length = binding.cableLength3.text.toString().toFloat() else null
        if (binding.stantionOutputVoltage.length() != 0) stantionVoltageOut = binding.stantionOutputVoltage.text.toString().toFloat() else null
        if (binding.motorVoltage.length() != 0) motorVoltage = binding.motorVoltage.text.toString().toFloat() else null
        if (binding.transOutputVoltage.length() != 0) transformerPower = binding.transOutputVoltage.text.toString().toFloat() else null
        if (binding.transImpedans.length() != 0) transformerImpedance = binding.transImpedans.text.toString().toFloat() else null
        if (binding.transPowerReserve.length() != 0) transformerPowerReserve = binding.transPowerReserve.text.toString().toFloat() else null
        if (binding.stantionPowerReserve.length() != 0) stantionPowerReserve = binding.stantionPowerReserve.text.toString().toFloat() else null
    }

}
