package com.example.espspecialisthelper.ui.volume

import Tubing
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Classes.Calculations.Container
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDHv1
import com.example.espspecialisthelper.Classes.Well.Well
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentVolumeTubingsBinding

class VolumeTubings : Fragment() {
    private lateinit var binding: FragmentVolumeTubingsBinding
    private var wasPause = false //используется для исключения двойной инициализации адаптеров
    private lateinit var valueDHv1: ValueDHv1
    private lateinit var prefs: SharedPreferences
    private lateinit var uiFunc: UIFunctions
    private lateinit var tubing1: Tubing
    private lateinit var tubing2: Tubing
    private lateinit var tubing3: Tubing
    private lateinit var casing1: Tubing
    private lateinit var casing2: Tubing
    private lateinit var casing3: Tubing
    private lateinit var well: Well
    private lateinit var container1: Container //контейнер для трубы 1
    private lateinit var container2: Container //контейнер для трубы 2
    private lateinit var container3: Container //контейнер для трубы 3
    private lateinit var container4: Container //контейнер для ОК 1
    private lateinit var container5: Container //контейнер для ОК 2
    private lateinit var container6: Container //контейнер для ОК 3
    private lateinit var container7: Container //контейнер селектора давления в затрубе
    private lateinit var container8: Container //контейнер селектора давления на приеме

