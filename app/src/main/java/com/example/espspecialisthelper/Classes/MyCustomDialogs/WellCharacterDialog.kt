package com.example.espspecialisthelper.Classes.MyCustomDialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.Classes.Calculations.Container
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.Classes.Values.DownholeValues.ValueDHv1
import com.example.espspecialisthelper.Classes.Well.Well
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentWellCharacterDialogBinding

/**Конструктор класса принимает три значения:
 * 1 - объект со всеми величинами фрагмента
 * 2 - поле ввода текста, в которое необходимо подставить значение
 * 3 - метка поля ввода текста, чтобы при повторном вызове восстанавливать введенные
 *      погружные характеристики*/
class WellCharacterDialog (val valueDHv1: ValueDHv1, val editText: EditText, val label: Int) : DialogFragment() {

    private lateinit var binding: FragmentWellCharacterDialogBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var uiFunc: UIFunctions
    private lateinit var well: Well
    private lateinit var container1: Container //контейнер селектора давления в затрубе
    private lateinit var container2: Container //контейнер селектора давления на приеме
    private val key = if (editText.text.toString() == "")  {
        ""
    } else {
        label.toString()
    }

    //функция для ожидания инициализации всех адаптеров
    private fun waitAdaptersInit() {
        if ((container1.adapter?.isInit == true
                    && container2.adapter?.isInit == true)
            && !uiFunc.statusOfAdapters
        ) {
            uiFunc.statusOfAdapters = true
        }
    }

