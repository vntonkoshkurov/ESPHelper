package com.example.espspecialisthelper.ui.electrical

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Adapters.PickerAdapter
import com.example.espspecialisthelper.Classes.Calculations.Calculations
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.Classes.Values.ElectricalValues.ValuesES
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentElectricalCalcBinding

class ElectricalCalc() : Fragment() {
    private val RIGHT = 1
    private val LEFT = 0
    private var currentThemeTextColor: Int = 0
    private lateinit var uiFunc: UIFunctions
    private var wasPause = false //используется для исключения двойной инициализации адаптеров
    private lateinit var binding: FragmentElectricalCalcBinding
    private lateinit var valuesES: ValuesES
    private var statusOfAdapters: Boolean = false
    private lateinit var prefs: SharedPreferences

    //переменные с массивом данных для заполнения трещалок
    private var freqList: MutableList<Int> = mutableListOf()

    //private var powerVoltageList: MutableList<Int> = mutableListOf(380, 480)
    private var powerVoltageList2: MutableList<Int> = mutableListOf(380, 480)
    private var cblCrossSectionList: MutableList<Int> = mutableListOf(13, 16, 21, 33, 42)
    private var cblCrossSectionList2: MutableList<Int> = mutableListOf(13, 16, 21, 33, 42)
    private var cblCrossSectionList3: MutableList<Int> = mutableListOf(13, 16, 21, 33, 42)
    private lateinit var powerTypeList: MutableList<String>

    //переменные с массивом холдеров
    private var freqHolderList: MutableList<PickerAdapter.PickerItemViewHolder> = mutableListOf()
    private var cblCrossSectionHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var cblCrossSectionHolderList2: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var cblCrossSectionHolderList3: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()

    //private var powerVoltageHolderList: MutableList<PickerAdapter.PickerItemViewHolder> = mutableListOf()
    private var powerVoltageHolderList2: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var powerTypeHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()

    //Переменные помогают примагничивать позицию в RecyclerView к начальной позиции
    private val snapHelperForCblCrossSectionList: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForCblCrossSectionList2: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForCblCrossSectionList3: LinearSnapHelper = LinearSnapHelper()

    //private val snapHelperForPowerVoltageList: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForPowerVoltageList2: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForFreqList: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForPowerTypeList: LinearSnapHelper = LinearSnapHelper()

    //адаптеры для селекторов
    private lateinit var adapterCblCrossList: PickerAdapter
    private lateinit var adapterCblCrossList2: PickerAdapter
    private lateinit var adapterCblCrossList3: PickerAdapter
    private lateinit var adapterFreqList: PickerAdapter
    private lateinit var adapterPowerVoltageList2: PickerAdapter
    private lateinit var adapterPowerType: PickerAdapter

    /** Ниже создается лямбда функция для заполнения массива объектов списк
    функция используется в качестве коллбека. Передается в адаптер прокручиваемых списков, чтобы заполнять массив
    из холдеров. Это необходимо для чтения выбранных данных в холдерах.
     */
    private var getItemHolder: (
        PickerAdapter.PickerItemViewHolder,
        MutableList<PickerAdapter.PickerItemViewHolder>,
        Int
    ) -> Unit =
        { holder: PickerAdapter.PickerItemViewHolder,
          holderList: MutableList<PickerAdapter.PickerItemViewHolder>,
          position: Int ->
            if (holderList.size >= position + 1 && holderList.size != 0) {
                holderList[position] = holder
            } else holderList.add(holder)
        }

