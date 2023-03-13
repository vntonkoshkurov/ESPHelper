package com.example.espspecialisthelper.ui.downholeeqpt

import TFL_Calc
import Tubing
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
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
import com.example.espspecialisthelper.Classes.Pump.Pump
import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDHv1
import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDownhole
import com.example.espspecialisthelper.Classes.Well.Well
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentDownholeEqptCalcBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.math.RoundingMode


class DownholeEqptCalc() : Fragment() {

    private lateinit var binding: FragmentDownholeEqptCalcBinding
    private var wasPause = false //используется для исключения двойной инициализации адаптеров
    private lateinit var valueDHv1: ValueDHv1
    private lateinit var func: UIFunctions
    private lateinit var prefs: SharedPreferences
    private lateinit var pump: Pump
    private lateinit var well: Well
    private lateinit var tubing1: Tubing
    private lateinit var tubing2: Tubing
    private lateinit var tubing3: Tubing
    private lateinit var container1: Container
    private lateinit var container2: Container
    private lateinit var container3: Container
    private lateinit var container4: Container
    private lateinit var container5: Container
    private lateinit var container6: Container
    private lateinit var container7: Container
    private lateinit var container8: Container

    //функция для ожидания инициализации всех адаптеров
    private fun waitAdaptersInit() {
        if ((container1.adapter?.isInit == true
                    && container2.adapter?.isInit == true
                    && container3.adapter?.isInit == true
                    && container4.adapter?.isInit == true
                    && container5.adapter?.isInit == true
                    && container6.adapter?.isInit == true
                    && container7.adapter?.isInit == true
                    && container8.adapter?.isInit == true) && !func.statusOfAdapters
        ) {
            getCalculations()
            func.statusOfAdapters = true
        }
    }

