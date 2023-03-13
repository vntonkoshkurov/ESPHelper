package com.example.espspecialisthelper.ui.checkstartup

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Adapters.PickerAdapter
import com.example.espspecialisthelper.Classes.Calculations.Calculations
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.Classes.Values.ElectricalValues.ValueCS
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentCheckStartupBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.math.abs
import kotlin.math.roundToInt

class CheckStartup() : Fragment() {
    private val RIGHT = 1
    private val LEFT = 0
    private var wasPause = false //используется для исключения двойной инициализации адаптеров
    private var currentThemeTextColor: Int = 0
    private lateinit var uiFunc: UIFunctions
    private lateinit var binding: FragmentCheckStartupBinding
    private lateinit var valueCS: ValueCS //объект для хранения всех заполняемых данных
    private var statusOfAdapters: Boolean = false
    private lateinit var prefs: SharedPreferences

    //переменные с массивом данных для заполнения трещалок
    private var freqList1: MutableList<Int> = mutableListOf() //массив частот1
    private var freqList2: MutableList<Int> = mutableListOf() //массив частот2
    private var powerVoltageList2: MutableList<Int> = mutableListOf(380, 480) //массив напряжений
    private var cblCrossSectionList: MutableList<Int> =
        mutableListOf(13, 16, 21, 33, 42) //массив сечений кабеля
    private var cblCrossSectionList2: MutableList<Int> = mutableListOf(13, 16, 21, 33, 42)
    private var cblCrossSectionList3: MutableList<Int> = mutableListOf(13, 16, 21, 33, 42)
    private lateinit var powerTypeList: MutableList<String>

    //переменные с массивом холдеров
    private var freqHolderList1: MutableList<PickerAdapter.PickerItemViewHolder> = mutableListOf()
    private var freqHolderList2: MutableList<PickerAdapter.PickerItemViewHolder> = mutableListOf()
    private var cblCrossSectionHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var cblCrossSectionHolderList2: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var cblCrossSectionHolderList3: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var powerVoltageHolderList2: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()
    private var powerTypeHolderList: MutableList<PickerAdapter.PickerItemViewHolder> =
        mutableListOf()

    //Переменные помогают примагничивать позицию в RecyclerView к начальной позиции
    private val snapHelperForCblCrossSectionList: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForCblCrossSectionList2: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForCblCrossSectionList3: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForPowerVoltageList2: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForFreqList1: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForFreqList2: LinearSnapHelper = LinearSnapHelper()
    private val snapHelperForPowerTypeList: LinearSnapHelper = LinearSnapHelper()

    //адаптеры для селекторов
    private lateinit var adapterCblCrossList: PickerAdapter
    private lateinit var adapterCblCrossList2: PickerAdapter
    private lateinit var adapterCblCrossList3: PickerAdapter
    private lateinit var adapterFreqList1: PickerAdapter
    private lateinit var adapterFreqList2: PickerAdapter
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
        { holder: PickerAdapter.PickerItemViewHolder, //получаем холдер
          holderList: MutableList<PickerAdapter.PickerItemViewHolder>, //получаем ссылку на массив холдеров данного фрагмента, который передает используемый адаптер
          position: Int -> //получаем позицию холдера в списке
            if (holderList.size >= position + 1 && holderList.size != 0) { //этой позицией перезаписывается холдер в массиве, если он существует, или добавляется новый
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
            val firstVisibleIndex = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleIndex = layoutManager.findLastVisibleItemPosition()
            val middle: Int = abs(lastVisibleIndex - firstVisibleIndex) / 2 + firstVisibleIndex
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
            adapterCblCrossList3.isInit && adapterFreqList1.isInit &&
            adapterFreqList2.isInit && adapterPowerVoltageList2.isInit && adapterPowerType.isInit
        ) statusOfAdapters = true

        if (statusOfAdapters) MainActivity.vibro(requireContext())
    }