    /** функция используется совместно со scrollHelper, который создает эффект примагничивания (притягивания)
    элемента (холдера) к заданной позиции в списке RecyclerView к
     */
    private var scrollHelper: (
        LinearLayoutManager,
        MutableList<*>,
        MutableList<PickerAdapter.PickerItemViewHolder>
    ) -> Int =
        { layoutManager: LinearLayoutManager, mutableList: MutableList<*>, holderList: MutableList<PickerAdapter.PickerItemViewHolder> ->
            val middle = posInMutableList(layoutManager)
            //идет вычисление сдернего элемента в прокручиваемом списке
            //средний элемент подкрашивается в красный цвет, крайние подкрашиваются в черный
            if (listControl(
                    middle,
                    holderList,
                    LEFT
                )
            ) holderList[middle - 1].getBind().textView.setTextColor(currentThemeTextColor)
            if (middle >= 0 && middle <= holderList.size - 1) {
                if (holderList[middle].getBind().textView.currentTextColor != Color.RED) vibrator()
                holderList[middle].getBind().textView.setTextColor(Color.RED)
            }
            if (listControl(
                    middle,
                    holderList,
                    RIGHT
                )
            ) holderList[middle + 1].getBind().textView.setTextColor(currentThemeTextColor)

            middle
        }

    /** лямбда-функция помогающая избежать выйти за пределы размера массива с данными для RecyclerView
    функция получает вычесленную позицию в центре прокручеваемого виджета.
    если вычесленное значение является крайним значением в принимаемом массиве, то
    функция вернет ложное булевое значение.
    сторона (левая, правая) по которой нужно провести проверку, так же принимается данной
    функцией последним значением
     */
    private var listControl = { pos: Int, mList: MutableList<*>,
                                side: Int ->
        when (pos) {
            0 -> side != LEFT
            mList.size - 1 -> side != RIGHT
            else -> {
                !(pos < 0 || pos > mList.size - 1)
            }
        }
    }

    /** функция возвращает значение элемента в массиве, который соответствует позиции в RecyclerView
     */
    private var posInMutableList: (LinearLayoutManager) -> Int =
        { layoutManager: LinearLayoutManager ->
            var firstVisibleIndex = layoutManager.findFirstVisibleItemPosition()
            var lastVisibleIndex = layoutManager.findLastVisibleItemPosition()
            var middle: Int = Math.abs(lastVisibleIndex - firstVisibleIndex) / 2 + firstVisibleIndex
            middle
        }

    /** Лямбда обрабатывающая изменение текста в поля
    Главная задача - чтобы были введены в правильном формате числа.
    Не должно быть нулевого значения.
    Если вводится нулевое значение, то автоматически добавляется разедлитель целой части от десятичной
    Нулевая целая часть удаляется автоматически с точкой-разделителем от десятичной части
     */
    private var checkEditText: (EditText, (Float?) -> Unit) -> TextWatcher =
        { editText: EditText, valueHelper: (Float?) -> Unit ->
            object : TextWatcher {
                var _start = 0
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    _start =
                        start //фиксируем позицию курсора, с которой началось редактирование поля
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(s: Editable?) {
                    var _s: String? = s.toString()
                    if ((_s?.length ?: 0) != 0) {
                        if (_s!!.length == 2) { //если введен ноль, то после него автоматически добавляется точка
                            if (_s!![0] == '0' && _s!![1] != '.') {
                                editText.setText(_s!![0] + "." + _s!![1])
                                editText.setSelection(_s.length + 1)
                                _s = editText.text.toString()
                            }
                        } else if (_s == ".") { //если введена точка, то перед ней автоматически добавляется ноль
                            _s = "0."
                            editText.setText(_s)
                            editText.setSelection(_s.length)
                        }
                        valueHelper(_s?.toFloat())
                    } else valueHelper(null)
                }
            }
        }

    //функция для реализации вибрации при каких-либо действиях. Используется при прокрутке селекторов
    private fun vibrator() {

        if (adapterCblCrossList.isInit && adapterCblCrossList2.isInit &&
            adapterCblCrossList3.isInit && adapterFreqList.isInit &&
            adapterPowerVoltageList2.isInit && adapterPowerType.isInit
        ) statusOfAdapters = true

        if (statusOfAdapters) MainActivity.vibro(requireContext())
    }

