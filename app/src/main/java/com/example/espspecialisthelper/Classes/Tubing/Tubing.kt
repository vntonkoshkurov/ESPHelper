import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Adapters.PickerAdapter
import kotlin.math.pow
import kotlin.collections.MutableMap as MutableMap

class Tubing(val context: Context) {

    private lateinit var prefs: SharedPreferences
    companion object {
        val ID = "id"
        val OD = "od"
    }

    /* тип трубы */
    var tubingType: String? = null
        set(value) = when (value) {
            "60x5.0" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "73x5.5" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "73x7.0" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "89x6.5" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "89x8.0" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "102x6.5" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "114x7.0" -> {
                tubingIDmm = tubingTypesList.getValue(value)[0]
                tubingODmm = tubingTypesList.getValue(value)[1]
                field = value
            }
            "114x7.4" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "127x9.2" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "146x7.7" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "168x8.9" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "178x9.2" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "178x10.4" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            "245x12.0" -> {
                tubingIDmm = casingTypesList.getValue(value)[0]
                tubingODmm = casingTypesList.getValue(value)[1]
                field = value
            }
            else -> {
                field = null
            }
        }

    /* длина трубы, метры */
    var length: Float? = null
        set(value) {
            field = if (value != null && value > 0f) value else null
        }

    /* внутренний и внешний диаметр, хранится в мм */
    var tubingIDmm: Float = 0f
    var tubingODmm: Float = 0f

    /* здесь хранятся некторые типоразмеры труб НКТ
    * внутри параметра хранится массив, где первое значение - внутренний диаметр
    * второй - наружный диаметр */
    /* здесь хранятся некторые типоразмеры труб НКТ */
    val tubingTypesList = mutableMapOf(
        "60x5.0" to arrayOf(50.3f, 60.0f),
        "73x5.5" to arrayOf(62.0f, 73.0f),
        "73x7.0" to arrayOf(59.0f, 73.0f),
        "89x6.5" to arrayOf(75.9f, 89.0f),
        "89x8.0" to arrayOf(72.9f, 89.0f),
        "102x6.5" to arrayOf(88.6f, 102.0f),
        "114x7.0" to arrayOf(100.3f, 114.0f)
    )

    /* здесь хранятся некторые типоразмеры труб ЭК */
    val casingTypesList = mutableMapOf(
        "114x7.4" to arrayOf(99.5f, 114.0f),
        "127x9.2" to arrayOf(108.6f, 127.0f),
        "146x7.7" to arrayOf(130.7f, 146.0f),
        "168x8.9" to arrayOf(150.5f, 168.0f),
        "178x9.2" to arrayOf(159.4f, 178.0f),
        "178x10.4" to arrayOf(157.0f, 178.0f),
        "245x12.0" to arrayOf(220.5f, 245.0f)
    )

    /*здесь будет храниться список холдеров по прокручиваемому списку-селектору RecyclerView */
    var tubingHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf<PickerAdapter.PickerItemViewHolder>()

    /* объем передается в м3, по уполчанию функция использует исходную длину трубы,
    но длину также можно указать самостоятельно */
    fun getVolume(len_m: Float, diam_mm: Float): Float {
        //ниже возвращается значение по классической формуле объема цилиндра v = r2 * PI * l
        return (diam_mm.div(2).pow(2) * Math.PI * len_m.times(1000)).div(1000000000).toFloat()

    }

    /* передача диаметра в метрах */
    fun getDiamInMeter(diam_mm: Float): Float {
        return diam_mm.div(1000f)
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

}