    //Функция возвращает округленное значение
    private fun valRound(value: Float?): Int? {
        return try {
            (value!!).roundToInt()
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 35..70) {
            freqList1.add(i) //создаются массивы с диапазонами частот, на основании которых будет строится RecyclerView
            freqList2.add(i)
        }
        powerTypeList =
            mutableListOf(getString(R.string.power_type_HP), getString(R.string.power_type_kvt))
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!wasPause) {
            binding = FragmentCheckStartupBinding.inflate(inflater)
            uiFunc = UIFunctions(requireContext())
            valueCS = ValueCS(binding)
            currentThemeTextColor = binding.motorTitle.currentTextColor
            adaptersInit() //инициализация адаптеров для прокручиваемых списков

            //Слушатель селектора выбора сечения кабеля №1
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
                    valueCS.cable1CrossSection = cblCrossSectionList[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //Слушатель селектора выбора сечения кабеля №2
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
                    valueCS.cable2CrossSection = cblCrossSectionList2[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //Слушатель селектора выбора сечения кабеля №3
            binding.cableCrossection3.addOnScrollListener(/* listener = */ object :
                RecyclerView.OnScrollListener() {
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
                    valueCS.cable3CrossSection = cblCrossSectionList3[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //слушатель RecyclerView для селектора выбора напряжения ТМПН
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
                    valueCS.transformetVoltageIn = powerVoltageList2[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //слушатель RecyclerView для селектора выбора базовой частоты СУ
            binding.stantionFreqBase.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.stantionFreqBase.layoutManager as LinearLayoutManager,
                        freqList1,
                        freqHolderList1
                    )
                    if (middle == prefs.getInt("stantionFreqBase", 0)) adapterFreqList1.isInit =
                        true //фиксируем окончание инициализации списка
                    valueCS.stantionFreqBase = freqList1[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //слушатель RecyclerView для селектора выбора рабочей частоты СУ
            binding.stantionFreqOper.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.stantionFreqOper.layoutManager as LinearLayoutManager,
                        freqList2,
                        freqHolderList2
                    )
                    if (middle == prefs.getInt("stantionFreqOper", 0)) adapterFreqList2.isInit =
                        true //фиксируем окончание инициализации списка
                    valueCS.stantionFreqOper = freqList2[middle].toFloat()
                    getCalculations(binding, valueCS)
                }
            })

            //слушатель RecyclerView для селектора выбора мощности ПЭД
            binding.powerType.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val middle: Int = scrollHelper(
                        binding.powerType.layoutManager as LinearLayoutManager,
                        powerTypeList,
                        powerTypeHolderList
                    )
                    if (middle == prefs.getInt("powerType", 0)) adapterPowerType.isInit =
                        true //фиксируем окончание инициализации списка
                    when (powerTypeList[middle]) {
                        "ЛС" -> valueCS.motorPowerType = "HP"
                        "HP" -> valueCS.motorPowerType = "HP"
                        "кВт" -> valueCS.motorPowerType = "KVT"
                        "kVt" -> valueCS.motorPowerType = "KVT"
                    }
                    getCalculations(binding, valueCS)
                }
            })

            /**обработка ввода данных в поле напряжения ПЭД*/
            binding.motorVoltage.addTextChangedListener(
                checkEditText(
                    binding.motorVoltage
                ) { value: Float? ->
                    valueCS.motorVoltage = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.motorVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.motorVoltage, hasFocus, false)
            }
            binding.motorVoltage.setText(prefs.getString("motorVoltage", ""))

            /**обработка ввода данных в поле мощности ПЭД*/
            binding.motorPower.addTextChangedListener(
                checkEditText(
                    binding.motorPower
                ) { value: Float? ->
                    valueCS.motorPower = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.motorPower.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.motorPower, hasFocus, false)
            }
            binding.motorPower.setText(prefs.getString("motorPower", ""))