    //функция для ожидания инициализации всех адаптеров
    private fun waitAdaptersInit() {
        if ((container1.adapter?.isInit == true
                    && container2.adapter?.isInit == true
                    && container3.adapter?.isInit == true
                    && container4.adapter?.isInit == true
                    && container5.adapter?.isInit == true
                    && container6.adapter?.isInit == true
                    && container7.adapter?.isInit == true
                    && container8.adapter?.isInit == true) && !uiFunc.statusOfAdapters
        ) {
            getCalculations()
            uiFunc.statusOfAdapters = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!wasPause) {
            binding = FragmentVolumeTubingsBinding.inflate(inflater)
            uiFunc = UIFunctions(requireContext())
            valueDHv1 = ValueDHv1()
            uiFunc.currentThemeTextColor = binding.tubingTitle.currentTextColor
            adaptersInit() //инициализация адаптеров для прокручиваемых списков

            //БЛОК ОБРАБОТКИ ПРОКРУЧИВАЕМЫХ ПОЛЕЙ ФРАГМЕНТА

            //обрабатываем прокручиваемый список типа трубы НКТ №1
            binding.tubing1Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.tubing1Type.layoutManager as LinearLayoutManager,
                        tubing1.tubingTypesList.keys.toList().toMutableList(),
                        tubing1.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing1Type", 0) && !uiFunc.statusOfAdapters) {
                        container1.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing1.tubingType = tubing1.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы НКТ №2
            binding.tubing2Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.tubing2Type.layoutManager as LinearLayoutManager,
                        tubing2.tubingTypesList.keys.toList().toMutableList(),
                        tubing2.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing2Type", 0) && !uiFunc.statusOfAdapters) {
                        container2.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing2.tubingType = tubing2.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы НКТ №3
            binding.tubing3Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.tubing3Type.layoutManager as LinearLayoutManager,
                        tubing3.tubingTypesList.keys.toList().toMutableList(),
                        tubing3.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing3Type", 0) && !uiFunc.statusOfAdapters) {
                        container3.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing3.tubingType = tubing3.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы ЭК №1
            binding.casing1Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.casing1Type.layoutManager as LinearLayoutManager,
                        casing1.casingTypesList.keys.toList().toMutableList(),
                        casing1.tubingHolderList
                    )
                    if (middle == prefs.getInt("casing1Type", 0) && !uiFunc.statusOfAdapters) {
                        container4.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    casing1.tubingType = casing1.casingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы ЭК №2
            binding.casing2Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.casing2Type.layoutManager as LinearLayoutManager,
                        casing2.casingTypesList.keys.toList().toMutableList(),
                        casing2.tubingHolderList
                    )
                    if (middle == prefs.getInt("casing2Type", 0) && !uiFunc.statusOfAdapters) {
                        container5.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    casing2.tubingType = casing2.casingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы ЭК №3
            binding.casing3Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.casing3Type.layoutManager as LinearLayoutManager,
                        casing3.casingTypesList.keys.toList().toMutableList(),
                        casing3.tubingHolderList
                    )
                    if (middle == prefs.getInt("casing3Type", 0) && !uiFunc.statusOfAdapters) {
                        container6.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    casing3.tubingType = casing3.casingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа затрубного давления
            binding.wellCasingPressureType.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.wellCasingPressureType.layoutManager as LinearLayoutManager,
                        well.pressureTypeList.keys.toList().toMutableList(),
                        well.CasingPressureHolderList
                    )
                    if (middle == prefs.getInt("wellCasingPressureType", 0) && !uiFunc.statusOfAdapters
                    ) {
                        container7.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    well.casingPressure_type = well.pressureTypeList.keys.toMutableList()[middle]
                    valueDHv1.casigPressure_Pa = well.getCasingPressure_Pa()
                    getCalculations()
                    //при изменении значения автоматически пересчитываем значение уровня жидкости в скважине
                    valueDHv1.calcFluidLevel()?.let {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(it).toString()
                        )
                    }
                }
            })

            //обрабатываем прокручиваемый список типа давления на приеме
            binding.wellIntakePressureType.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = uiFunc.scrollHelper(
                        binding.wellIntakePressureType.layoutManager as LinearLayoutManager,
                        well.pressureTypeList.keys.toList().toMutableList(),
                        well.IntakePressureHolderList
                    )
                    if (middle == prefs.getInt("wellIntakePressureType", 0) && !uiFunc.statusOfAdapters
                    ) {
                        container8.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    well.intakePressure_type = well.pressureTypeList.keys.toMutableList()[middle]
                    valueDHv1.intakePressure_Pa = well.getIntakePressure_Pa()
                    getCalculations()
                    //при изменении значения автоматически пересчитываем значение уровня жидкости в скважине
                    valueDHv1.calcFluidLevel()?.let {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(it).toString()
                        )
                    }
                }
            })

            //БЛОК ОБРАБОТКИ ТЕКСТОВЫХ ПОЛЕЙ ФРАГМЕНТА

            //обработка ввода данных в поле дебита насоса
            //обработка ввода данных в поле трубы НКТ №1
            binding.tubing1Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.tubing1Length
                ) { value: Float? ->
                    tubing1.length = value
                    binding.tubing2Length.isEnabled = value != null //если поле пустое, то второе не может быть заполнено и наоборот
                    if (value == null) {
                        binding.tubing3Length.isEnabled = false
                    } else if (binding.tubing2Length.length() != 0) binding.tubing3Length.isEnabled = value != null
                    valueDHv1.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing1Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.tubing1Length.setText(prefs.getString("tubing1Length", ""))
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.tubing1Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.tubing1Length, hasFocus, false)
                //если после потери фокуса поле будет пустое, то должен быть очищен следующий участок
                if (!hasFocus) {
                    if (binding.tubing1Length.length() == 0) {
                        binding.tubing2Length.text = null
                        binding.tubing3Length.text = null
                    }
                }
            }

            //обработка ввода данных в поле трубы НКТ №2
            binding.tubing2Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.tubing2Length
                ) { value: Float? ->
                    tubing2.length = value
                    binding.tubing3Length.isEnabled = value != null //если поле пустое, то третье не может быть заполнено и наоборот
                    valueDHv1.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing2Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }

                }
            )
            binding.tubing2Length.setText(prefs.getString("tubing2Length", ""))
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.tubing2Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.tubing2Length, hasFocus, false)
                //если после потери фокуса поле будет пустое, то должен быть очищен следующий участок
                if (!hasFocus) {
                    if (binding.tubing2Length.length() == 0) binding.tubing3Length.text = null
                }
            }

            //обработка ввода данных в поле трубы НКТ №3
            binding.tubing3Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.tubing3Length
                ) { value: Float? ->
                    tubing3.length = value
                    valueDHv1.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing3Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.tubing3Length.setText(prefs.getString("tubing3Length", ""))
            //обработка потери фокуса полем ввода текста
            binding.tubing3Length.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.tubing3Length, hasFocus, false)
            }

            //обработка ввода данных в поле трубы ЭК №1
            binding.casing1Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.casing1Length
                ) { value: Float? ->
                    /*проверяем, чтобы введенное значение в полях длин ЭК было не больше
                    * длины трубы НКТ, иначе удаляем введенное значение */
                    if (value != null) {
                        /*проверяется разница между общей длинной НКТ и введенными участками ЭК*/
                        if (value > ((valueDHv1.liftDepth_meter ?: 0f) - (casing2.length ?: 0f) - (casing3.length ?: 0f))) {
                            MainActivity.dialogCaller(getString(R.string.alert1) +
                                valueDHv1.valRound(valueDHv1.liftDepth_meter).toString() +
                                " " + getString(R.string.length_measurement_val), requireContext())
                            binding.casing1Length.text = null
                            casing1.length = null
                        } else casing1.length =value
                    } else casing1.length = null
                    /*Ниже, в зависимости от наличия или отсуствия данных будут активироваться остальные участки ЭК
                    * для ввода данных*/
                    binding.casing2Length.isEnabled = binding.casing1Length.length() != 0 //если поле пустое, то второе не может быть заполнено и наоборот
                    if (binding.casing1Length.length() == 0) {
                        binding.casing3Length.isEnabled = false
                    } else if (binding.casing2Length.length() != 0) binding.casing3Length.isEnabled = value != null
                    valueDHv1.casingLength_meter = (casing1.length ?:0f) + (casing2.length ?:0f) + (casing3.length ?:0f)
                    getCalculations()
                }
            )
            binding.casing1Length.setText(prefs.getString("casing1Length", ""))
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.casing1Length.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (binding.casing1Length.length() == 0) {
                        binding.casing2Length.text = null
                        binding.casing3Length.text = null
                    }
                }
            }

            //обработка ввода данных в поле трубы ЭК №2
            binding.casing2Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.casing2Length
                ) { value: Float? ->
                    /*проверяем, чтобы введенное значение в полях длин ЭК было не больше
                    * длины трубы НКТ, иначе удаляем введенное значение */
                    if (value != null) {
                        /*проверяется разница между общей длинной НКТ и введенными участками ЭК*/
                        if (value > ((valueDHv1.liftDepth_meter ?: 0f) - (casing1.length ?: 0f) - (casing3.length ?: 0f))) {
                            MainActivity.dialogCaller(getString(R.string.alert1) +
                                    valueDHv1.valRound(valueDHv1.liftDepth_meter).toString() +
                                    " " + getString(R.string.length_measurement_val), requireContext())
                            binding.casing2Length.text = null
                            casing2.length = null
                        } else casing2.length =value
                    } else casing2.length = null
                    /*Ниже, в зависимости от наличия или отсуствия данных будут активироваться остальные участки ЭК
                    * для ввода данных*/
                    binding.casing3Length.isEnabled = binding.casing2Length.length() != 0 //если поле пустое, то третье не может быть заполнено и наоборот
                    valueDHv1.casingLength_meter = (casing1.length ?:0f) + (casing2.length ?:0f) + (casing3.length ?:0f)
                    getCalculations()
                }
            )
            binding.casing2Length.setText(prefs.getString("casing2Length", ""))
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.casing2Length.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (binding.casing2Length.length() == 0) binding.casing3Length.text = null
                }
            }

            //обработка ввода данных в поле трубы ЭК №3
            binding.casing3Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.casing3Length
                ) { value: Float? ->
                    /*проверяем, чтобы введенное значение в полях длин ЭК было не больше
                    * длины трубы НКТ, иначе удаляем введенное значение */
                    if (value != null) {
                        /*проверяется разница между общей длинной НКТ и введенными участками ЭК*/
                        if (value > ((valueDHv1.liftDepth_meter ?: 0f) - (casing1.length ?: 0f) - (casing2.length ?: 0f))) {
                            MainActivity.dialogCaller(getString(R.string.alert1) +
                                    valueDHv1.valRound(valueDHv1.liftDepth_meter).toString() +
                                    " " + getString(R.string.length_measurement_val), requireContext())
                            binding.casing3Length.text = null
                            casing3.length = null
                        } else casing3.length =value
                    } else casing3.length = null
                    valueDHv1.casingLength_meter = (casing1.length ?:0f) + (casing2.length ?:0f) + (casing3.length ?:0f)
                    getCalculations()
                }
            )
            binding.casing3Length.setText(prefs.getString("casing3Length", ""))
            //обработка ввода данных в поле уровня жидкости
            binding.wellFluidLevel.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.wellFluidLevel
                ) { value: Float? ->
                    well.fluidLevel_meter = value
                    valueDHv1.fluidLevel_meter = well.fluidLevel_meter
                    valueDHv1.fluidLevel_Pa = well.getFluidLevel_Pa()
                    getCalculations()
                    //при наличии плотности жидкости производим расчет давления на приеме оборудования
                    if (binding.wellFluidLevel.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellIntakePressure.setText(
                            valueDHv1.calcIntakePressure()?.let { it1 ->
                                well.intakePressure_type?.let { it2 ->
                                    well.pressureTypeList[it2]?.let { valueDHv1.valRound(it1.times(it)).toString() }
                                }
                            })
                    }
                }
            )
            binding.wellFluidLevel.setText(prefs.getString("fluidLevel", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellFluidLevel.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.wellFluidLevel, hasFocus, true)
            }

            //обработка ввода данных в поле давление в затрубе
            binding.wellCasingPressure.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.wellCasingPressure
                ) { value: Float? ->
                    well.casigPressure = value
                    valueDHv1.casigPressure_Pa = well.getCasingPressure_Pa()
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    valueDHv1.calcFluidLevel()
                        ?.let { binding.wellFluidLevel.setText(valueDHv1.valRound(it).toString()) }

                }
            )
            binding.wellCasingPressure.setText(prefs.getString("casingPressure", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellCasingPressure.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.wellCasingPressure, hasFocus, true)
            }

            //обработка ввода данных в поле давление на приеме
            binding.wellIntakePressure.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.wellIntakePressure
                ) { value: Float? ->
                    well.intakePressure = value
                    valueDHv1.intakePressure_Pa = well.getIntakePressure_Pa()
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в поле плотности жидкости
                    if (binding.wellIntakePressure.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.wellIntakePressure.setText(prefs.getString("intakePressure", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellIntakePressure.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.wellIntakePressure, hasFocus, true)
            }

            //обработка ввода данных в поле плотности жидкости
            binding.wellFluidDensity.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.wellFluidDensity
                ) { value: Float? ->
                    well.densityFluid = value
                    valueDHv1.fluidDensity = well.densityFluid
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в поле плотности жидкости
                    if (binding.wellFluidDensity.hasFocus() && binding.wellIntakePressure.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.wellFluidDensity.setText(prefs.getString("fluidDensity", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellFluidDensity.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.wellFluidDensity, hasFocus, false)
            }


            //обработка нажатия иконки "назад" в заголовке фрагмента
            val toolbar: Toolbar = binding.toolbar
            toolbar.setNavigationOnClickListener {
                MainActivity.vibro(requireContext())
                requireActivity().onBackPressed()
            }

            //обработка нажатий информационных кнопок
            binding.infoSet1.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_2_1), requireContext())
            }

            binding.infoSet2.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2), requireContext())
            }

            binding.infoSet3.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_2_3), requireContext())
            }

            binding.infoSet4.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_2_4), requireContext())
            }

        }
        return binding.root
    }

    private fun adaptersInit (){
        //инициализация объектов фрагмента
        container1 = Container()
        container2 = Container()
        container3 = Container()
        container4 = Container()
        container5 = Container()
        container6 = Container()
        container7 = Container()
        container8 = Container()

        tubing1 = context?.let { Tubing(context = it) }!!
        tubing2 = context?.let { Tubing(context = it) }!!
        tubing3 = context?.let { Tubing(context = it) }!!
        casing1 = context?.let { Tubing(context = it) }!!
        casing2 = context?.let { Tubing(context = it) }!!
        casing3 = context?.let { Tubing(context = it) }!!
        well = context?.let { Well(context = it) }!!

        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ #1
        container1.adapter = tubing1.setAdapter(
            binding.tubing1Type,
            tubing1.tubingTypesList.keys.toList().toMutableList(),
            tubing1.tubingHolderList,
            "tubing1Type"
        )
        binding.tubing1Type.adapter = container1.adapter
        container1.snapHelper.attachToRecyclerView(binding.tubing1Type)
        binding.tubing1Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing1Type.onFlingListener = container1.snapHelper
        binding.tubing1Type.smoothScrollToPosition(prefs.getInt("tubing1Type", 0))
        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ #2
        container2.adapter = tubing2.setAdapter(
            binding.tubing2Type,
            tubing2.tubingTypesList.keys.toList().toMutableList(),
            tubing2.tubingHolderList,
            "tubing2Type"
        )
        binding.tubing2Type.adapter = container2.adapter
        container2.snapHelper.attachToRecyclerView(binding.tubing2Type)
        binding.tubing2Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing2Type.onFlingListener = container2.snapHelper
        binding.tubing2Type.smoothScrollToPosition(prefs.getInt("tubing2Type", 0))
        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ #3
        container3.adapter = tubing3.setAdapter(
            binding.tubing3Type,
            tubing3.tubingTypesList.keys.toList().toMutableList(),
            tubing3.tubingHolderList,
            "tubing3Type"
        )
        binding.tubing3Type.adapter = container3.adapter
        container3.snapHelper.attachToRecyclerView(binding.tubing3Type)
        binding.tubing3Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing3Type.onFlingListener = container3.snapHelper
        binding.tubing3Type.smoothScrollToPosition(prefs.getInt("tubing3Type", 0))
        //инициализация контейнера для прокручиваемого списка типразмеровтрубы ЭК #1
        container4.adapter = casing1.setAdapter(
            binding.casing1Type,
            casing1.casingTypesList.keys.toList().toMutableList(),
            casing1.tubingHolderList,
            "casing1Type"
        )
        binding.casing1Type.adapter = container4.adapter
        container4.snapHelper.attachToRecyclerView(binding.casing1Type)
        binding.casing1Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.casing1Type.onFlingListener = container4.snapHelper
        binding.casing1Type.smoothScrollToPosition(prefs.getInt("casing1Type", 0))
        //инициализация контейнера для прокручиваемого списка типразмеровтрубы ЭК #2
        container5.adapter = casing2.setAdapter(
            binding.casing2Type,
            casing2.casingTypesList.keys.toList().toMutableList(),
            casing2.tubingHolderList,
            "casing2Type"
        )
        binding.casing2Type.adapter = container5.adapter
        container5.snapHelper.attachToRecyclerView(binding.casing2Type)
        binding.casing2Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.casing2Type.onFlingListener = container5.snapHelper
        binding.casing2Type.smoothScrollToPosition(prefs.getInt("casing2Type", 0))
        //инициализация контейнера для прокручиваемого списка типразмеровтрубы ЭК #3
        container6.adapter = casing3.setAdapter(
            binding.casing3Type,
            casing3.casingTypesList.keys.toList().toMutableList(),
            casing3.tubingHolderList,
            "casing3Type"
        )
        binding.casing3Type.adapter = container6.adapter
        container6.snapHelper.attachToRecyclerView(binding.casing3Type)
        binding.casing3Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.casing3Type.onFlingListener = container6.snapHelper
        binding.casing3Type.smoothScrollToPosition(prefs.getInt("casing3Type", 0))
        //инициализация контейнера для прокручиваемого списка типов затрубного давления
        container7.adapter = well.setAdapter(
            binding.wellCasingPressureType,
            well.pressureTypeList.keys.toList().toMutableList(),
            well.CasingPressureHolderList,
            "wellCasingPressureType"
        )
        binding.wellCasingPressureType.adapter = container7.adapter
        container7.snapHelper.attachToRecyclerView(binding.wellCasingPressureType)
        binding.wellCasingPressureType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.wellCasingPressureType.onFlingListener = container7.snapHelper
        binding.wellCasingPressureType.smoothScrollToPosition(
            prefs.getInt(
                "wellCasingPressureType",
                0
            )
        )
        //инициализация контейнера для прокручиваемого списка типов давления на приеме
        container8.adapter = well.setAdapter(
            binding.wellIntakePressureType,
            well.pressureTypeList.keys.toList().toMutableList(),
            well.IntakePressureHolderList,
            "wellIntakePressureType"
        )
        binding.wellIntakePressureType.adapter = container8.adapter
        container8.snapHelper.attachToRecyclerView(binding.wellIntakePressureType)
        binding.wellIntakePressureType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.wellIntakePressureType.onFlingListener = container8.snapHelper
        binding.wellIntakePressureType.smoothScrollToPosition(
            prefs.getInt(
                "wellIntakePressureType",
                0
            )
        )
    }

    override fun onPause() {
        super.onPause()
        wasPause =
            true //фиксируем, что фрагмент уже был активен, чтобы предотвратить повторное перестроение элементов
        //при ухода с фрагмента сохраняем значения всех полей в файл настроек
        val editor = prefs.edit()
        editor.putInt(
            "tubing1Type",
            uiFunc.posInMutableList(binding.tubing1Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "tubing2Type",
            uiFunc.posInMutableList(binding.tubing2Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "tubing3Type",
            uiFunc.posInMutableList(binding.tubing3Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putString("tubing1Length", binding.tubing1Length.text.toString()).apply()
        editor.putString("tubing2Length", binding.tubing2Length.text.toString()).apply()
        editor.putString("tubing3Length", binding.tubing3Length.text.toString()).apply()
        editor.putInt(
            "casing1Type",
            uiFunc.posInMutableList(binding.casing1Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "casing2Type",
            uiFunc.posInMutableList(binding.casing2Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "casing3Type",
            uiFunc.posInMutableList(binding.casing3Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putString("casing1Length", binding.casing1Length.text.toString()).apply()
        editor.putString("casing2Length", binding.casing2Length.text.toString()).apply()
        editor.putString("casing3Length", binding.casing3Length.text.toString()).apply()
        editor.putString("fluidLevel", binding.wellFluidLevel.text.toString()).apply()
        editor.putString("casingPressure", binding.wellCasingPressure.text.toString()).apply()
        editor.putString("intakePressure", binding.wellIntakePressure.text.toString()).apply()
        editor.putString("fluidDensity", binding.wellFluidDensity.text.toString()).apply()
    }

    private fun getCalculations() {
        var casingVol: Float? = null
        var tubingVolOD: Float? = null
        var tubingVolID: Float? = null
        if (uiFunc.statusOfAdapters) {
            //установка значений общей длины труб
            if(tubing1.length != null) {
                //устанавливаем остаток ввода длины ЭК
                /*ниже, в случае уже введенных данных по ЭК и изменении длины НКТ проверяется
                * превышение длины ЭК по отношению к длине НКТ. Если длина превышается, то будут поэтапно удаляться
                * данные из полей ЭК, вплоть до полной очистки всех полей*/
                if (valueDHv1.liftDepth_meter!! < (valueDHv1.casingLength_meter ?:0f)){
                    if (valueDHv1.liftDepth_meter!! >= ((casing1.length ?: 0f) + (casing2.length ?: 0f))){
                        binding.casing3Length.text = null
                    } else if (valueDHv1.liftDepth_meter!! >= (casing1.length ?: 0f)) {
                        binding.casing2Length.text = null
                        binding.casing3Length.text = null
                    } else {
                        binding.casing1Length.text = null
                        binding.casing2Length.text = null
                        binding.casing3Length.text = null
                    }
                }
                /*после проверки по превышению длины ЭК над длиной НКТ устанавливаем рекоммендованую
                для ввода длину ЭК*/
                binding.casingRecLengthVal.text = valueDHv1.valRound(valueDHv1.liftDepth_meter!! - (valueDHv1.casingLength_meter ?: 0f)).toString()
                //значение длины НКТ
                binding.tubingTotalLengthVal.text = valueDHv1.valRound(valueDHv1.liftDepth_meter).toString()
                //значение объема НКТ по внутреннему и внешнему диаметрам
                tubingVolOD = valueDHv1.valRound(tubing1.getVolume(tubing1.length ?:0f, tubing1.tubingODmm) +
                        tubing2.getVolume(tubing2.length ?:0f, tubing2.tubingODmm) +
                        tubing3.getVolume(tubing3.length ?:0f, tubing3.tubingODmm))
                tubingVolID = valueDHv1.valRound(tubing1.getVolume(tubing1.length ?:0f, tubing1.tubingIDmm) +
                        tubing2.getVolume(tubing2.length ?:0f, tubing2.tubingIDmm) +
                        tubing3.getVolume(tubing3.length ?:0f, tubing3.tubingIDmm))
                binding.tubingVolumeVal.text = tubingVolOD.toString()
                binding.tubingVolumeVal2.text = tubingVolID.toString()
            } else  {
                //если первый участок не заполнен, то вставляем пустышку
                binding.tubingTotalLengthVal.text = "-"
                binding.tubingVolumeVal.text = "-"
                binding.tubingVolumeVal2.text = "-"
                binding.casingRecLengthVal.text = "-"
            }
            //значение объема ЭК
            if (casing1.length != null) {
                casingVol = valueDHv1.valRound(casing1.getVolume(casing1.length ?:0f, casing1.tubingODmm) +
                        casing2.getVolume(casing2.length ?:0f, casing2.tubingODmm) +
                        casing3.getVolume(casing3.length ?:0f, casing3.tubingODmm))
                binding.casingVolumeVal.text = casingVol.toString()
            } else {
                binding.casingVolumeVal.text = "-"
            }

            //устанавливаем разницу в объеме ЭК и НКТ
            if (casingVol != null && tubingVolOD != null && casingVol > tubingVolOD) {
                binding.resudialVolVal.text = (valueDHv1.valRound(casingVol - tubingVolOD)).toString()
            } else {
                binding.resudialVolVal.text = "-"
            }

            //расчет объема жидкости в скважине
            if (well.fluidLevel_meter != null && valueDHv1.liftDepth_meter != null
                && valueDHv1.casingLength_meter != null){
                var fluidVolume: Float? = null
                //расчет производится только при уровне меньшем общей длины труб НКТ
                if (well.fluidLevel_meter!! <= valueDHv1.liftDepth_meter!! &&  valueDHv1.casingLength_meter == valueDHv1.liftDepth_meter) {
                    val tubingVolID = valueDHv1.calcVolumefromLeveltoBottomofLift(tubing1, tubing2, tubing3, level = well.fluidLevel_meter!!, whatDiam = Tubing.ID)
                    val tubingVolOD = valueDHv1.calcVolumefromLeveltoBottomofLift(tubing1, tubing2, tubing3, level = well.fluidLevel_meter!!, whatDiam = Tubing.OD)
                    val casingVolID = valueDHv1.calcVolumefromLeveltoBottomofLift(casing1, casing2, casing3, level = well.fluidLevel_meter!!, whatDiam = Tubing.ID)
                    fluidVolume = (casingVolID ?: 0f) - (tubingVolOD ?: 0f) + (tubingVolID ?: 0f)
                }
                if (fluidVolume != null) {
                    binding.fluidVolVal.text = valueDHv1.valRound(fluidVolume).toString()
                } else binding.fluidVolVal.text = "-"
            } else binding.fluidVolVal.text = "-"

            //проверка разницы уровня жидкости и длины трубы. Если длина превышет, то отобразится соответствующся строка
            if (well.fluidLevel_meter != null && valueDHv1.liftDepth_meter != null){
                if (well.fluidLevel_meter!! > valueDHv1.liftDepth_meter!!) {
                    binding.levelAlertTitle.visibility = View.VISIBLE
                } else binding.levelAlertTitle.visibility = View.GONE
            }
        }
    }
}