    //Функция возвращает округленное значение
    private fun valRound(value: Float?): Int? {
        return try {
            Math.round(value!!)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 35..70) freqList.add(i)
        powerTypeList =
            mutableListOf(getString(R.string.power_type_HP), getString(R.string.power_type_kvt))
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = activity!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!wasPause) {
            binding = FragmentElectricalCalcBinding.inflate(inflater)
            currentThemeTextColor = binding.motorTitle.currentTextColor
            uiFunc = UIFunctions(requireContext())
            valuesES = ValuesES(binding)
            adaptersInit()

            /**Слушатель для селектора выбора сечения кабеля №1*/
            binding.cableCrossection.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.cableCrossection.layoutManager as LinearLayoutManager,
                        cblCrossSectionList,
                        cblCrossSectionHolderList
                    )
                    if (middle == prefs.getInt("cableCrossection", 0)) adapterCblCrossList.isInit =
                        true //фиксируем окончание инициализации списка
                    valuesES.cable1CrossSection = cblCrossSectionList[middle].toFloat()
                    getCalculations(binding, valuesES)
                }
            })

            /**Слушатель для селектора выбора сечения кабеля №1*/
            binding.cableCrossection2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.cableCrossection2.layoutManager as LinearLayoutManager,
                        cblCrossSectionList2,
                        cblCrossSectionHolderList2
                    )
                    if (middle == prefs.getInt(
                            "cableCrossection2",
                            0
                        )
                    ) adapterCblCrossList2.isInit =
                        true //фиксируем окончание инициализации списка
                    valuesES.cable2CrossSection = cblCrossSectionList2[middle].toFloat()
                    getCalculations(binding, valuesES)
                }
            })

            /**Слушатель для селектора выбора сечения кабеля №3*/
            binding.cableCrossection3.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.cableCrossection3.layoutManager as LinearLayoutManager,
                        cblCrossSectionList3,
                        cblCrossSectionHolderList3
                    )
                    if (middle == prefs.getInt(
                            "cableCrossection3",
                            0
                        )
                    ) adapterCblCrossList3.isInit =
                        true //фиксируем окончание инициализации списка
                    valuesES.cable3CrossSection = cblCrossSectionList3[middle].toFloat()
                    getCalculations(binding, valuesES)
                }
            })

            /**Слушатель для селектора выбора напряжения ТМПН*/
            binding.transformerLsv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.transformerLsv.layoutManager as LinearLayoutManager,
                        powerVoltageList2,
                        powerVoltageHolderList2
                    )
                    if (middle == prefs.getInt(
                            "transformerLsv",
                            0
                        )
                    ) adapterPowerVoltageList2.isInit =
                        true //фиксируем окончание инициализации списка
                    valuesES.transformetVoltageIn = powerVoltageList2[middle].toFloat()
                    getCalculations(binding, valuesES)
                }
            })

            /**Слушатель для селектора выбора базовой частоты*/
            binding.stantionFreqBase.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.stantionFreqBase.layoutManager as LinearLayoutManager,
                        freqList,
                        freqHolderList
                    )
                    if (middle == prefs.getInt("stantionFreqBase", 0)) adapterFreqList.isInit =
                        true //фиксируем окончание инициализации списка
                    valuesES.stantionFreqBase = freqList[middle].toFloat()
                    getCalculations(binding, valuesES)
                }
            })

            /**Слушатель для селектора выбора мощности ПЭД*/
            binding.powerType.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    var middle: Int
                    middle = scrollHelper(
                        binding.powerType.layoutManager as LinearLayoutManager,
                        powerTypeList,
                        powerTypeHolderList
                    )
                    if (middle == prefs.getInt("powerType", 0)) adapterPowerType.isInit =
                        true //фиксируем окончание инициализации списка
                    when (powerTypeList[middle]) {
                        "ЛС" -> valuesES.motorPowerType = "HP"
                        "HP" -> valuesES.motorPowerType = "HP"
                        "кВт" -> valuesES.motorPowerType = "KVT"
                        "kVt" -> valuesES.motorPowerType = "KVT"
                    }
                    getCalculations(binding, valuesES)
                }
            })

            /**обработка ввода данных в поле напряжения ПЭД*/
            binding.motorVoltage.addTextChangedListener(checkEditText(binding.motorVoltage) { value: Float? ->
                valuesES.motorVoltage = value
                getCalculations(binding, valuesES)
            })
            binding.motorVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.motorVoltage, hasFocus, false)
            }
            binding.motorVoltage.setText(prefs.getString("motorVoltage", ""))

            /**обработка ввода данных в поле мощности ПЭД*/
            binding.motorPower.addTextChangedListener(checkEditText(binding.motorPower) { value: Float? ->
                valuesES.motorPower = value
                getCalculations(binding, valuesES)
            })
            binding.motorPower.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.motorPower, hasFocus, false)
            }
            binding.motorPower.setText(prefs.getString("motorPower", ""))

            /**обработка ввода данных в поле тока ПЭД*/
            binding.motorCurent.addTextChangedListener(checkEditText(binding.motorCurent) { value: Float? ->
                valuesES.motorCurrent = value
                getCalculations(binding, valuesES)
            })
            binding.motorCurent.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.motorCurent, hasFocus, false)
            }
            binding.motorCurent.setText(prefs.getString("motorCurent", ""))

            /**обработка ввода данных в поле длины кабеля №1*/
            binding.cableLength.addTextChangedListener(checkEditText(binding.cableLength) { value: Float? ->
                /*Ниже, в зависимости от наличия или отсуствия данных будут активироваться остальные участки кабеля
                 * для ввода данных*/
                binding.cableLength2.isEnabled = binding.cableLength.length() != 0 //если поле пустое, то второе не может быть заполнено и наоборот
                if (binding.cableLength.length() == 0) {
                    binding.cableLength3.isEnabled = false
                } else if (binding.cableLength2.length() != 0) binding.cableLength3.isEnabled = value != null
                valuesES.cable1Length = value
                getCalculations(binding, valuesES)
            })
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 2-го и 3-го участка
            binding.cableLength.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.cableLength, hasFocus, false)
                if (!hasFocus) {
                    if (binding.cableLength.length() == 0) {
                        binding.cableLength2.text = null
                        binding.cableLength3.text = null
                    }
                }
            }
            binding.cableLength.setText(prefs.getString("cableLength", ""))

            /**обработка ввода данных в поле длины кабеля №2*/
            binding.cableLength2.addTextChangedListener(checkEditText(binding.cableLength2) { value: Float? ->
                /*Ниже, в зависимости от наличия или отсуствия данных будут активироваться остальные участки кабеля
                    * для ввода данных*/
                binding.cableLength3.isEnabled =
                    binding.cableLength2.length() != 0 //если поле пустое, то третье не может быть заполнено и наоборот
                valuesES.cable2Length = value
                getCalculations(binding, valuesES)
            })
            //при потере фокуса и пустом значении необходимо удалить данные из поля ввода 3-го участка
            binding.cableLength2.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.cableLength2, hasFocus, false)
                if (!hasFocus) {
                    if (binding.cableLength2.length() == 0) binding.cableLength3.text = null
                }
            }
            binding.cableLength2.setText(prefs.getString("cableLength2", ""))

            /**обработка ввода данных в поле длины кабеля №3*/
            binding.cableLength3.addTextChangedListener(checkEditText(binding.cableLength3) { value: Float? ->
                valuesES.cable3Length = value
                getCalculations(binding, valuesES)
            })
            binding.cableLength3.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.cableLength3, hasFocus, false)
            }
            binding.cableLength3.setText(prefs.getString("cableLength3", ""))

            /**обработка ввода данных в поле выходного напряжения СУ*/
            binding.stantionOutputVoltage.addTextChangedListener(checkEditText(binding.stantionOutputVoltage) { value: Float? ->
                valuesES.stantionVoltageOut = value
                getCalculations(binding, valuesES)
            })
            binding.stantionOutputVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.stantionOutputVoltage, hasFocus, false)
            }
            binding.stantionOutputVoltage.setText(prefs.getString("stantionOutputVoltage", ""))

            /**обработка ввода данных в поле мощности ТМПН*/
            binding.transOutputVoltage.addTextChangedListener(checkEditText(binding.transOutputVoltage) { value: Float? ->
                valuesES.transformerPower = value
                getCalculations(binding, valuesES)
            })
            binding.transOutputVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transOutputVoltage, hasFocus, false)
            }
            binding.transOutputVoltage.setText(prefs.getString("transOutputVoltage", ""))

            /**обработка ввода данных в поле импеданса ТМПН*/
            binding.transImpedans.addTextChangedListener(checkEditText(binding.transImpedans) { value: Float? ->
                valuesES.transformerImpedance = value
                getCalculations(binding, valuesES)
            })
            binding.transImpedans.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transImpedans, hasFocus, false)
            }
            binding.transImpedans.setText(prefs.getString("transImpedans", ""))

            /**обработка ввода данных в поле запаса мощности ТМПН*/
            binding.transPowerReserve.addTextChangedListener(checkEditText(binding.transPowerReserve) { value: Float? ->
                valuesES.transformerPowerReserve = value
                getCalculations(binding, valuesES)
            })
            binding.transPowerReserve.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transPowerReserve, hasFocus, true)
            }
            binding.transPowerReserve.setText(prefs.getString("transPowerReserve", ""))

            /**обработка ввода данных в поле запаса мощности СУ*/
            binding.stantionPowerReserve.addTextChangedListener(checkEditText(binding.stantionPowerReserve) { value: Float? ->
                valuesES.stantionPowerReserve = value
                getCalculations(binding, valuesES)
            })
            binding.stantionPowerReserve.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.stantionPowerReserve, hasFocus, true)
            }
            binding.stantionPowerReserve.setText(prefs.getString("stantionPowerReserve", ""))

            val toolbar: Toolbar = binding.toolbar
            toolbar.setNavigationOnClickListener {
                MainActivity.vibro(requireContext())
                activity!!.onBackPressed()
            }

            /**обработка нажатий информационных кнопок*/
            binding.infoSet1.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_1_1), requireContext())
            }
            binding.infoSet2.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_1_2), requireContext())
            }
            binding.infoSet3.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_1_3), requireContext())
            }
            binding.infoSet4.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_1_4), requireContext())
            }
            binding.infoSet5.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_1_5), requireContext())
            }

            getCalculations(binding, valuesES)
        }
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun adaptersInit() {
        //создаем объекты адаптеров для селекторов

        //настройка RecyclerView для селектора выбора сечения кабеля №1
        adapterCblCrossList = this.context?.let {
            PickerAdapter(
                it,
                cblCrossSectionList,
                cblCrossSectionHolderList,
                getItemHolder
            ) { item: Int ->
                if (item >= prefs.getInt("cableCrossection", 0))
                //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                //использование данной функции актуально для списков численностью большей 4-8 шт.
                {
                    binding.cableCrossection.smoothScrollToPosition(
                        prefs.getInt(
                            "cableCrossection",
                            0
                        )
                    )
                } else {
                    binding.cableCrossection.smoothScrollToPosition(item)
                }
            }
        }!!
        binding.cableCrossection.adapter = adapterCblCrossList
        snapHelperForCblCrossSectionList.attachToRecyclerView(binding.cableCrossection)
        binding.cableCrossection.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.cableCrossection.onFlingListener = snapHelperForCblCrossSectionList
        binding.cableCrossection.smoothScrollToPosition(prefs.getInt("cableCrossection", 0));

        //настройка RecyclerView для селектора выбора сечения кабеля №2
        adapterCblCrossList2 = this.context?.let {
            PickerAdapter(
                it,
                cblCrossSectionList2,
                cblCrossSectionHolderList2,
                getItemHolder
            ) { item: Int ->
                if (item >= prefs.getInt("cableCrossection2", 0))
                //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                //использование данной функции актуально для списков численностью большей 4-8 шт.
                {
                    binding.cableCrossection2.smoothScrollToPosition(
                        prefs.getInt(
                            "cableCrossection2",
                            0
                        )
                    )
                } else {
                    binding.cableCrossection2.smoothScrollToPosition(item)
                }
            }
        }!!
        binding.cableCrossection2.adapter = adapterCblCrossList2
        snapHelperForCblCrossSectionList2.attachToRecyclerView(binding.cableCrossection2)
        binding.cableCrossection2.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.cableCrossection2.onFlingListener = snapHelperForCblCrossSectionList2
        binding.cableCrossection2.smoothScrollToPosition(prefs.getInt("cableCrossection2", 0));

        //настройка RecyclerView для селектора выбора сечения кабеля №3
        adapterCblCrossList3 = this.context?.let {
            PickerAdapter(
                it,
                cblCrossSectionList3,
                cblCrossSectionHolderList3,
                getItemHolder
            ) { item: Int ->
                if (item >= prefs.getInt("cableCrossection3", 0))
                //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                //использование данной функции актуально для списков численностью большей 4-8 шт.
                {
                    binding.cableCrossection3.smoothScrollToPosition(
                        prefs.getInt(
                            "cableCrossection3",
                            0
                        )
                    )
                } else {
                    binding.cableCrossection3.smoothScrollToPosition(item)
                }
            }
        }!!
        binding.cableCrossection3.adapter = adapterCblCrossList3
        snapHelperForCblCrossSectionList3.attachToRecyclerView(binding.cableCrossection3)
        binding.cableCrossection3.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.cableCrossection3.onFlingListener = snapHelperForCblCrossSectionList3
        binding.cableCrossection3.smoothScrollToPosition(prefs.getInt("cableCrossection3", 0));

        //настройка RecyclerView для селектора выбора базовой частоты СУ
        adapterFreqList = this.context?.let {
            PickerAdapter(it, freqList, freqHolderList, getItemHolder) { item: Int ->
                if (item >= prefs.getInt("stantionFreqBase", 0))
                //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                //использование данной функции актуально для списков численностью большей 4-8 шт.
                {
                    binding.stantionFreqBase.smoothScrollToPosition(
                        prefs.getInt(
                            "stantionFreqBase",
                            0
                        )
                    )
                } else {
                    binding.stantionFreqBase.smoothScrollToPosition(item)
                }
            }

        }!!
        binding.stantionFreqBase.adapter = adapterFreqList
        snapHelperForFreqList.attachToRecyclerView(binding.stantionFreqBase)
        binding.stantionFreqBase.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.stantionFreqBase.onFlingListener = snapHelperForFreqList

        //настройка RecyclerView для селектора выбора напряжения ТМПН
        adapterPowerVoltageList2 = this.context?.let {
            PickerAdapter(
                it,
                powerVoltageList2,
                powerVoltageHolderList2,
                getItemHolder
            )
        }!!
        binding.transformerLsv.adapter = adapterPowerVoltageList2
        snapHelperForPowerVoltageList2.attachToRecyclerView(binding.transformerLsv)
        binding.transformerLsv.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.transformerLsv.onFlingListener = snapHelperForPowerVoltageList2
        binding.transformerLsv.smoothScrollToPosition(prefs.getInt("transformerLsv", 0));

        //настройка RecyclerView для селектора выбора мощности ПЭД
        adapterPowerType = this.context?.let {
            PickerAdapter(
                it,
                powerTypeList,
                powerTypeHolderList,
                getItemHolder
            )
        }!!
        binding.powerType.adapter = adapterPowerType
        snapHelperForPowerTypeList.attachToRecyclerView(binding.powerType)
        binding.powerType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.powerType.onFlingListener = snapHelperForPowerTypeList
        binding.powerType.smoothScrollToPosition(prefs.getInt("powerType", 0));
    }

    //функция для проведения всех вычеслений и заполнений полей фрагмента полученными значениями
    private fun getCalculations(binding: FragmentElectricalCalcBinding, valuesES: ValuesES) {

        val calculations: Calculations = Calculations(valuesES)
        binding.stantionCurrentRes.text =
            valRound(calculations.currentStantionRecommended)?.toString() ?: "-"
        binding.stantionOutCurrentRes.text =
            valRound(calculations.currentStantionOutStantion)?.toString() ?: "-"
        binding.transformetTapResVal.text = valRound(calculations.voltageOutput)?.toString() ?: "-"
        binding.transformetPowerResVal.text =
            valRound(calculations.powerKVATransformerRecommended)?.toString() ?: "-"
        binding.cableVoltageDropResVal.text =
            valRound(calculations.cableVoltageDrop)?.toString() ?: "-"
        binding.motorCurentResVal.text = valRound(valuesES.motorCurrent)?.toString() ?: "-"
        binding.motorPowerFreqVal2.text = valRound(valuesES.stantionFreqBase)?.toString() ?: "-"
        binding.motorPowerVal.text = valRound(calculations.motorPowerHP)?.toString() ?: "-"
        binding.motorPower2FreqVal2.text = valRound(valuesES.stantionFreqBase)?.toString() ?: "-"
        binding.motorPower2Val.text = valRound(calculations.motorPowerkVT)?.toString() ?: "-"
        binding.motorVoltageFreqVal1.text = valRound(valuesES.stantionFreqBase)?.toString() ?: "-"
        binding.motorVoltageVal.text = valRound(calculations.motorVoltage)?.toString() ?: "-"
        binding.cableTotalLengthResVal.text =
            valRound(calculations.cableTotalLength)?.toString() ?: "-"

    }

    override fun onPause() {
        super.onPause()
        wasPause = true //после возобновления отображения фрагмента по данному значению будет определяться делать повторную инициализацию адаптеров или нет
        val editor = prefs.edit()
        editor.putInt(
            "cableCrossection",
            posInMutableList(binding.cableCrossection.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "cableCrossection2",
            posInMutableList(binding.cableCrossection2.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "cableCrossection3",
            posInMutableList(binding.cableCrossection3.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "transformerLsv",
            posInMutableList(binding.transformerLsv.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "stantionFreqBase",
            posInMutableList(binding.stantionFreqBase.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "powerType",
            posInMutableList(binding.powerType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putString("motorVoltage", binding.motorVoltage.text.toString()).apply()
        editor.putString("motorPower", binding.motorPower.text.toString()).apply()
        editor.putString("motorCurent", binding.motorCurent.text.toString()).apply()
        editor.putString("cableLength", binding.cableLength.text.toString()).apply()
        editor.putString("cableLength2", binding.cableLength2.text.toString()).apply()
        editor.putString("cableLength3", binding.cableLength3.text.toString()).apply()
        editor.putString("stantionOutputVoltage", binding.stantionOutputVoltage.text.toString())
            .apply()
        editor.putString("transOutputVoltage", binding.transOutputVoltage.text.toString()).apply()
        editor.putString("transImpedans", binding.transImpedans.text.toString()).apply()
        editor.putString("transPowerReserve", binding.transPowerReserve.text.toString()).apply()
        editor.putString("stantionPowerReserve", binding.stantionPowerReserve.text.toString())
            .apply()
    }

    override fun onResume() {
        super.onResume()
        getCalculations(binding, valuesES)
    }

}
