import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDownhole
import kotlin.math.pow

class TFL_Calc {

    data class Container(var value: Float? = null, var type: String? = null)

    companion object {
        //расчет потерь на трение
        const val water_kinVisc10_7_50C = 5.55f //кинематическая вязкость воды при 50С
        const val oil_kinVisc10_7_50C = 20f //средняя кинематическая вязкость нефти при 50С

        //функция расчета потерь на трения в трубопроводе
        fun tflInMeter(
            flowRateM3Day: Float,
            tubingIDmeter: Float,
            tubingLengthM: Float,
            kinVisc10_7_50C: Float
        ): Container {
            val fluidSpeedMS = fluidSpeed(flowRateM3Day, tubingIDmeter)
            val reinolds = reinolds(fluidSpeedMS, tubingIDmeter, kinVisc10_7_50C)
            val coefFriction: Container = coefFriction(reinolds, tubingIDmeter)

            return Container(
                with(coefFriction.value) {
                    if (this != null) {
                        this * tubingLengthM * fluidSpeedMS.pow(2) /
                                (2f * tubingIDmeter * 9.8f)
                    } else null
                }, if (tubingLengthM == 0f) null else coefFriction.type
            )
        }

        //расчет скорости жидкости в трубе
        private fun fluidSpeed(flowRateM3Day: Float, tubingIDmeter: Float): Float {
            return flowRateM3Day.div(21600f).div((tubingIDmeter.pow(2)).times(Math.PI.toFloat()))
        }

        //расчет числа Реинолдса
        private fun reinolds(
            fluidSpeedMS: Float,
            tubingIDmeter: Float,
            kinVisc10_7_50C: Float
        ): Float {
            val v = kinVisc10_7_50C.times(10f.pow(-7))
            return fluidSpeedMS.times(tubingIDmeter).div(v.toFloat())
        }

        //расчет коэффициента гидравлического трения
        private fun coefFriction(reinolds: Float, tubingIDmeter: Float): Container {
            val d = 0.1f //коффициент шероховатости для стальной, новой трубы
            val tubingIDmm = tubingIDmeter.times(1000f)
            if (reinolds < 2300f) {
                //расчет для ламинарного потока
                return Container(64f / reinolds, ValueDownhole.LAMINAR)
            } else if (reinolds > 4000f && reinolds < 10f.times(tubingIDmm.div(d))) {
                //турбулентный поток, 1-я область. Используется формула Блазиуса
                return Container(0.3164f.div(reinolds.pow(0.25f)), ValueDownhole.TURBULENT1)
            } else if (reinolds > 10f.times(tubingIDmm.div(d)) && reinolds < 560f.times(
                    tubingIDmm.div(
                        d
                    )
                )
            ) {
                //турбулентный поток, 2-я область. Используется формула Альтшуля
                return Container(
                    0.11f.times(((d / tubingIDmm).plus(68f / reinolds)).pow(0.25f)),
                    ValueDownhole.TURBULENT2
                )
            } else if (reinolds > 560f.times(tubingIDmm.div(d))) {
                //турбулентный поток 3-я область, Используется формула Алуштуля для шероховатой стенки
                return Container(
                    0.11f.times((d.div(tubingIDmm)).pow(0.25f)),
                    ValueDownhole.TURBULENT3
                )
            } else return Container(type = ValueDownhole.TRANSITIONAL) //не рекомендуемый в расчетах режим
        }
    }
}