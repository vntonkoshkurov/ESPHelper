package com.example.espspecialisthelper.Classes.Well

import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Adapters.PickerAdapter

class Well(val context: Context) {

    private lateinit var prefs: SharedPreferences

    //Стат. или динам. уровень жидкости в скважине
    var fluidLevel_meter: Float? = null

    //глубина спуска оборудования
    var liftDepth_meter: Float? = null

    //плотность скаженной жидкости
    var densityFluid: Float? = null

    //буферное давление на скважине
    var headPressure: Float? = null
    var headPressure_type: String? = null

    //давление в затрубе
    var casigPressure: Float? = null
    var casingPressure_type: String? = null

    //давление на приеме оборудования
    var intakePressure: Float? = null
    var intakePressure_type: String? = null

    //константа хранения некоторых типов размерностей давеления
    val pressureTypeList = mutableMapOf(
        "MPa" to 0.000001f,
        "kgf/cm2" to 0.0000102f,
        "bar" to 0.00001f,
        "atm" to 0.000009869f,
        "psi" to 0.000145f
    )

    //здесь будут храниться списки холдеров по прокручиваемому списку-селектору рабочей частоты RecyclerView
    var HeadPressureHolderList = mutableListOf<PickerAdapter.PickerItemViewHolder>()
    var CasingPressureHolderList = mutableListOf<PickerAdapter.PickerItemViewHolder>()
    var IntakePressureHolderList = mutableListOf<PickerAdapter.PickerItemViewHolder>()

    /** Ниже создается лямбда функция для заполнения массива объектов списк
    функция используется в качестве коллбека. Передается в адаптер прокручиваемых списков, чтобы заполнять массив
    из холдеров. Это необходимо для чтения выбранных данных в холдерах.
     */
    private var getItemHolder: (
        PickerAdapter.PickerItemViewHolder,
        MutableList<PickerAdapter.PickerItemViewHolder>,
        Int
    ) -> Unit =
        { holder: PickerAdapter.PickerItemViewHolder, //получаем холдер
          holderList: MutableList<PickerAdapter.PickerItemViewHolder>, /* получаем ссылку на массив холдеров данного
                                                                        фрагмента, который передает используемый адаптер */
          position: Int -> //получаем позицию холдера в списке
            if (holderList.size >= position + 1 && holderList.size != 0) { /* этой позицией перезаписывается холдер в массиве,
                                                                            если он существует, или добавляется новый */
                holderList[position] = holder
            } else holderList.add(holder)
        }

    /* функция установки адаптера для переменных класса */
    fun setAdapter(
        recyclerView: RecyclerView,
        dataList: MutableList<*>,
        holderList: MutableList<PickerAdapter.PickerItemViewHolder>,
        settingKey: String
    ): PickerAdapter {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val adapter = this.context?.let {
            PickerAdapter(
                it,
                dataList,
                holderList,
                getItemHolder
            ) { item: Int ->
                /*ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                использование данной функции актуально для списков численностью большей 4-8 шт.
                */
                if (item >= prefs.getInt(settingKey, 0)) {
                    recyclerView.smoothScrollToPosition(
                        prefs.getInt(
                            settingKey,
                            0
                        )
                    )
                } else {
                    recyclerView.smoothScrollToPosition(item)
                }
            }
        }!!
        return adapter
    }

    /* функция для получения хранящейся величины буферного давления в метрах */
    fun getHeadPressure_Pa(): Float? {
        return if (headPressure_type != null && headPressure != null) {
            headPressure!!.div(pressureTypeList[headPressure_type]!!)
        } else null
    }

    /* функция для получения хранящейся величины затрубного давления в метрах */
    fun getCasingPressure_Pa(): Float? {
        return if (casingPressure_type != null && casigPressure != null) {
            casigPressure!!.div(pressureTypeList[casingPressure_type]!!)
        } else {
            null
        }
    }

    /* функция для получения хранящейся величины давление на приеме оборудования в метрах */
    fun getIntakePressure_Pa(): Float? {
        return if (intakePressure != null && intakePressure_type != null) {
            intakePressure!!.div(pressureTypeList[intakePressure_type]!!)
        } else {
            null
        }
    }

    fun getFluidLevel_Pa(): Float? {
        return if (fluidLevel_meter != null) {
            fluidLevel_meter!!.div(0.000102f)
        } else {
            null
        }
    }

}