    //метод по открытию диалога
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            builder.setTitle(getString(R.string.well_title))
            //устанавливаем в диалог кастомный фрагмент для заполнения необходимых данных
            builder.setView(сreateDialogView(inflater))
                // Add action buttons
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        /*принятие значения будет только при не пустом введенном значении
                        * и уровне, не превышающем длину труб, которая определяется по
                        * отсутствию надписи во вью диалогового фрагмента*/
                        val str = binding.wellFluidLevel.text.toString()
                        val textViewIsVisible = binding.levelAlertTitle.visibility != View.GONE
                        if (str != "" && !textViewIsVisible) {
                            editText.setText(
                                valueDHv1.valRound(str.toFloat()).toString()
                            )
                            saveWelldData()
                        } else {
                            /*если введено не корректное значение вывождим уведомление*/
                            MainActivity.dialogCaller(getText(R.string.enter_alert).toString(), requireContext())
                        }
                    })
                .setNegativeButton(getString(R.string.cancel_title),
                    DialogInterface.OnClickListener { dialog, id ->
                        saveWelldData()
                        getDialog()!!.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //метод для формирования и заполнения кастомного фрагмента необходимыми данными
    private fun сreateDialogView(
        inflater: LayoutInflater
    ): View {
        binding = FragmentWellCharacterDialogBinding.inflate(inflater)
        uiFunc = UIFunctions(requireContext())
        uiFunc.currentThemeTextColor = binding.wellAdditionalValTitle.currentTextColor
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        adaptersInit()

        //БЛОК ОБРАБОТКИ ПРОКРУЧИВАЕМЫХ ПОЛЕЙ ФРАГМЕНТА
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
                if (middle == prefs.getInt("wellCasingPressureType$key", 0) && !uiFunc.statusOfAdapters
                ) {
                    container1.adapter!!.isInit = true
                    waitAdaptersInit()
                } //фиксируем окончание инициализации списка
                //по вычесленному выделенному индексу с прокручиваемом списке определяем
                //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                //преобразуем в отдельный список и по индексу выбираем необходимое значение
                well.casingPressure_type = well.pressureTypeList.keys.toMutableList()[middle]
                valueDHv1.casigPressure_Pa = well.getCasingPressure_Pa()
                //getCalculations()
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
                if (middle == prefs.getInt("wellIntakePressureType$key", 0) && !uiFunc.statusOfAdapters
                ) {
                    container2.adapter!!.isInit = true
                    waitAdaptersInit()
                } //фиксируем окончание инициализации списка
                //по вычесленному выделенному индексу с прокручиваемом списке определяем
                //значение в перечне ключей словаря величин по трубам. Т.е. из словаря все значения ключей
                //преобразуем в отдельный список и по индексу выбираем необходимое значение
                well.intakePressure_type = well.pressureTypeList.keys.toMutableList()[middle]
                valueDHv1.intakePressure_Pa = well.getIntakePressure_Pa()
                //getCalculations()
                //при изменении значения автоматически пересчитываем значение уровня жидкости в скважине
                valueDHv1.calcFluidLevel()?.let {
                    binding.wellFluidLevel.setText(
                        valueDHv1.valRound(it).toString()
                    )
                }
            }
        })

        //БЛОК ОБРАБОТКИ ТЕКСТОВЫХ ПОЛЕЙ ФРАГМЕНТА

        //обработка ввода данных в поле уровня жидкости
        binding.wellFluidLevel.addTextChangedListener(
            uiFunc.checkEditText(
                binding.wellFluidLevel
            ) { value: Float? ->
                well.fluidLevel_meter = value
                valueDHv1.fluidLevel_meter = well.fluidLevel_meter
                valueDHv1.fluidLevel_Pa = well.getFluidLevel_Pa()
                //при наличии плотности жидкости производим расчет давления на приеме оборудования
                if (binding.wellFluidLevel.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                    binding.wellIntakePressure.setText(
                        valueDHv1.calcIntakePressure()?.let { it1 ->
                            well.intakePressure_type?.let { it2 ->
                                well.pressureTypeList[it2]?.let { valueDHv1.valRound(it1.times(it)).toString() }
                            }
                        })
                }
                //проверяем превышение введенного уровня по отношению к длине турбы НКТ
                if (well.fluidLevel_meter != null && valueDHv1.liftDepth_meter != null){
                    if (well.fluidLevel_meter!! > valueDHv1.liftDepth_meter!!) {
                        binding.levelAlertTitle.visibility = View.VISIBLE
                    } else binding.levelAlertTitle.visibility = View.GONE
                }
            }
        )
        binding.wellFluidLevel.setText(prefs.getString("fluidLevel$key", ""))
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
                //getCalculations()
                //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                valueDHv1.calcFluidLevel()
                    ?.let { binding.wellFluidLevel.setText(valueDHv1.valRound(it).toString()) }
            }
        )
        binding.wellCasingPressure.setText(prefs.getString("casingPressure$key", ""))
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
                //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                //расчет производится только при условии наличия фокуса в поле плотности жидкости
                if (binding.wellIntakePressure.hasFocus() && binding.wellFluidDensity.text.toString() != "") {
                    binding.wellFluidLevel.setText(
                        valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                    )
                }
            }
        )
        binding.wellIntakePressure.setText(prefs.getString("intakePressure$key", ""))
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
                //при изменении значения в поле автоматически пересчитываем значение уровня жидкости в скважине
                //расчет производится только при условии наличия фокуса в поле плотности жидкости
                if (binding.wellFluidDensity.hasFocus() && binding.wellIntakePressure.text.toString() != "") {
                    binding.wellFluidLevel.setText(
                        valueDHv1.valRound(valueDHv1.calcFluidLevel())?.toString() ?: ""
                    )
                }
            }
        )
        binding.wellFluidDensity.setText(prefs.getString("fluidDensity$key", ""))
        //обработка потери фокуса полем ввода текста
        binding.wellFluidDensity.setOnFocusChangeListener { _, hasFocus ->
            uiFunc.checkFocusChange(binding.wellFluidDensity, hasFocus, false)
        }

        return binding.root
    }

    private fun adaptersInit(){
        //инициализация объектов фрагмента
        container1 = Container()
        container2 = Container()
        well = context?.let { Well(context = it) }!!

        //инициализация контейнера для прокручиваемого списка типов затрубного давления

        container1.adapter = well.setAdapter(
            binding.wellCasingPressureType,
            well.pressureTypeList.keys.toList().toMutableList(),
            well.CasingPressureHolderList,
             "wellCasingPressureType$key"
        )
        binding.wellCasingPressureType.adapter = container1.adapter
        container1.snapHelper.attachToRecyclerView(binding.wellCasingPressureType)
        binding.wellCasingPressureType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.wellCasingPressureType.onFlingListener = container1.snapHelper
        binding.wellCasingPressureType.smoothScrollToPosition(
            prefs.getInt(
                "wellCasingPressureType$key",
                0
            )
        )
        //инициализация контейнера для прокручиваемого списка типов давления на приеме
        container2.adapter = well.setAdapter(
            binding.wellIntakePressureType,
            well.pressureTypeList.keys.toList().toMutableList(),
            well.IntakePressureHolderList,
            "wellIntakePressureType$key"
        )
        binding.wellIntakePressureType.adapter = container2.adapter
        container2.snapHelper.attachToRecyclerView(binding.wellIntakePressureType)
        binding.wellIntakePressureType.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        binding.wellIntakePressureType.onFlingListener = container2.snapHelper
        binding.wellIntakePressureType.smoothScrollToPosition(
            prefs.getInt(
                "wellIntakePressureType$key",
                0
            )
        )
    }

    //метод для сохранения введенных данных по скважине
    private fun  saveWelldData (){
        val editor = prefs.edit()
        editor.putInt(
            "wellCasingPressureType$label",
            uiFunc.posInMutableList(binding.wellCasingPressureType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putInt(
            "wellIntakePressureType$label",
            uiFunc.posInMutableList(binding.wellIntakePressureType.layoutManager as LinearLayoutManager)
        ).apply()
        editor.putString("fluidLevel$label", binding.wellFluidLevel.text.toString()).apply()
        editor.putString("casingPressure$label", binding.wellCasingPressure.text.toString()).apply()
        editor.putString("intakePressure$label", binding.wellIntakePressure.text.toString()).apply()
        editor.putString("fluidDensity$label", binding.wellFluidDensity.text.toString()).apply()
    }

}