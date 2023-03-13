package com.example.espspecialisthelper.Classes.Values.DownholeValues

import Tubing
import com.example.espspecialisthelper.Classes.Calculations.ContainerForTimeAppearance
import java.math.RoundingMode

class ValueDHv1() : ValueDownhole() {

    //функция расчета уровня жидкости, при изместном давлении на приеме, давлении в затрубе и плотности жидкости
    fun calcFluidLevel(): Float? {
        if (casigPressure_Pa != null && intakePressure_Pa != null &&
            fluidDensity != null && fluidDensity != 0f)
        {
            //ур.жидкости = гл.сп - ((давл.пр - давл.затр)/плотн*g)
            val res = (liftDepth_meter ?: 0f) - ((intakePressure_Pa!! - casigPressure_Pa!!) / (fluidDensity!!.times(9.81f)))
            return if (res > 0f) {
                res
            } else 0f
        } else return null
    }

    //вычисление давления на приеме относительно уровня жидкости и ее плотности
    fun calcIntakePressure(): Float? {
        if (casigPressure_Pa != null && fluidDensity != null && fluidDensity != 0f
            && fluidLevel_meter != null)
        {
            //давл.пр = (гл.сп-ур.жидк) * плотн * g + давл.затр
            val res = ((liftDepth_meter ?: 0f) - fluidLevel_meter!!) * fluidDensity!! * 9.81f + casigPressure_Pa!!
            return if (res > 0f) {
                res
            } else 0f
        } else return null
    }

    /*функция расчета времени подачи жидкости. вычисление идет по объему относительно уровня жидкости в скважине.
    * Вычисляемый объем идет сверху вниз по участкам труб*/
    fun calcFlowAppearanceTime(vararg tubings: Tubing): ContainerForTimeAppearance? {
        /*рассчитываем высоту на которую поднимится столб жидкости при наличии давлении в затрубе
        * При рассчетах предпологается, что в НКТ отсутствует давление газа*/
        return if (casigPressure_Pa != null && fluidDensity != null && fluidDensity != 0f
            && fluidLevel_meter != null && tubings[0].length!= null && pumpFlowRate != null) {

            val h = casigPressure_Pa!!.div(fluidDensity!! * 9.81f)
            val result: ContainerForTimeAppearance = ContainerForTimeAppearance() /*результат возврата, будет содержать
                                                                                    время подачи и объем*/

            /*рассчитываем остаточную длину трубы, которую необходимо заполнить жидкостью*/
            var length_m = fluidLevel_meter!! - h

            /*рассчитываем объем всех труб, которые необходимо заполнить жидкостью*/
            var volume_m3 = 0f
            var totalLength = 0f

            for (tubing in tubings) {
                totalLength += tubing.length ?: 0f
            }

            if (totalLength >= fluidLevel_meter!! && length_m > 0f) {
                for (tubing in tubings) {
                    if ((tubing.length ?: 0f) <= length_m) {
                        volume_m3 += tubing.getVolume(tubing.length ?: 0f, tubing.tubingIDmm)
                        length_m -= tubing.length ?: 0f
                    } else {
                        volume_m3 += tubing.getVolume(length_m, tubing.tubingIDmm)
                        break
                    }
                }

                result.volume_m3 = volume_m3

                /*расчет времени заполнения пустой трубы, или времени подачи*/
                result.time_min = (volume_m3 * 1440f) / (pumpFlowRate!! * (operFreq!! / 50f))
            }

            return result
        } else null
    }

    /*функция вычисляет объемы всех труб передаваемых в неё*/
    fun calcTotalTubingVolume(vararg tubings: Tubing): Float {
        var totalvolume_m3: Float = 0f
        for (tubing in tubings){
            totalvolume_m3 += tubing.getVolume(tubing.length ?: 0f, tubing.tubingIDmm)
        }
        return totalvolume_m3
    }

    /*функция вычисляет объем по длине получаемого значения уровня жидкости в скважине
    * вычисление объема идет снизу вверх по трубам*/
    fun calcVolumefromLeveltoBottomofLift(vararg tubings: Tubing, level: Float, whatDiam: String): Float? {
        if (liftDepth_meter != null) {
            var fluidLength = liftDepth_meter!! - level
            var volume = 0f
            tubings.reverse() //делаем реверс массива, поскольку нужно считать объемы труб снизу вверх

            /*Ниже перебираем все трубы, пока не будет вычислен объем для длины, от уровня, до низа*/
            for (tubing in tubings) {
                if (tubing.length != null) {
                    if (tubing.length!! < fluidLength) {
                        if (whatDiam == Tubing.ID) {
                            volume += tubing.getVolume(tubing.length!!, tubing.tubingIDmm)
                        } else if (whatDiam == Tubing.OD) {
                            volume += tubing.getVolume(tubing.length!!, tubing.tubingODmm)
                        }
                        fluidLength -= tubing.length!!
                    } else {
                        if (whatDiam == Tubing.ID) {
                            volume += tubing.getVolume(fluidLength, tubing.tubingIDmm)
                        } else if (whatDiam == Tubing.OD) {
                            volume += tubing.getVolume(fluidLength, tubing.tubingODmm)
                        }
                        break
                    }
                }
            }
            return volume
        } else return null
    }

    //Функция возвращает округленное значение
    fun valRound(value: Float?): Float? {
        return try {
            value!!.toBigDecimal().setScale(1, RoundingMode.UP).toFloat()
        } catch (e: Exception) {
            null
        }
    }
}
