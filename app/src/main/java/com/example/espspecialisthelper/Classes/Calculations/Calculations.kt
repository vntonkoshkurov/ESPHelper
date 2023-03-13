package com.example.espspecialisthelper.Classes.Calculations

import Cable.*
import Motor.Motor
import Stantion.Stantion
import Transformer.Transformer
import com.example.espspecialisthelper.Classes.Values.ElectricalValues.Value
import java.lang.Math.abs

class Calculations(values: Value) {

    private var cable1: Cable? = null
    private var cable2: Cable? = null
    private var cable3: Cable? = null
    private var stantion: Stantion? = null
    private var transformer: Transformer? = null
    private var motor: Motor? = null
    var cableTotalLength: Float? = null
    var cableVoltageDrop: Float? = null
    var motorFullPower: Float? = null
    var voltageOutput: Float? = null
    var actualVoltageOut: Float? = null
    var powerKVATransformerRecommended: Float? = null
    var loSideCurrent: Float? = null
    var currentStantionRecommended: Float? = null
    var currentStantionOutStantion: Float? = null
    var motorVoltage: Float? = null
    var motorPowerHP: Float? = null
    var motorPowerkVT: Float? = null

    init {
        try {
            when (values.cable1CrossSection) {
                13f -> cable1 = values.cable1Length?.let { CableAWG6(it) }
                16f -> cable1 = values.cable1Length?.let { CableAWG5(it) }
                21f -> cable1 = values.cable1Length?.let { CableAWG4(it) }
                33f -> cable1 = values.cable1Length?.let { CableAWG2(it) }
                42f -> cable1 = values.cable1Length?.let { CableAWG1(it) }
                else -> null
            }
        } catch (_: Exception) {}

        try{
            when (values.cable2CrossSection) {
                13f -> cable2 = values.cable2Length?.let { CableAWG6(it) }
                16f -> cable2 = values.cable2Length?.let { CableAWG5(it) }
                21f -> cable2 = values.cable2Length?.let { CableAWG4(it) }
                33f -> cable2 = values.cable2Length?.let { CableAWG2(it) }
                42f -> cable2 = values.cable2Length?.let { CableAWG1(it) }
                else -> null
            }
        } catch (_: Exception) {}

        try{
            when (values.cable3CrossSection) {
                13f -> cable3 = values.cable3Length?.let { CableAWG6(it) }
                16f -> cable3 = values.cable3Length?.let { CableAWG5(it) }
                21f -> cable3 = values.cable3Length?.let { CableAWG4(it) }
                33f -> cable3 = values.cable3Length?.let { CableAWG2(it) }
                42f -> cable3 = values.cable3Length?.let { CableAWG1(it) }
                else -> null
            }
        } catch (_: Exception) {}

        try{
            cableTotalLength = ((cable1?.cableLength ?: 0f) +
                    (cable2?.cableLength ?: 0f) +
                    (cable3?.cableLength ?: 0f)) / 3.28f
        } catch (_: Exception) {}

        try{
            stantion = Stantion(
                0f,
                //values.powerSupplyVoltage!!,
                values.stantionVoltageOut!!,
                values.stantionFreqBase!!
            )
        } catch (_: Exception) {}

        try{
            transformer = Transformer(
                values.transformerPower!!,
                values.transformetVoltageIn!!,
                values.transformerImpedance!!
            )
        } catch (_: Exception) {}

        try{
            motor = Motor(
                values.motorVoltage!!,
                values.motorPower!!,
                values.motorPowerType!!,
                values.motorCurrent!!
            )
        } catch (_: Exception) {}

        try{
            cableVoltageDrop = cable1!!.cableDropVoltage(motor?.motorCurent!!) +
                    (cable2?.cableDropVoltage(motor?.motorCurent!!) ?: 0f) +
                    (cable3?.cableDropVoltage(motor?.motorCurent!!) ?: 0f)
        } catch (_: Exception) {}

        try{
            motorFullPower = motor!!.motorFullPower(stantion!!.frequencyBase)
        } catch (_: Exception) {}

        try{
            voltageOutput = transformer!!.tapRecommendedValue(motor!!.getVoltage(values.stantionFreqBase!!),
                motor!!.motorFullPower(values.stantionFreqBase!!),
            cableVoltageDrop!!,
                stantion!!.voltageOutput)
        } catch (_: Exception) {}

        try{
            powerKVATransformerRecommended = transformer?.powerKVARecommendedValue(voltageOutput!!,
                motor!!.motorCurent,
                values.transformerPowerReserve!!)!!
        } catch (_: Exception) {}

        try{
            loSideCurrent = (voltageOutput!! / transformer!!.voltageInput) * motor!!.motorCurent
        } catch (_: Exception) {}

        try{
            currentStantionRecommended = stantion!!.currentRecommendedValue(voltageOutput!!,
                transformer!!.voltageInput,
                motor!!.motorCurent,
                values.stantionPowerReserve!!)
        } catch (_: Exception) {}

        try{
            currentStantionOutStantion = stantion!!.currentOutValue(voltageOutput!!,
                transformer!!.voltageInput,
                motor!!.motorCurent)
        } catch (_: Exception) {}

        try{
            motorVoltage = motor!!.getVoltage(values.stantionFreqBase!!)
        } catch (_: Exception) {}

        try{
            motorPowerHP = motor!!.getPower(values.stantionFreqBase!!, "HP")
        } catch (_: Exception) {}

        try{
            motorPowerkVT = motor!!.getPower(values.stantionFreqBase!!, "KVT")
        } catch (_: Exception) {}

        try {
            actualVoltageOut  = (((values.transformetTap!! * values.stantionVoltageOut!!) / values.transformetVoltageIn!!) *
            values.stantionFreqOper!!) /
            values.stantionFreqBase!! // вых. напряжение на рабочей частоте
        } catch (_: Exception) {}

        try{
            if (voltageOutput != null && values.transformetTap != null) calculateStartup(values)
        } catch (_: Exception) {}

    }