            /**обработка ввода данных в поле тока ПЭД*/
            binding.motorCurent.addTextChangedListener(
                checkEditText(
                    binding.motorCurent
                ) { value: Float? ->
                    valueCS.motorCurrent = value
                    getCalculations(binding, valueCS)
                }
            )
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
                valueCS.cable1Length = value
                getCalculations(binding, valueCS)
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
                valueCS.cable2Length = value
                getCalculations(binding, valueCS)
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
                valueCS.cable3Length = value
                getCalculations(binding, valueCS)
            })
            binding.cableLength3.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.cableLength3, hasFocus, false)
            }
            binding.cableLength3.setText(prefs.getString("cableLength3", ""))

            /**обработка ввода данных в поле выходного напряжения СУ*/
            binding.stantionOutputVoltage.addTextChangedListener(
                checkEditText(
                    binding.stantionOutputVoltage
                ) { value: Float? ->
                    valueCS.stantionVoltageOut = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.stantionOutputVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.stantionOutputVoltage, hasFocus, false)
            }
            binding.stantionOutputVoltage.setText(prefs.getString("stantionOutputVoltage", ""))

            /**обработка ввода данных в поле мощности ТМПН*/
            binding.transOutputVoltage.addTextChangedListener(
                checkEditText(
                    binding.transOutputVoltage
                ) { value: Float? ->
                    valueCS.transformerPower = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.transOutputVoltage.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transOutputVoltage, hasFocus, false)
            }
            binding.transOutputVoltage.setText(prefs.getString("transOutputVoltage", ""))

            /**обработка ввода данных в поле импеданса ТМПН*/
            binding.transImpedans.addTextChangedListener(
                checkEditText(
                    binding.transImpedans
                ) { value: Float? ->
                    valueCS.transformerImpedance = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.transImpedans.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transImpedans, hasFocus, false)
            }
            binding.transImpedans.setText(prefs.getString("transImpedans", ""))

            /**обработка ввода данных в поле отпайки ТМПН*/
            binding.transOutputVoltage2.addTextChangedListener(
                checkEditText(
                    binding.transOutputVoltage2
                ) { value: Float? ->
                    valueCS.transformetTap = value
                    getCalculations(binding, valueCS)
                }
            )
            binding.transOutputVoltage2.setOnFocusChangeListener { _, hasFocus ->
                //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
                uiFunc.checkFocusChange(binding.transOutputVoltage2, hasFocus, false)
            }
            binding.transOutputVoltage2.setText(prefs.getString("transTap", ""))

            val toolbar: Toolbar = binding.toolbar
            toolbar.setNavigationOnClickListener {
                MainActivity.vibro(requireContext())
                activity!!.onBackPressed()
            }

            /**обработка нажатий информационных кнопок*/
            binding.infoSet1.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_2_1), requireContext())
            }
            binding.infoSet2.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_2_2), requireContext())
            }
            binding.infoSet3.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_2_3), requireContext())
            }
            binding.infoSet4.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_2_4), requireContext())
            }
            binding.infoSet5.setOnClickListener {
                MainActivity.dialogCaller(getString(R.string.info1_2_5), requireContext())
            }

            getCalculations(binding, valueCS)
        }
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun adaptersInit() {
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
        adapterFreqList1 =
            this.context?.let {
                PickerAdapter(it, freqList1, freqHolderList1, getItemHolder) { item: Int ->
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
        binding.stantionFreqBase.adapter = adapterFreqList1
        snapHelperForFreqList1.attachToRecyclerView(binding.stantionFreqBase)
        binding.stantionFreqBase.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.stantionFreqBase.onFlingListener = snapHelperForFreqList1

        //настройка RecyclerView для селектора выбора рабочей частоты СУ
        adapterFreqList2 =
            this.context?.let {
                PickerAdapter(it, freqList1, freqHolderList2, getItemHolder) { item: Int ->
                    if (item >= prefs.getInt("stantionFreqOper", 0))
                    //ниже передаем в адаптер функцию, которая позволяет корректно прокрутить список до значения из файла настроек
                    //без данной функции прокрутка работает не корректно, поскольку адаптер может еще не инициализировать необходимое значение
                    //а функция прокрутки будет пытаться сделать свое дело, при котором будет установлено не верное значение
                    //использование данной функции актуально для списков численностью большей 4-8 шт.
                    {
                        binding.stantionFreqOper.smoothScrollToPosition(
                            prefs.getInt(
                                "stantionFreqOper",
                                0
                            )
                        )
                    } else {
                        binding.stantionFreqOper.smoothScrollToPosition(item)
                    }
                }
            }!!
        binding.stantionFreqOper.adapter = adapterFreqList2
        snapHelperForFreqList2.attachToRecyclerView(binding.stantionFreqOper)
        binding.stantionFreqOper.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.stantionFreqOper.onFlingListener = snapHelperForFreqList2

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
        binding.transformerLsv.smoothScrollToPosition(prefs.getInt("transformerLsv", 0))

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
    private fun getCalculations(binding: FragmentCheckStartupBinding, valueCS: ValueCS) {

        val calculations: Calculations = Calculations(valueCS)
        valueCS.stantionPowerReserve = 0f
        valueCS.transformerPowerReserve = 0f
        binding.transformetTapResVal.text = valRound(calculations.voltageOutput)?.toString() ?: "-"
        binding.actualVoltage1.text = valRound(valueCS.stantionFreqOper)?.toString() ?: "-"
        binding.actualVoltage3.text = valRound(calculations.actualVoltageOut)?.toString() ?: "-"
        //вывод графика
        chartBuild()
    }

    //функция вывода графика
    private fun chartBuild() {
        val chart: LineChart = binding.chart1
        val voltageFact: MutableList<Entry> =
            mutableListOf() //график напряжения с вычетом потерь, которое приходит на ПЭД
        val voltageCalc: MutableList<Entry> = mutableListOf() //график напряжения ПЭД расчетного
        val customLimitChart: MutableList<Entry> =
            mutableListOf() //график минимального напряжения, необходимого для запуска ПЭД (прямая линия)
        val highliteVal: MutableList<Entry> = mutableListOf() //точка пересечения графиков
        var minVal = 0f
        var strMinVal = 0

        chart.setScaleYEnabled(false) //отключаем масштабирование графика по вертикали
        chart.description.isEnabled = false //отключаем вывод описания графика
        val legend: Legend = chart.legend
        legend.isEnabled = false // отключаем легенду, она будет своя

        //заполняем массивы значениями из сводного массива
        for (str in 0..valueCS.startupData.size - 1) {
            voltageCalc.add(Entry(valueCS.startupData[str][1], valueCS.startupData[str][3]))
            voltageFact.add(Entry(valueCS.startupData[str][1], valueCS.startupData[str][4]))
            customLimitChart.add(
                Entry(
                    valueCS.startupData[str][1],
                    (valueCS.startupData[99][3] * 55f / 100f)
                )
            )
            //ниже ищется минимальная разница между напряжениями
            if (str == 0) {
                minVal = valueCS.startupData[str][7]
                strMinVal = str
            } else if (minVal > valueCS.startupData[str][7]) {
                minVal = valueCS.startupData[str][7]
                strMinVal = str
            }
        }

        highliteVal.add(Entry(valueCS.startupData[strMinVal][1], valueCS.startupData[strMinVal][3]))
        //формируются датасеты графиков на основании заполненных массивов
        val dataset: LineDataSet = LineDataSet(voltageCalc, "1")
        val dataset2: LineDataSet = LineDataSet(voltageFact, "2")
        val limitChart: LineDataSet = LineDataSet(customLimitChart, "3")
        val pointChart: LineDataSet = LineDataSet(highliteVal, "4")

        //ниже идет кастомизация графиков
        //настраивается внешний вид графика напряжения ПЭД
        dataset.setDrawFilled(false)
        dataset.setColor(Color.GREEN)
        dataset.isHighlightEnabled = false
        dataset.setDrawCircles(false)
        dataset.lineWidth = 2f
        //настраивается внешний вид графика напряжения ПЭД с вычетом потерь
        dataset2.setDrawFilled(false)
        dataset2.setColor(Color.BLUE)
        dataset2.isHighlightEnabled = false
        dataset2.setDrawCircles(false)
        dataset2.lineWidth = 2f
        //настраивается внешний вид точки пересечения
        pointChart.setCircleColors(Color.RED)
        pointChart.circleHoleColor = Color.YELLOW
        pointChart.circleRadius = 5f
        pointChart.circleHoleRadius = 3f
        pointChart.setColor(Color.YELLOW)
        //настраивается внешний вид графика ограничения
        limitChart.isHighlightEnabled = true
        limitChart.setDrawCircles(false)
        limitChart.setColor(Color.RED)
        limitChart.setDrawFilled(true)
        limitChart.fillColor = Color.RED

        //все датасеты (графики) собираются в один массив
        val datasets: MutableList<ILineDataSet> = mutableListOf()
        datasets.add(dataset)
        datasets.add(dataset2)
        datasets.add(limitChart)
        datasets.add(pointChart)

        val lineData: LineData = LineData(datasets)
        //все графики передаются на вывод
        chart.data = lineData
        chart.setTouchEnabled(false)
        chart.axisLeft.spaceBottom = 0f
        chart.axisRight.isEnabled = false
        chart.xAxis.labelCount = 5

        chart.invalidate()
        //ниже выводятся расчетные данные в необходимые поля фрагмента
        binding.actualVoltage.text = valRound(valueCS.startupData[strMinVal][3]).toString()
        binding.startupVoltage.text = valRound(valueCS.startupData[99][3] * 55f / 100f).toString()

        if (valueCS.startupData[strMinVal][3] >= (valueCS.startupData[99][3] * 55f / 100f)) {
            binding.graf1Txt6.text = getString(R.string.startup_res_pos)
            binding.graf1Txt6.setTextColor(Color.GREEN)
        } else {
            binding.graf1Txt6.text = getString(R.string.startup_res_neg)
            binding.graf1Txt6.setTextColor(Color.RED)
        }
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
        var i = posInMutableList(binding.stantionFreqBase.layoutManager as LinearLayoutManager)
        editor.putInt(
            "stantionFreqOper",
            posInMutableList(binding.stantionFreqOper.layoutManager as LinearLayoutManager)
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
        editor.putString("transTap", binding.transOutputVoltage2.text.toString()).apply()
    }

    override fun onResume() {
        super.onResume()
        getCalculations(binding, valueCS)
    }

}