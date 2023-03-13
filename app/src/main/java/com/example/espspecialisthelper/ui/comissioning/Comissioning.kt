package com.example.espspecialisthelper.ui.comissioning

import Tubing
import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Classes.Calculations.Container
import com.example.espspecialisthelper.Classes.MyCustomDialogs.WellCharacterDialog
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDHv1
import com.example.espspecialisthelper.Classes.Well.Well
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.TimerServiceHelper
import com.example.espspecialisthelper.databinding.FragmentComissioningBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class Comissioning : Fragment() {
    /**для того, чтобы при нажатии на уведомление запускалась текущая активность, а не создавалась новая
    а Manifest в MainActivity добавлена строка android:launchMode="singleTop"
    так же обрати внимание, что в Manifest добавлена отдельная информация по данному сервису*/

    private lateinit var binding: FragmentComissioningBinding
    private var wasPause = false //используется для исключения двойной инициализации адаптеров
    private lateinit var prefs: SharedPreferences
    //переменные для элементов интерфейса
    private lateinit var timerServiceHelper: TimerServiceHelper
    private lateinit var valueDHv1: ValueDHv1
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
    private val SAVE = "save"
    private val RESTORE = "restore"
    private val CLEAN = "clean"

    private var selectedActyvityType = "undefined"

    /**Лямбда для вызова диалогового фрагмента для вывода запроса о том, какая активность наступила*/
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    val timerDialogCaller = {
        val acivityType = arrayOf(getString(R.string.comis_startup_title),
            getString(R.string.comis_flow_appear_time_title))
        val mAlertDialog = AlertDialog.Builder(requireContext())
        mAlertDialog.setTitle(getString(R.string.comis_select_activity))
            .setSingleChoiceItems(acivityType, -1){_, item ->
                selectedActyvityType = acivityType[item]
            }
        mAlertDialog.setPositiveButton("OK") { dialog, _ ->
            timerController()
            dialog.dismiss()
        }
        mAlertDialog.setNegativeButton(getString(R.string.cancel_title)) { dialog, _ ->
            dialog.dismiss()
        }
        mAlertDialog.show()
    }

    /**Лямбда для вызова диалогового фрагмента для определения уровня жидкости в скважине*/
    val levelFilling : (EditText, Int) -> Unit = { editText, key ->
        val customDialog = WellCharacterDialog(valueDHv1, editText, key)
        val manager = requireActivity().supportFragmentManager
        customDialog.show(manager, "myDialog")
    }

    /**Колбэк*/
    private val callback: (Boolean) -> Unit = {
        dialogRes ->
        if (dialogRes) commissionData(CLEAN)
    }

    //функция для ожидания инициализации всех адаптеров
    private fun waitAdaptersInit() {
        if ((container1.adapter?.isInit == true
                    && container2.adapter?.isInit == true
                    && container3.adapter?.isInit == true
                    && container4.adapter?.isInit == true
                    && container5.adapter?.isInit == true
                    && container6.adapter?.isInit == true) && !uiFunc.statusOfAdapters
        ) {
            getCalculations()
            uiFunc.statusOfAdapters = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!wasPause) {
            binding = FragmentComissioningBinding.inflate(inflater)
            timerServiceHelper = TimerServiceHelper(binding.watchTimer, requireContext(), requireActivity())
            uiFunc = UIFunctions(requireContext())
            valueDHv1 = ValueDHv1()
            uiFunc.currentThemeTextColor = binding.tubingTitle.currentTextColor
            adaptersInit() //инициализация адаптеров для прокручиваемых списков

            /**БЛОК ОБРАБОТКИ ПРОКРУЧИВАЕМЫХ ПОЛЕЙ ФРАГМЕНТА*/

            /**обрабатываем прокручиваемый список типа трубы НКТ №1*/
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

            /**обрабатываем прокручиваемый список типа трубы НКТ №2*/
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

            /**обрабатываем прокручиваемый список типа трубы НКТ №3*/
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

            /**обрабатываем прокручиваемый список типа трубы ЭК №1*/
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

            /**обрабатываем прокручиваемый список типа трубы ЭК №2*/
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

            /**обрабатываем прокручиваемый список типа трубы ЭК №3*/
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

            /**БЛОК ОБРАБОТКИ ТЕКСТОВЫХ ПОЛЕЙ ФРАГМЕНТА*/

            /**обработка ввода данных в поле трубы НКТ №1*/
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
                }
            )
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
            binding.tubing1Length.setText(prefs.getString("tubing1Length", ""))

            /**обработка ввода данных в поле трубы НКТ №2*/
            binding.tubing2Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.tubing2Length
                ) { value: Float? ->
                    tubing2.length = value
                    binding.tubing3Length.isEnabled = value != null //если поле пустое, то третье не может быть заполнено и наоборот
                    valueDHv1.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    getCalculations()
                }
            )
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.tubing2Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.tubing2Length, hasFocus, false)
                //если после потери фокуса поле будет пустое, то должен быть очищен следующий участок
                if (!hasFocus) {
                    if (binding.tubing2Length.length() == 0) binding.tubing3Length.text = null
                }
            }
            binding.tubing2Length.setText(prefs.getString("tubing2Length", ""))

            /**обработка ввода данных в поле трубы НКТ №3*/
            binding.tubing3Length.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.tubing3Length
                ) { value: Float? ->
                    tubing3.length = value
                    valueDHv1.liftDepth_meter = (tubing1.length ?:0f) + (tubing2.length ?:0f) + (tubing3.length ?:0f)
                    getCalculations()
                }
            )
            //обработка потери фокуса полем ввода текста
            binding.tubing3Length.setOnFocusChangeListener { _, hasFocus ->
                uiFunc.checkFocusChange(binding.tubing3Length, hasFocus, false)
            }
            binding.tubing3Length.setText(prefs.getString("tubing3Length", ""))

            /**обработка ввода данных в поле трубы ЭК №1*/
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
                        } else casing1.length = value
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
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.casing1Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.casing1Length, hasFocus, false)
                if (!hasFocus) {
                    if (binding.casing1Length.length() == 0) {
                        binding.casing2Length.text = null
                        binding.casing3Length.text = null
                    }
                }
            }
            binding.casing1Length.setText(prefs.getString("casing1Length", ""))

            /**обработка ввода данных в поле трубы ЭК №2*/
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
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 3-го участка
            binding.casing2Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.casing2Length, hasFocus, false)
                if (!hasFocus) {
                    if (binding.casing2Length.length() == 0) binding.casing3Length.text = null
                }
            }
            binding.casing2Length.setText(prefs.getString("casing2Length", ""))

            /**обработка ввода данных в поле трубы ЭК №3*/
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
            binding.casing3Length.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.casing3Length, hasFocus, false)
            }
            binding.casing3Length.setText(prefs.getString("casing3Length", ""))

            /**обработка ввода данных в поле "Время ожидания" раздела "Появление подачи"*/
            binding.flAppearanceTimeValue.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.flAppearanceTimeValue
                ) { value: Float? ->
                    getCalculations()
                }
            )
            binding.flAppearanceTimeValue.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.flAppearanceTimeValue, hasFocus, false)
            }

            /**обработка ввода данных в поле "Время ожидания" раздела "Замер"*/
            binding.measureTimeValue.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.measureTimeValue
                ) { value: Float? ->
                    /*При изменения значения в поле будет автоматически
                    * произведен пересчет значений в поле "Измеренный дебит", при заполненном
                    * значениии "Измеренный объем", или "Измеренный объем", при заполненном значении поля "Измеренный дебит"*/
                    if (binding.measureTimeValue.hasFocus() &&
                        binding.measuredVolumeVal.text.toString() != "" && value != null &&
                            value != 0f) {
                        binding.measuredFlowrateVal.setText(valueDHv1.valRound(binding.measuredVolumeVal.text.toString().toFloat().times(1440f).div(value)).toString())
                    } else if (binding.measureTimeValue.hasFocus() &&
                        binding.measuredFlowrateVal.text.toString() != "" && value != null) {
                        binding.measuredVolumeVal.setText(valueDHv1.valRound(binding.measuredFlowrateVal.text.toString().toFloat().times(value).div(1440f)).toString())
                    }
                    getCalculations()
                }
            )
            binding.measureTimeValue.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.measureTimeValue, hasFocus, false)
            }


            /**обработка ввода данных в поле "Измеренный объем" раздела "Замер"*/
            binding.measuredVolumeVal.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.measuredVolumeVal
                ) { value: Float? ->
                    /*При изменении значения в поле автоматически пересчитается
                    * измеренный дебит в поле "Измеренный дебит" раздела "Замер"
                    * расчет будет произведен при заполненном значении поля "Время ожидания" раздела "Замер" */
                    if (binding.measureTimeValue.text.toString() != "" &&
                            value != null && binding.measuredVolumeVal.hasFocus()) {
                        binding.measuredFlowrateVal.setText(valueDHv1.valRound(value.times(1440f).div(binding.measureTimeValue.text.toString().toFloat())).toString())
                    }
                    getCalculations()
                }
            )
            binding.measuredVolumeVal.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.measuredVolumeVal, hasFocus, false)
            }

            /**обработка ввода данных в поле "Измеренный дебит" раздела "Замер"*/
            binding.measuredFlowrateVal.addTextChangedListener(
                uiFunc.checkEditText(
                    binding.measuredFlowrateVal
                ) { value: Float? ->
                    /*При изменении значения в поле автоматически пересчитается
                    * измеренный объем в поле "Измеренный объем" раздела "Замер"
                    * расчет будет произведен при заполнении значения поля "Время ожидания" раздела "замер"*/
                    if (binding.measureTimeValue.text.toString() != "" &&
                        value != null && binding.measuredFlowrateVal.hasFocus()) {
                        binding.measuredVolumeVal.setText(valueDHv1.valRound(value.times(binding.measureTimeValue.text.toString().toFloat()).div(1440f)).toString())
                    }
                    getCalculations()
                }
            )
            binding.measuredFlowrateVal.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.measuredFlowrateVal, hasFocus, false)
            }

            /**вызов диалога с параметрами скважины для "Уровень" раздела "Запуск"*/
            binding.startupTimeFluidLevelVal.setOnClickListener {
                levelFilling(binding.startupTimeFluidLevelVal, 1)
            }
            binding.startupTimeFluidLevelVal.addTextChangedListener {
                getCalculations()
            }

            /**вызов диалога с параметрами скважины для "Уровень" раздела "Появление подачи"*/
            binding.flAppearanceTimeFluidLevelVal.setOnClickListener {
                levelFilling(binding.flAppearanceTimeFluidLevelVal, 2)
            }
            binding.flAppearanceTimeFluidLevelVal.addTextChangedListener {
                getCalculations()
            }

            /**вызов диалога с параметрами скважины для "Уровень" раздела "Замер!*/
            binding.measureTimeFluidLevelVal.setOnClickListener {
                levelFilling(binding.measureTimeFluidLevelVal, 3)
            }
            binding.measureTimeFluidLevelVal.addTextChangedListener {
                getCalculations()
            }

            //восстанавливаем данные по ВНР
            commissionData(RESTORE)

            /**обработка нажатия иконки "назад" в заголовке фрагмента*/
            val toolbar: Toolbar = binding.toolbar
            toolbar.setNavigationOnClickListener {
                MainActivity.vibro(requireContext())
                requireActivity().onBackPressed()
            }

            /**обработка нажатия на кнопку запуска секундомера*/
            binding.buttonActivity.setOnClickListener {
                //если таймер не запущен, вызываем диалог, который спросит с какой активности начать отсчет времени
                MainActivity.vibro(requireContext())
                if (!timerServiceHelper.timerStarted) {
                    timerDialogCaller()
                }
                /*если таймер запущен, то при последующих нажатиях кнопки будет запускаться следующая
                по порядку активность*/
                else {
                    when (selectedActyvityType) {
                        getString(R.string.comis_startup_title) -> {
                            /*если нажатие произошло на этапе ожидания подачи, то произодет фиксирование времени подачи
                            * далее будет сброшен таймер и обновлена запись на кнопке запуска об ожидании новой активности*/
                            selectedActyvityType = getString(R.string.comis_flow_appear_time_title)
                            timerController()
                        }
                        getString(R.string.comis_flow_appear_time_title) -> {
                            /*если нажатие произошло на этапе ожидания замера, то произодет фиксирование времени замера,
                            возвращена надпись ожидания на кнопку запуска секундомера и остановлен таймер*/
                            binding.measurementTimeVal.text = getCurrentDateAndTime()
                            binding.measureTimeValue.setText(getMinutesFromDouble(timerServiceHelper.time))
                            //пишем на кнопке запуска секундомера вид активности
                            binding.buttonActivity.text = getText(R.string.button_waiting)
                            //останавливаем таймер
                            timerServiceHelper.stopTimer()
                        }
                    }
                }
            }

            /**обработка кнопки нажатия очистки полей ВНР*/
            binding.cleaner.setOnClickListener {
                if (timerServiceHelper.isMyServiceRunning()) {
                    /*если сервис секундомера работает, то при попытке очистить поля ВНР
                    * выдаем пользователю сообщение, что сейчас это сделать не возможно*/
                    MainActivity.dialogCaller(getString(R.string.cleaner_dialog1), requireContext(),
                        getString(R.string.cleaner_dialog_title))
                } else {
                    /*если сервис секундомера не рабоает, то выводим диалогое окно с выбор действия*/
                    MainActivity.dialogCaller(getString(R.string.cleaner_dialog2), requireContext(),
                        getString(R.string.cleaner_dialog_title), true, callback)
                }
            }

            /**Обработка нажатий информационных кнопок*/

            binding.infoSet1.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2), requireContext())
            }
            binding.infoSet2.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_2_3), requireContext())
            }
            binding.infoSet3.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_3_3), requireContext())
            }
            binding.infoSet4.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_3_4), requireContext())
            }
            binding.infoSet5.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info2_3_5), requireContext())
            }

        }
        return binding.root
    }

    //восстанавление значений полей фрагмента, относящихся к ВНР
    private fun commissionData(action: String) {
        when (action) {
            //сохранение данных
            SAVE -> {
                val editor = prefs.edit()
                editor.putBoolean("timerStarted", timerServiceHelper.timerStarted).apply()
                editor.putString("buttonText", binding.buttonActivity.text.toString()).apply()
                editor.putString("selectedActyvityType", selectedActyvityType).apply()
                editor.putString("startupTimeValue", binding.startupTimeValue.text.toString()).apply()
                editor.putString("startupTimeFluidLevelVal", binding.startupTimeFluidLevelVal.text.toString()).apply()
                editor.putString("flAppearanceTimeVal", binding.flAppearanceTimeVal.text.toString()).apply()
                editor.putString("flAppearanceTimeValue", binding.flAppearanceTimeValue.text.toString()).apply()
                editor.putString("flAppearanceTimeFluidLevelVal", binding.flAppearanceTimeFluidLevelVal.text.toString()).apply()
                editor.putString("measurementTimeVal", binding.measurementTimeVal.text.toString()).apply()
                editor.putString("measureTimeValue", binding.measureTimeValue.text.toString()).apply()
                editor.putString("measureTimeFluidLevelVal", binding.measureTimeFluidLevelVal.text.toString()).apply()
                editor.putString("measuredVolumeVal", binding.measuredVolumeVal.text.toString()).apply()
                editor.putString("measuredFlowrateVal", binding.measuredFlowrateVal.text.toString()).apply()
            }
            //восстановление данных
            RESTORE -> {
                if (timerServiceHelper.isMyServiceRunning()) {
                    timerServiceHelper.timerStarted = true
                    selectedActyvityType = prefs.getString("selectedActyvityType", "undefined") ?: "undefined"
                    binding.buttonActivity.text = prefs.getString("buttonText", getString(R.string.button_waiting))
                    } else {
                    timerServiceHelper.timerStarted = false
                    selectedActyvityType = "undefined"
                    binding.buttonActivity.text = getString(R.string.button_waiting)
                }
                binding.startupTimeValue.text = prefs.getString("startupTimeValue", "-")
                binding.startupTimeFluidLevelVal.setText(prefs.getString("startupTimeFluidLevelVal", ""))
                binding.flAppearanceTimeVal.text = prefs.getString("flAppearanceTimeVal", "-")
                binding.flAppearanceTimeValue.setText(prefs.getString("flAppearanceTimeValue", ""))
                binding.flAppearanceTimeFluidLevelVal.setText(prefs.getString("flAppearanceTimeFluidLevelVal", ""))
                binding.measurementTimeVal.text = prefs.getString("measurementTimeVal", "-")
                binding.measureTimeValue.setText(prefs.getString("measureTimeValue", ""))
                binding.measureTimeFluidLevelVal.setText(prefs.getString("measureTimeFluidLevelVal", ""))
                binding.measuredVolumeVal.setText(prefs.getString("measuredVolumeVal", ""))
                binding.measuredFlowrateVal.setText(prefs.getString("measuredFlowrateVal", ""))
            }
            //очистка данных
            CLEAN -> {
                binding.startupTimeValue.text = "-"
                binding.startupTimeFluidLevelVal.setText("")
                binding.flAppearanceTimeVal.text = "-"
                binding.flAppearanceTimeValue.setText("")
                binding.flAppearanceTimeFluidLevelVal.setText("")
                binding.measurementTimeVal.text = "-"
                binding.measureTimeValue.setText("")
                binding.measureTimeFluidLevelVal.setText("")
                binding.measuredVolumeVal.setText("")
                binding.measuredFlowrateVal.setText("")
                /*кроме очистки данных в полях надо так же очистить значения в файле настроек
                * i=3 - количество блоков в ВНР, поле "Уровень"*/
                val editor = prefs.edit()
                for (i in 1..3) {
                    editor.remove("wellCasingPressureType$i")
                    editor.remove("wellIntakePressureType$i")
                    editor.remove("fluidLevel$i")
                    editor.remove("casingPressure$i")
                    editor.remove("intakePressure$i")
                    editor.remove("fluidDensity$i")
                }
            }
        }
    }

    private fun adaptersInit (){
        //инициализация объектов фрагмента
        container1 = Container()
        container2 = Container()
        container3 = Container()
        container4 = Container()
        container5 = Container()
        container6 = Container()

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
    }

    private fun getCalculations(){

        if (uiFunc.statusOfAdapters) {
            /**Установка значений общей длины труб*/
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
            } else  {
                //если первый участок не заполнен, то вставляем пустышку
                binding.tubingTotalLengthVal.text = "-"
                binding.casingRecLengthVal.text = "-"
            }
            /**Рассчет данных по ВНР*/
            if (tubing1.length != null && valueDHv1.liftDepth_meter != null &&
                valueDHv1.casingLength_meter != null &&
                valueDHv1.liftDepth_meter == valueDHv1.casingLength_meter) {
                /**Расчет для раздела "Появление подачи"
                 * значение времени ожидания и уровня в разделе, а так же уровня
                 * разделе "Запуск" не должны быть пустыми*/
                if (binding.flAppearanceTimeValue.text.toString() != "" &&
                    binding.startupTimeFluidLevelVal.text.toString() != "" &&
                    binding.flAppearanceTimeFluidLevelVal.text.toString() != "" &&
                    binding.flAppearanceTimeValue.text.toString().toFloat() != 0f) {
                    val volStartup = volumeCalculation(binding.startupTimeFluidLevelVal.text.toString().toFloat())
                    val volFlAppearance = volumeCalculation(binding.flAppearanceTimeFluidLevelVal.text.toString().toFloat())
                    val volRes = volStartup!! - volFlAppearance!!
                    if (volRes >= 0f) {
                        /*устанавливаем значение откаченного объема в соответвующее поле раздела появления подачи*/
                        binding.flAppearanceCalculationVolVal.text =
                            valueDHv1.valRound(volRes).toString()
                        /*устанавливаем расчётное значение дебита в соответствующее поле раздела появления подачи*/
                        binding.flAppearanceCalculationFlowVal.text =
                            valueDHv1.valRound(
                                volRes.times(1440f)
                                    .div(binding.flAppearanceTimeValue.text.toString().toFloat())
                            ).toString()
                    } else {
                        /*при отрицательном значении уровня (т.е. при откачке был прирост уровня) в поле будет выставлено
                        * соответствующее уведомление, поскольку расчитать корректно параметры не возможно*/
                        binding.flAppearanceCalculationVolVal.text = getString(R.string.calculation_alert)
                        binding.flAppearanceCalculationFlowVal.text = getString(R.string.calculation_alert)
                    }
                } else {
                    /*если не достаточно данных для расчетов данного раздела, устанавливаем пустышки в расчетные поля раздела*/
                    binding.flAppearanceCalculationVolVal.text = "-"
                    binding.flAppearanceCalculationFlowVal.text = "-"
                }
                /**Расчет для раздела "Замер"
                 * значение полей в разделе, а так же уровня
                 * в разделе "Появление подачи" не должны быть пустыми*/
                if (binding.flAppearanceTimeFluidLevelVal.text.toString() != "" &&
                        binding.measureTimeValue.text.toString() != "" &&
                        binding.measureTimeFluidLevelVal.text.toString() != "" &&
                        binding.measuredVolumeVal.text.toString() != "" &&
                        binding.measuredFlowrateVal.text.toString() != "" &&
                        binding.measureTimeValue.text.toString().toFloat() != 0f) {
                    val volFlAppearance = volumeCalculation(binding.flAppearanceTimeFluidLevelVal.text.toString().toFloat())
                    val volMeasuring = volumeCalculation(binding.measureTimeFluidLevelVal.text.toString().toFloat())
                    val volRes = volFlAppearance!! - volMeasuring!!
                    if (volRes >= 0f) {
                        val calcFlowrate = valueDHv1.valRound(
                            volRes.times(1440f)
                                .div(binding.measureTimeValue.text.toString().toFloat())
                        )
                        val inflowRate =
                            binding.measuredFlowrateVal.text.toString().toFloat() - calcFlowrate!!
                        /*устанавливаем значение откаченного объема в соответвующее поле раздела "Замер"*/
                        binding.measureCalculationVolVal.text =
                            valueDHv1.valRound(volRes).toString()
                        /*устанавливаем расчётное значение дебита в соответствующее поле раздела появления подачи*/
                        binding.measureCalculationFlowVal.text = calcFlowrate.toString()
                        /*устанавливаем расчётное значение притока в соответствующее поле раздела замера*/
                        binding.inflowCalculationFlowVal.text =
                            valueDHv1.valRound(inflowRate).toString()
                    } else {
                        /*при отрицательном значении уровня (т.е. при откачке был прирост уровня) в поле будет выставлено
                        * соответствующее уведомление, поскольку расчитать корректно параметры не возможно*/
                        binding.measureCalculationVolVal.text = getString(R.string.calculation_alert)
                        binding.measureCalculationFlowVal.text = getString(R.string.calculation_alert)
                        binding.inflowCalculationFlowVal.text = getString(R.string.calculation_alert)
                    }
                } else {
                    /*если не достаточно данных для расчетов данного раздела, устанавливаем пустышки в расчетные поля раздела*/
                    binding.measureCalculationVolVal.text = "-"
                    binding.measureCalculationFlowVal.text = "-"
                    binding.inflowCalculationFlowVal.text = "-"
                }
            } else {
                /*если не достаточно расчетных данных для всех пунктов ВНР, устанавливаем пустышки в расчетные поля разделов*/
                binding.flAppearanceCalculationVolVal.text = "-"
                binding.flAppearanceCalculationFlowVal.text = "-"
                binding.measureCalculationVolVal.text = "-"
                binding.inflowCalculationFlowVal.text = "-"
            }
        }
    }

    /*функция для расчета объема жижкости в скважине*/
    private fun volumeCalculation (level: Float?): Float? {
        var fluidVolume: Float? = null
        //расчет производится только при уровне меньшем общей длины труб НКТ
        if (level!! <= valueDHv1.liftDepth_meter!!) {
            val tubingVolID = valueDHv1.calcVolumefromLeveltoBottomofLift(tubing1, tubing2, tubing3, level = level!!, whatDiam = Tubing.ID)
            val tubingVolOD = valueDHv1.calcVolumefromLeveltoBottomofLift(tubing1, tubing2, tubing3, level = level!!, whatDiam = Tubing.OD)
            val casingVolID = valueDHv1.calcVolumefromLeveltoBottomofLift(casing1, casing2, casing3, level = level!!, whatDiam = Tubing.ID)
            fluidVolume = (casingVolID ?: 0f) - (tubingVolOD ?: 0f) + (tubingVolID ?: 0f)
        }
        return fluidVolume
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timerController (){
        when (selectedActyvityType) {
            getString(R.string.comis_startup_title) -> {
                //записываем время запуска в соответствующее поле фрагмента
                binding.startupTimeValue.text = getCurrentDateAndTime()
                //пишем на кнопке запуска секундомера вид активности
                binding.buttonActivity.text = getString(R.string.comis_waiting_flow_appearance)
                timerServiceHelper.startTimer(getString(R.string.comis_waiting_flow_appearance))
            }
            getString(R.string.comis_flow_appear_time_title) -> {
                //записываем время запуска в соответствующее поле фрагмента
                binding.flAppearanceTimeVal.text = getCurrentDateAndTime()
                //записываем показания секундомера в соответстующее поле
                if (timerServiceHelper.timerStarted) {
                    binding.flAppearanceTimeValue.setText(getMinutesFromDouble(timerServiceHelper.time))
                }
                //пишем на кнопке запуска секундомера вид активности
                binding.buttonActivity.text = getString(R.string.comis_waiting_measurement)
                timerServiceHelper.stopTimer()
                timerServiceHelper.startTimer(getString(R.string.comis_waiting_measurement))
            }
        }
    }

    private fun getMinutesFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        return (valueDHv1.valRound(resultInt / 60f)).toString()
    }

    //функция, которая возвращает текущее время и дату
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateAndTime () : String {//ниже поля для определения времени выполнения активности
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss")
        return current.format(formatter)
    }

    override fun onPause() {
        wasPause = true
        val editor = prefs.edit()
        /**сохраняем через специальную функцию данные по ВНР*/
        commissionData(SAVE)

        /**В данном разделе сохраняются остальные значения полей данного фрагмента*/
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
        super.onPause()
    }

}