    //Функция возвращает округленное значение
    private fun valRound(value: Float?): Float? {
        return try {
            value!!.toBigDecimal().setScale(1, RoundingMode.UP).toFloat()
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        //проверяем на наличие построенного view
        if (!wasPause) {
            binding = FragmentDownholeEqptCalcBinding.inflate(inflater)
            func = UIFunctions(requireContext())
            adaptersInit() //инициализация адаптеров для прокручиваемых списков
            valueDHv1 = ValueDHv1()
            func.currentThemeTextColor = binding.tubingTitle.currentTextColor

            //БЛОК ОБРАБОТКИ ПРОКРУЧИВАЕМЫХ ПОЛЕЙ ФРАГМЕНТА

            //обрабатываем прокручиваемый список рабочей частоты насоса
            binding.operFreq.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.operFreq.layoutManager as LinearLayoutManager,
                        pump.freqList,
                        pump.frequencyHolderList
                    )
                    if (middle == prefs.getInt("pumpOperFreq", 0) && !func.statusOfAdapters) {
                        container1.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    valueDHv1.operFreq = pump.freqList[middle].toFloat()
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа мощности насоса
            binding.powerType.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.powerType.layoutManager as LinearLayoutManager,
                        pump.powerTypeList,
                        pump.powerTypeHolderList
                    )
                    if (middle == prefs.getInt("pumpPowerType", 0) && !func.statusOfAdapters) {
                        container2.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    pump.power_type = pump.powerTypeList[middle]
                    valueDHv1.pumpPowerType = pump.power_type
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы №1
            binding.tubing1Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.tubing1Type.layoutManager as LinearLayoutManager,
                        tubing1.tubingTypesList.keys.toList().toMutableList(),
                        tubing1.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing1Type", 0) && !func.statusOfAdapters) {
                        container3.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing1.tubingType = tubing1.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы №2
            binding.tubing2Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.tubing2Type.layoutManager as LinearLayoutManager,
                        tubing2.tubingTypesList.keys.toList().toMutableList(),
                        tubing2.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing2Type", 0) && !func.statusOfAdapters) {
                        container4.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing2.tubingType = tubing2.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа трубы №3
            binding.tubing3Type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.tubing3Type.layoutManager as LinearLayoutManager,
                        tubing3.tubingTypesList.keys.toList().toMutableList(),
                        tubing3.tubingHolderList
                    )
                    if (middle == prefs.getInt("tubing3Type", 0) && !func.statusOfAdapters) {
                        container5.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    tubing3.tubingType = tubing3.tubingTypesList.keys.toMutableList()[middle]
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа буферного давления
            binding.wellHeadPressureType.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.wellHeadPressureType.layoutManager as LinearLayoutManager,
                        well.pressureTypeList.keys.toList().toMutableList(),
                        well.HeadPressureHolderList
                    )
                    if (middle == prefs.getInt("wellHeadPressureType", 0) && !func.statusOfAdapters
                    ) {
                        container6.adapter!!.isInit = true
                        waitAdaptersInit()
                    } //фиксируем окончание инициализации списка
                    //по вычесленному выделенному индексу с прокручиваемом списке определяем
                    //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                    //преобразуем в отдельный список и по индексу выбираем необходимое значение
                    well.headPressure_type = well.pressureTypeList.keys.toMutableList()[middle]
                    valueDHv1.totalHeadPressure_Pa = well.getHeadPressure_Pa()
                    getCalculations()
                }
            })

            //обрабатываем прокручиваемый список типа затрубного давления
            binding.wellCasingPressureType.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.wellCasingPressureType.layoutManager as LinearLayoutManager,
                        well.pressureTypeList.keys.toList().toMutableList(),
                        well.CasingPressureHolderList
                    )
                    if (middle == prefs.getInt("wellCasingPressureType", 0) && !func.statusOfAdapters
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
                            valRound(it).toString()
                        )
                    }
                }
            })

            //обрабатываем прокручиваемый список типа давления на приеме
            binding.wellIntakePressureType.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = func.scrollHelper(
                        binding.wellIntakePressureType.layoutManager as LinearLayoutManager,
                        well.pressureTypeList.keys.toList().toMutableList(),
                        well.IntakePressureHolderList
                    )
                    if (middle == prefs.getInt("wellIntakePressureType", 0) && !func.statusOfAdapters
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
                            valRound(it).toString()
                        )
                    }
                }
            })

            //БЛОК ОБРАБОТКИ ТЕКСТОВЫХ ПОЛЕЙ ФРАГМЕНТА

            //обработка ввода данных в поле дебита насоса
            binding.pumpQ.addTextChangedListener(
                func.checkEditText(
                    binding.pumpQ
                ) { value: Float? ->
                    pump.q_m3_50Hz = value
                    valueDHv1.pumpFlowRate = pump.q_m3_50Hz
                    getCalculations()
                }
            )
            binding.pumpQ.setText(prefs.getString("pumpFlowRate", ""))
            binding.pumpQ.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.pumpQ, hasFocus, false)
            }

            //обработка ввода данных в поле напора насоса
            binding.pumpH.addTextChangedListener(
                func.checkEditText(
                    binding.pumpH
                ) { value: Float? ->
                    pump.head_m_50Hz = value
                    valueDHv1.pumpHead = pump.head_m_50Hz
                    getCalculations()
                }
            )
            binding.pumpH.setText(prefs.getString("pumpHead", ""))
            binding.pumpH.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.pumpH, hasFocus, false)
            }

            //обработка ввода данных в поле количества ступеней насоса
            binding.pumpS.addTextChangedListener(
                func.checkEditText(
                    binding.pumpS
                ) { value: Float? ->
                    pump.stages = value
                    valueDHv1.pumpStageQty = pump.stages
                    getCalculations()
                }
            )
            binding.pumpS.setText(prefs.getString("pumpStagesQTY", ""))
            binding.pumpS.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.pumpS, hasFocus, false)
            }

            //обработка ввода данных в поле мощности насоса
            binding.pumpP.addTextChangedListener(
                func.checkEditText(
                    binding.pumpP
                ) { value: Float? ->
                    pump.power_50Hz = value
                    valueDHv1.pumpPower = pump.power_50Hz
                    getCalculations()
                }
            )
            binding.pumpP.setText(prefs.getString("pumpPowerVal", ""))
            //обработка потери фокуса полем ввода текста
            binding.pumpP.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.pumpP, hasFocus, false)
            }

            //обработка ввода данных в поле трубы №1
            binding.tubing1Length.addTextChangedListener(
                func.checkEditText(
                    binding.tubing1Length
                ) { value: Float? ->
                    tubing1.length = value
                    binding.tubing2Length.isEnabled = value != null //если поле пустое, то второе не может быть заполнено и наоборот
                    if (value == null) {
                        binding.tubing3Length.isEnabled = false
                    } else if (binding.tubing2Length.length() != 0) binding.tubing3Length.isEnabled = value != null
                    well.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    valueDHv1.liftDepth_meter = well.liftDepth_meter
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing1Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.tubing1Length.setText(prefs.getString("tubing1Length", ""))
            //обработка потери фокуса полем ввода текста
            binding.tubing1Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                func.checkFocusChange(binding.tubing1Length, hasFocus, false)
                //если после потери фокуса поле будет пустое, то должен быть очищен следующий участок
                if (!hasFocus) {
                    if (binding.tubing1Length.length() == 0) {
                        binding.tubing2Length.text = null
                        binding.tubing3Length.text = null
                    }
                }
            }

            //обработка ввода данных в поле трубы №2
            binding.tubing2Length.addTextChangedListener(
                func.checkEditText(
                    binding.tubing2Length
                ) { value: Float? ->
                    tubing2.length = value
                    binding.tubing3Length.isEnabled = value != null //если поле пустое, то третье не может быть заполнено и наоборот
                    well.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    valueDHv1.liftDepth_meter = well.liftDepth_meter
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing2Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.tubing2Length.setText(prefs.getString("tubing2Length", ""))
            //обработка потери фокуса полем ввода текста
            binding.tubing2Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                func.checkFocusChange(binding.tubing2Length, hasFocus, false)
                //если после потери фокуса поле будет пустое, то должен быть очищен следующий участок
                if (!hasFocus) {
                    if (binding.tubing2Length.length() == 0) binding.tubing3Length.text = null
                }
            }

            //обработка ввода данных в поле трубы №3
            binding.tubing3Length.addTextChangedListener(
                func.checkEditText(
                    binding.tubing3Length
                ) { value: Float? ->
                    tubing3.length = value
                    well.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    valueDHv1.liftDepth_meter = well.liftDepth_meter
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в данном поле, а так же
                    //заполненных значениях давления на приеме и плотности жидкости
                    if (binding.tubing3Length.hasFocus() && binding.wellIntakePressure.text.toString() != "" && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.tubing3Length.setText(prefs.getString("tubing3Length", ""))
            //обработка потери фокуса полем ввода текста
            binding.tubing3Length.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.tubing3Length, hasFocus, false)
            }

            //обработка ввода данных в поле уровня жидкости
            binding.wellFluidLevel.addTextChangedListener(
                func.checkEditText(
                    binding.wellFluidLevel
                ) { value: Float? ->
                    if (valueDHv1.liftDepth_meter != null && value != null) {
                        /*производится проверка коректности ввода значения уровня жидкости в скважине*/
                        if (value <= valueDHv1.liftDepth_meter!!) {
                            well.fluidLevel_meter = value
                            //если данные введены корректно, то уведомление об ошибке пропадает
                            binding.levelAlertTitle.visibility = View.GONE
                        } else {
                            /*если данные введены не корректно, то появится уведомление оо ошибке
                            * а так же присвоется пустое значение в соответсвующее поле экземпляра
                            * класса Well, чтобы не производились некорректные расчеты*/
                            well.fluidLevel_meter = null
                            binding.levelAlertTitle.visibility = View.VISIBLE
                        }
                    } else well.fluidLevel_meter = null
                    valueDHv1.fluidLevel_meter = well.fluidLevel_meter
                    valueDHv1.fluidLevel_Pa = well.getFluidLevel_Pa()
                    getCalculations()
                    //при наличии плотности жидкости производим расчет давления на приеме оборудования
                    if (binding.wellFluidLevel.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellIntakePressure.setText(
                            valueDHv1.calcIntakePressure()?.let { it1 ->
                                well.intakePressure_type?.let { it2 ->
                                    well.pressureTypeList[it2]?.let { valRound(it1.times(it)).toString() }
                                }
                            })
                    }
                }
            )
            binding.wellFluidLevel.setText(prefs.getString("fluidLevel", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellFluidLevel.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.wellFluidLevel, hasFocus, true)
            }

            //обработка ввода данных в поле давление в затрубе
            binding.wellCasingPressure.addTextChangedListener(
                func.checkEditText(
                    binding.wellCasingPressure
                ) { value: Float? ->
                    well.casigPressure = value
                    valueDHv1.casigPressure_Pa = well.getCasingPressure_Pa()
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    valueDHv1.calcFluidLevel()
                        ?.let { binding.wellFluidLevel.setText(valRound(it).toString()) }

                }
            )
            binding.wellCasingPressure.setText(prefs.getString("casingPressure", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellCasingPressure.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.wellCasingPressure, hasFocus, true)
            }

            //обработка ввода данных в поле давление на буфере
            binding.wellHeadPressure.addTextChangedListener(
                func.checkEditText(
                    binding.wellHeadPressure
                ) { value: Float? ->
                    well.headPressure = value
                    valueDHv1.totalHeadPressure_Pa = well.getHeadPressure_Pa()
                    getCalculations()
                }
            )
            binding.wellHeadPressure.setText(prefs.getString("headPressure", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellHeadPressure.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.wellHeadPressure, hasFocus, true)
            }

            //обработка ввода данных в поле давление на приеме
            binding.wellIntakePressure.addTextChangedListener(
                func.checkEditText(
                    binding.wellIntakePressure
                ) { value: Float? ->
                    well.intakePressure = value
                    valueDHv1.intakePressure_Pa = well.getIntakePressure_Pa()
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в поле плотности жидкости
                    if (binding.wellIntakePressure.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.wellIntakePressure.setText(prefs.getString("intakePressure", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellIntakePressure.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.wellIntakePressure, hasFocus, true)
            }

            //обработка ввода данных в поле плотности жидкости
            binding.wellFluidDensity.addTextChangedListener(
                func.checkEditText(
                    binding.wellFluidDensity
                ) { value: Float? ->
                    well.densityFluid = value
                    valueDHv1.fluidDensity = well.densityFluid
                    getCalculations()
                    //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                    //расчет производится только при условии наличия фокуса в поле плотности жидкости
                    if (binding.wellFluidDensity.hasFocus() && binding.wellIntakePressure.text.toString() != "") {
                        binding.wellFluidLevel.setText(
                            valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                        )
                    }
                }
            )
            binding.wellFluidDensity.setText(prefs.getString("fluidDensity", ""))
            //обработка потери фокуса полем ввода текста
            binding.wellFluidDensity.setOnFocusChangeListener { _, hasFocus ->
                func.checkFocusChange(binding.wellFluidDensity, hasFocus, false)
            }

            //обработка нажатия иконки "назад" в заголовке фрагмента
            val toolbar: Toolbar = binding.toolbar
            toolbar.setNavigationOnClickListener {
                MainActivity.vibro(requireContext())
                activity!!.onBackPressed()
            }

            //обработка нажатий информационных кнопок
            binding.infoSet1.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1), requireContext())
            }
            binding.infoSet2.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2), requireContext())
            }
            binding.infoSet3.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info3), requireContext())
            }
            binding.infoSet4.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info4), requireContext())
            }
            binding.infoSet5.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info5), requireContext())
            }
            binding.infoSet6.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info6), requireContext())
            }
            binding.infoSet7.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info7), requireContext())
            }
        }
        return binding.root
    }

    //инициализация адаптеров для прокручиваемых списков RecyclerView, используемых в фрагменте
    private fun adaptersInit() {
        //инициализация объектов фрагмента
        container1 = Container()
        container2 = Container()
        container3 = Container()
        container4 = Container()
        container5 = Container()
        container6 = Container()
        container7 = Container()
        container8 = Container()

        pump = context?.let { Pump(context = it) }!!
        tubing1 = context?.let { Tubing(context = it) }!!
        tubing2 = context?.let { Tubing(context = it) }!!
        tubing3 = context?.let { Tubing(context = it) }!!
        well = context?.let { Well(context = it) }!!

        //инициализация контейнера для прокручиваемого списка рабочей частоты насоса
        container1.adapter = pump.setAdapter(
            binding.operFreq,
            pump.freqList,
            pump.frequencyHolderList,
            "pumpOperFreq"
        )
        binding.operFreq.adapter = container1.adapter
        container1.snapHelper.attachToRecyclerView(binding.operFreq)
        binding.operFreq.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.operFreq.onFlingListener = container1.snapHelper
        binding.operFreq.smoothScrollToPosition(prefs.getInt("pumpOperFreq", 0))

        //инициализация контейнера для прокручиваемого списка типов мощности насоса
        container2.adapter = pump.setAdapter(
            binding.powerType,
            pump.powerTypeList,
            pump.powerTypeHolderList,
            "pumpPowerType"
        )
        binding.powerType.adapter = container2.adapter
        container2.snapHelper.attachToRecyclerView(binding.powerType)
        binding.powerType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.powerType.onFlingListener = container2.snapHelper
        binding.powerType.smoothScrollToPosition(prefs.getInt("pumpPowerType", 0))

        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ
        container3.adapter = tubing1.setAdapter(
            binding.tubing1Type,
            tubing1.tubingTypesList.keys.toList().toMutableList(),
            tubing1.tubingHolderList,
            "tubing1Type"
        )
        binding.tubing1Type.adapter = container3.adapter
        container3.snapHelper.attachToRecyclerView(binding.tubing1Type)
        binding.tubing1Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing1Type.onFlingListener = container3.snapHelper
        binding.tubing1Type.smoothScrollToPosition(prefs.getInt("tubing1Type", 0))

        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ
        container4.adapter = tubing2.setAdapter(
            binding.tubing2Type,
            tubing2.tubingTypesList.keys.toList().toMutableList(),
            tubing2.tubingHolderList,
            "tubing2Type"
        )
        binding.tubing2Type.adapter = container4.adapter
        container4.snapHelper.attachToRecyclerView(binding.tubing2Type)
        binding.tubing2Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing2Type.onFlingListener = container4.snapHelper
        binding.tubing2Type.smoothScrollToPosition(prefs.getInt("tubing2Type", 0))

        //инициализация контейнера для прокручиваемого списка типразмеровтрубы НКТ
        container5.adapter = tubing3.setAdapter(
            binding.tubing3Type,
            tubing3.tubingTypesList.keys.toList().toMutableList(),
            tubing3.tubingHolderList,
            "tubing3Type"
        )
        binding.tubing3Type.adapter = container5.adapter
        container5.snapHelper.attachToRecyclerView(binding.tubing3Type)
        binding.tubing3Type.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.tubing3Type.onFlingListener = container5.snapHelper
        binding.tubing3Type.smoothScrollToPosition(prefs.getInt("tubing3Type", 0))

        //инициализация контейнера для прокручиваемого списка типов буферного давления
        container6.adapter = well.setAdapter(
            binding.wellHeadPressureType,
            well.pressureTypeList.keys.toList().toMutableList(),
            well.HeadPressureHolderList,
            "wellHeadPressureType"
        )
        binding.wellHeadPressureType.adapter = container6.adapter
        container6.snapHelper.attachToRecyclerView(binding.wellHeadPressureType)
        binding.wellHeadPressureType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.wellHeadPressureType.onFlingListener = container6.snapHelper
        binding.wellHeadPressureType.smoothScrollToPosition(prefs.getInt("wellHeadPressureType", 0))

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

    //произведение расчетов и заполнение необходимых полей полученными значениями
    private fun getCalculations() {
        if (func.statusOfAdapters) { //разрешаем запускать вычисления только после инициализации всех адаптеров для увеличения быстродействия
            val typeHelper: (String?) -> String? = { str: String? ->
                when (str) {
                    null -> "-"
                    ValueDownhole.TURBULENT1 -> context?.getString(R.string.flow_type_turbulent1)
                    ValueDownhole.TURBULENT2 -> context?.getString(R.string.flow_type_turbulent2)
                    ValueDownhole.TURBULENT3 -> context?.getString(R.string.flow_type_turbulent3)
                    ValueDownhole.LAMINAR -> context?.getString(R.string.flow_type_laminar)
                    ValueDownhole.TRANSITIONAL -> context?.getString(R.string.flow_type_transitional)
                    else -> {
                        "-"
                    }
                }
            }
            val tubing1flowtypeWater = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.water_kinVisc10_7_50C, this).tflTubing1?.type
                } else null
            }
            val tubing1flowtypeOil = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.oil_kinVisc10_7_50C, this).tflTubing1?.type
                } else null
            }
            val tubing2flowtypeWater = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.water_kinVisc10_7_50C, this).tflTubing2?.type
                } else null
            }
            val tubing2flowtypeOil = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.oil_kinVisc10_7_50C, this).tflTubing2?.type
                } else null
            }
            val tubing3flowtypeWater = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.water_kinVisc10_7_50C, this).tflTubing3?.type
                } else null
            }
            val tubing3flowtypeOil = with(valueDHv1.operFreq) {
                if (this != null) {
                    tdhCal(TFL_Calc.oil_kinVisc10_7_50C, this).tflTubing3?.type
                } else null
            }
            val recommendationFrequency: Int? = getRecommendationFrequency()
            val recommendationStages = getRecommendationStages()

            binding.tubing1FlowOilValue.text = typeHelper(tubing1flowtypeOil)
            binding.tubing1FlowWaterValue.text = typeHelper(tubing1flowtypeWater)
            binding.tubing2FlowOilValue.text = typeHelper(tubing2flowtypeOil)
            binding.tubing2FlowWaterValue.text = typeHelper(tubing2flowtypeWater)
            binding.tubing3FlowOilValue.text = typeHelper(tubing3flowtypeOil)
            binding.tubing3FlowWaterValue.text = typeHelper(tubing3flowtypeWater)
            binding.tubingTotalLengthValue.text =
                valRound(valueDHv1.liftDepth_meter).toString()
            binding.pumpFlowrateOperfreq.text = valRound(valueDHv1.operFreq).toString()
            binding.pumpHeadOperfreq.text = valRound(valueDHv1.operFreq).toString()
            binding.pumpPowerHPOperfreq.text = valRound(valueDHv1.operFreq).toString()
            binding.pumpPowerKVTOperfreq.text = valRound(valueDHv1.operFreq).toString()
            binding.pumpFlowrateVal.text =
                valRound(pump.get_q_m3_operFreq(valueDHv1.operFreq))?.toString() ?: "-"
            binding.pumpHeadVal.text =
                valRound(pump.get_head_m_operFreq(valueDHv1.operFreq))?.toString() ?: "-"
            if (pump.power_type == pump.HP) {
                binding.pumpPowerHPVal.text =
                    valRound(pump.get_power_operFreq(valueDHv1.operFreq))?.toString() ?: "-"
                binding.pumpPowerKVTVal.text = valRound(
                    pump.get_power_operFreq(valueDHv1.operFreq)
                        ?.times(0.7457f)
                )?.toString() ?: "-"
            } else {
                binding.pumpPowerHPVal.text = valRound(
                    pump.get_power_operFreq(valueDHv1.operFreq)
                        ?.times(1.341f)
                )?.toString() ?: "-"
                binding.pumpPowerKVTVal.text =
                    valRound(pump.get_power_operFreq(valueDHv1.operFreq))?.toString() ?: "-"
            }
            if (recommendationFrequency == null) {
                binding.recommendation1Val.text = "-"
            } else if (recommendationFrequency > 0) {
                binding.recommendation1Val.text = recommendationFrequency.toString()
            } else if (recommendationFrequency < 0) {
                binding.recommendation1Val.text =
                    requireContext().getText(R.string.recommendation3_title)
            }
            binding.recommendation2Val.text = valRound(recommendationStages)?.toString() ?: "-"
            //значения рабочей частоты напорных показателей в графиках
            binding.graf1Txt3OperfreqVal.text = valRound(valueDHv1.operFreq).toString()
            binding.graf1Txt4OperfreqVal.text = valRound(valueDHv1.operFreq).toString()
            binding.graf1Txt5OperfreqVal.text = valRound(valueDHv1.operFreq).toString()
            //значение напора по нефти в графиках
            binding.graf1Txt3HeadVal.text =
                (valRound(tdhCal(TFL_Calc.oil_kinVisc10_7_50C, valueDHv1.operFreq ?: 0f).tdh)
                    ?: "-").toString()
            //значение напора по воде в графиках
            binding.graf1Txt4HeadVal.text =
                (valRound(tdhCal(TFL_Calc.water_kinVisc10_7_50C, valueDHv1.operFreq ?: 0f).tdh)
                    ?: "-").toString()
            //значение напора насоса в графиках
            binding.graf1Txt5HeadVal.text =
                (valRound(pump.get_head_m_operFreq(valueDHv1.operFreq)) ?: "-").toString()
            //время подачи
            binding.timeRes.text =
                (valRound(valueDHv1.calcFlowAppearanceTime(tubing1, tubing2, tubing3)?.time_min)
                    ?: "-").toString()
            //объем при расчете времени подачи
            binding.volumeRes.text =
                (valRound(valueDHv1.calcFlowAppearanceTime(tubing1, tubing2, tubing3)?.volume_m3)
                    ?: "-").toString()
            //общий объем трубы НКТ
            if (tubing1.length != null) {
                binding.tubingTotalVolumeValue.text =
                    valRound(valueDHv1.calcTotalTubingVolume(tubing1, tubing2, tubing3)).toString()
            } else binding.tubingTotalVolumeValue.text = "-"

            chartBuild()
        }
    }

    //функция вывода графика
    private fun chartBuild() {
        val chart: LineChart = binding.chart1
        val pumpHead_meter: MutableList<Entry> =
            mutableListOf() //график напорной характеристики насоса
        val tfhOil_meter: MutableList<Entry> = mutableListOf() //график TDH для нефти
        val tflWater_meter: MutableList<Entry> = mutableListOf() //график TDH для воды
        val operFrequencyPump: MutableList<Entry> = mutableListOf() //рабочая точка насоса

        chart.clear() //очищаем старый график, который возможно мог иметь место
        chart.setScaleYEnabled(false) //отключаем масштабирование графика по вертикали
        chart.description.isEnabled = false //отключаем вывод описания графика
        val legend: Legend = chart.legend
        legend.isEnabled = false // отключаем легенду, она будет своя

        //подготовка и построение графиков производится только при наличии всех необходимых данных
        if (valueDHv1.getStatusReady()) {
            //заполняем массивы значениями из сводного массива
            //определяем рабочую точку на графике, согласно рабочей частоте
            operFrequencyPump.add(
                Entry(
                    valueDHv1.operFreq!!,
                    pump.get_head_m_operFreq(valueDHv1.operFreq!!)!!
                )
            )
            for (str in 0 until pump.freqList.size) {
                //заполняем массив зависимости напора насоса от рабочей частоты
                pumpHead_meter.add(
                    Entry(
                        pump.freqList[str].toFloat(),
                        valRound(pump.get_head_m_operFreq(pump.freqList[str].toFloat()))?.toFloat()
                            ?: 0f
                    )
                )
                //заполняем массив зависимости динамического напоря от рабочей частоты для нефти
                tfhOil_meter.add(
                    Entry(
                        pump.freqList[str].toFloat(),
                        tdhCal(TFL_Calc.oil_kinVisc10_7_50C, pump.freqList[str].toFloat()).tdh ?: 0f
                    )
                )
                //заполняем массив зависимости динамического напоря от рабочей частоты для воды
                tflWater_meter.add(
                    Entry(
                        pump.freqList[str].toFloat(),
                        tdhCal(TFL_Calc.water_kinVisc10_7_50C, pump.freqList[str].toFloat()).tdh
                            ?: 0f
                    )
                )
            }

            //формируются датасеты графиков на основании заполненных массивов
            val dataset: LineDataSet = LineDataSet(pumpHead_meter, "1")
            val dataset2: LineDataSet = LineDataSet(tfhOil_meter, "2")
            val dataset3: LineDataSet = LineDataSet(tflWater_meter, "3")
            val pointChart: LineDataSet = LineDataSet(operFrequencyPump, "4")

            //ниже идет кастомизация графиков
            //настраивается внешний вид графика напорной характеристики насоса
            dataset.setDrawFilled(false)
            dataset.setColor(Color.GREEN)
            dataset.isHighlightEnabled = false
            dataset.setDrawCircles(false)
            dataset.lineWidth = 2f
            //настраивается внешний вид графика динамического напора для нефти
            dataset2.setDrawFilled(false)
            dataset2.setColor(Color.BLACK)
            dataset2.setDrawFilled(true)
            dataset2.fillColor = Color.YELLOW
            dataset2.isHighlightEnabled = false
            dataset2.setDrawCircles(false)
            dataset2.lineWidth = 2f
            //настраивается внешний вид графика динамического напора для воды
            dataset3.setDrawFilled(false)
            dataset3.setColor(Color.BLUE)
            dataset3.setDrawFilled(true)
            dataset3.fillColor = Color.RED
            dataset3.isHighlightEnabled = false
            dataset3.setDrawCircles(false)
            dataset3.lineWidth = 2f
            //настраивается внешний вид графика динамического напора для воды
            pointChart.setCircleColors(Color.RED)
            pointChart.circleHoleColor = Color.YELLOW
            pointChart.circleRadius = 5f
            pointChart.circleHoleRadius = 2f
            pointChart.setColor(Color.YELLOW)

            //все датасеты (графики) собираются в один массив
            val datasets: MutableList<ILineDataSet> = mutableListOf()
            datasets.add(dataset)
            datasets.add(dataset2)
            datasets.add(dataset3)
            datasets.add(pointChart)

            val lineData: LineData = LineData(datasets)

            //все графики передаются на вывод
            chart.setData(lineData)
            chart.setTouchEnabled(false)
            chart.axisLeft.spaceBottom = 0f
            //chart.xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
            chart.axisRight.isEnabled = false
            chart.xAxis.labelCount = 5

            chart.invalidate()
        }
    }

    //функция для расчета полного динамического напора. Вызывается из построителя графиков
    private fun tdhCal(coefVisc: Float, freq: Float) = object {
        val tflTubing1 = with(pump.get_q_m3_operFreq(freq)) {
            if (this != null) {
                TFL_Calc.tflInMeter(
                    this,
                    tubing1.getDiamInMeter(tubing1.tubingIDmm), tubing1.length ?:0f, coefVisc
                )
            } else null
        }

        val tflTubing2 = with(pump.get_q_m3_operFreq(freq)) {
            if (this != null) {
                TFL_Calc.tflInMeter(
                    this,
                    tubing2.getDiamInMeter(tubing2.tubingIDmm), tubing2.length ?:0f, coefVisc
                )
            } else null
        }

        val tflTubing3 = with(pump.get_q_m3_operFreq(freq)) {
            if (this != null) {
                TFL_Calc.tflInMeter(
                    this,
                    tubing3.getDiamInMeter(tubing3.tubingIDmm), tubing3.length ?:0f, coefVisc
                )
            } else null
        }

        var tdh: Float? = null

        init {
            try {
                tdh =
                    valueDHv1.fluidLevel_meter!! + valueDHv1.totalHeadPressure_Pa!!.times(0.000102f) + (tflTubing1?.value
                        ?: 0f) + (tflTubing2?.value
                        ?: 0f) + (tflTubing3?.value ?: 0f)
            } catch (_: Exception) {
            }
        }
    }

    private fun getRecommendationFrequency(): Int? {
        var tdhWater: Float? = null
        var tdhOil: Float? = null
        var pumpHead: Float? = null
        var freq = 0

        with(valueDHv1.operFreq) {
            if (this != null) {
                for (i in this.toInt()..pump.freqList[pump.freqList.size - 1]) {
                    freq = i
                    tdhWater = tdhCal(TFL_Calc.water_kinVisc10_7_50C, freq.toFloat()).tdh
                    tdhOil = tdhCal(TFL_Calc.oil_kinVisc10_7_50C, freq.toFloat()).tdh
                    pumpHead = pump.get_head_m_operFreq(freq.toFloat())
                    if (pumpHead != null && tdhWater != null && tdhOil != null) {
                        if (pumpHead!! > tdhWater!! && pumpHead!! > tdhOil!!) break
                        if (freq == pump.freqList[pump.freqList.size - 1]) freq = -1
                    } else break
                }
            }
        }
        return if (valueDHv1.operFreq != null) {
            if (freq == 0 || freq == valueDHv1.operFreq!!.toInt()) null else freq
        } else null
    }

    private fun getRecommendationStages(): Float? {
        if (valueDHv1.operFreq == null) return null
        val tdhWater = tdhCal(TFL_Calc.water_kinVisc10_7_50C, valueDHv1.operFreq!!).tdh
        val tdhOil = tdhCal(TFL_Calc.oil_kinVisc10_7_50C, valueDHv1.operFreq!!).tdh
        val pumpHead = pump.get_head_m_operFreq(valueDHv1.operFreq!!)

        if (pumpHead != null && tdhWater != null && tdhOil != null) {
            if (tdhWater >= tdhOil) {
                return if (tdhWater.div(pumpHead) < 1) {
                    null
                } else {
                    if (valueDHv1.pumpStageQty != null && valueDHv1.pumpHead != null) {
                        valueDHv1.pumpStageQty!! * tdhWater / valueDHv1.pumpHead!!
                    } else {
                        null
                    }
                }
            } else if (tdhOil.div(pumpHead) < 1) {
                return null
            } else {
                return if (valueDHv1.pumpStageQty != null && valueDHv1.pumpHead != null) {
                    valueDHv1.pumpStageQty!! * tdhOil / valueDHv1.pumpHead!!
                } else {
                    null
                }
            }
        } else return null
    }

    override fun onPause() {
        super.onPause()
        wasPause =
            true //фиксируем, что фрагмент уже был активен, чтобы предотвратить повторное перестроение элементов
        //при ухода с фрагмента сохраняем значения всех полей в файл настроек
        val editor = prefs.edit()
        editor.putInt(
            "pumpOperFreq",
            func.posInMutableList(binding.operFreq.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "pumpPowerType",
            func.posInMutableList(binding.powerType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "tubing1Type",
            func.posInMutableList(binding.tubing1Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "tubing2Type",
            func.posInMutableList(binding.tubing2Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "tubing3Type",
            func.posInMutableList(binding.tubing3Type.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "wellHeadPressureType",
            func.posInMutableList(binding.wellHeadPressureType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "wellCasingPressureType",
            func.posInMutableList(binding.wellCasingPressureType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "wellIntakePressureType",
            func.posInMutableList(binding.wellIntakePressureType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putString("pumpFlowRate", binding.pumpQ.text.toString()).apply()
        editor.putString("pumpHead", binding.pumpH.text.toString()).apply()
        editor.putString("pumpStagesQTY", binding.pumpS.text.toString()).apply()
        editor.putString("pumpPowerVal", binding.pumpP.text.toString()).apply()
        editor.putString("tubing1Length", binding.tubing1Length.text.toString()).apply()
        editor.putString("tubing2Length", binding.tubing2Length.text.toString()).apply()
        editor.putString("tubing3Length", binding.tubing3Length.text.toString()).apply()
        editor.putString("fluidLevel", binding.wellFluidLevel.text.toString()).apply()
        editor.putString("casingPressure", binding.wellCasingPressure.text.toString()).apply()
        editor.putString("headPressure", binding.wellHeadPressure.text.toString()).apply()
        editor.putString("intakePressure", binding.wellIntakePressure.text.toString()).apply()
        editor.putString("fluidDensity", binding.wellFluidDensity.text.toString()).apply()
    }

    override fun onResume() {
        super.onResume()
        getCalculations()
    }
}