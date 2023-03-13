package Cable

abstract class Cable {
    //значение переменной длины хранится и передается в футах, принимается в метрах
    var cableLength : Float = 0f
        set(value) {
            if (value < 0f) {
                if (value < 0f) {
                    println("Длина кабеля должна быть положительным значением.")
                    field = 0f
                }
            } else {
                field = value * 3.28f
            }
        }
    protected var cableResistivity: Float = 0f

    abstract fun cableDropVoltage (current: Float) : Float
}