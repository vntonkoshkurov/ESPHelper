package com.example.espspecialisthelper.Classes.Pump


import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Adapters.PickerAdapter
import com.example.espspecialisthelper.R
import kotlin.math.pow

class Pump(val context: Context) {

    val HP = "hp"
    val KVT = "kvt"
    var q_m3_50Hz: Float? = null
    var head_m_50Hz: Float? = null
    var power_50Hz: Float? = null
    var power_type: String? = null
        set(value) {
            if (value == context.applicationContext.getString(R.string.power_type_HP)) field = HP
            if (value == context.applicationContext.getString(R.string.power_type_kvt)) field =
                KVT
        }
    var stages: Float? = null
    private lateinit var prefs: SharedPreferences

    //здесь будут храниться списки холдеров по прокручиваемому списку-селектору рабочей частоты RecyclerView
    var frequencyHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    var powerTypeHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()

    //массив рабочих частот
    var freqList: MutableList<Int> = mutableListOf()

    //массив типов мощности
    var powerTypeList: MutableList<String> = mutableListOf()

    init {
        for (i in 35..70) {
            freqList.add(i) //создаются массивы с диапазонами частот, на основании которых будет строится RecyclerView
        }
        powerTypeList =
            mutableListOf(
                context.applicationContext.getString(R.string.power_type_HP),
                context.applicationContext.getString(R.string.power_type_kvt)
            )
    }

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
          holderList: MutableList<PickerAdapter.PickerItemViewHolder>, //получаем ссылку на массив холдеров данного фрагмента, который передает используемый адаптер
          position: Int -> //получаем позицию холдера в списке
            if (holderList.size >= position + 1 && holderList.size != 0) { //этой позицией перезаписывается холдер в массиве, если он существует, или добавляется новый
                holderList[position] = holder
            } else holderList.add(holder)
        }

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
                //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                //использование данной функции актуально для списков численностью большей 4-8 шт.
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

    //Ниже определены функции из законов подобия
    //зависимость дебита прямопропорциональная
    fun get_q_m3_operFreq(operFreq: Float?): Float? {
        return with(operFreq) {
            if (this != null && this > 0f) {
                q_m3_50Hz?.times(this.div(50))
            } else null
        }
    }

    //зависимость напора квадратичная
    fun get_head_m_operFreq(operFreq: Float?): Float? {
        return with(operFreq) {
            if (this != null && this >= 0f) {
                head_m_50Hz?.times(this.div(50).pow(2))
            } else null
        }
    }

    //зависимость мощности кубическая
    fun get_power_operFreq(operFreq: Float?): Float? {
        return with(operFreq) {
            if (this != null && this > 0f) {
                power_50Hz?.times(this.div(50).pow(3))
            } else null
        }
    }
}