package com.example.espspecialisthelper.Classes.Values.ElectricalValues

abstract class Value {
    //свойства мотора
    var motorVoltage: Float? = null
    var motorPower: Float? = null
    var motorPowerType: String? = null
    var motorCurrent: Float? = null
    //свойства кабеля
    var cable1CrossSection: Float? = null
    var cable1Length: Float? = null
    var cable2CrossSection: Float? = null
    var cable2Length: Float? = null
    var cable3CrossSection: Float? = null
    var cable3Length: Float? = null
    //свойства СУ
    var stantionFreqBase: Float? = null
    var stantionFreqOper: Float? = null
    var stantionVoltageOut: Float? = null
    //свойства ТМПН
    var transformerPower: Float? = null
    var transformerImpedance: Float? = null
    var transformetVoltageIn: Float? = null
    var transformetTap: Float? = null
    //свойства сети питания
    //var powerSupplyVoltage: Float? = null
    //запасы мощностей НЭО
    var transformerPowerReserve: Float? = null
    var stantionPowerReserve: Float? = null
    //расчетные данные анализа запуска ПЭД
    var startupData: Array<Array<Float>> = Array(100, {Array(8, {0f})})
}