    fun calculateStartup(values: Value) {
        var str: Int = 0
        var col: Int = 0
        var startupCurrent: Float = 0f
        var persent: Float = 1f
        val freqOperToBase = values.stantionFreqOper!! / values.stantionFreqBase!!

        for (str in 0 .. 99){
            for (col in 0..7) {
                when (col){
                    0 -> {
                        values.startupData[str][col] = startupCurrent //превышение пускокого тока, раз
                        startupCurrent += 0.05f
                    }
                    1 -> values.startupData[str][col] = persent++ //напряжение в % на двигателе предполагаемое
                    2 -> values.startupData[str][col] = ((voltageOutput!! * 50f) / values.stantionFreqBase!! - values.motorVoltage!!) * values.startupData[str][0] //падение напряжения на кабеле и НЭО, Вольт
                    3 -> values.startupData[str][col] = motor!!.getVoltage(values.stantionFreqOper!!) * values.startupData[str][1] / 100f//расчетный остаток напряжения на двигателе, Вольт
                    4 -> {
                        if ((voltageOutput!! * freqOperToBase - (voltageOutput!! * freqOperToBase - actualVoltageOut!!)- values.startupData[str][2]) > 0) {
                            values.startupData[str][col] = voltageOutput!! * freqOperToBase - (voltageOutput!! * freqOperToBase - actualVoltageOut!!)- values.startupData[str][2]
                        } else  values.startupData[str][col] = 0f
                    } //остаток напряжения на двигателе  после падения напряжения на кабеле, Вольт
                    5 -> values.startupData[str][col] = (values.startupData[str][4] * 100f) / motor!!.getVoltage(values.stantionFreqOper!!) //фактически в % приходит на двигатель напряжения
                    6 -> values.startupData[str][col] = values.motorCurrent!! * values.startupData[str][0] //пусковой ток
                    7 -> values.startupData[str][col] = abs(values.startupData[str][1] - values.startupData[str][5]) //разница в предполагаемой величине напряжения на двигателе и фактической в %
                }
            }
        }
    }

}