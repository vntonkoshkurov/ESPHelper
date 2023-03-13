package com.example.espspecialisthelper.ui.currentunballance

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.databinding.FragmentCurrentUnballanceCalcBinding
import kotlin.math.roundToInt

class CurrentUnballanceCalc() : Fragment() {

    private lateinit var binding: FragmentCurrentUnballanceCalcBinding
    private lateinit var uiFunc: UIFunctions
    private var iA: Float? = null
    private var iB: Float? = null
    private var iC: Float? = null

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
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentUnballanceCalcBinding.inflate(inflater)
        uiFunc = UIFunctions(requireContext())
        /**обработка ввода данных в поле iA*/
        binding.iA.addTextChangedListener(checkEditText(binding.iA) { value: Float? ->
            iA = value
            getCalculations(binding)
        })
        binding.iA.setOnFocusChangeListener { _, hasFocus ->
            //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
            uiFunc.checkFocusChange(binding.iA, hasFocus, true)
        }

        /**обработка ввода данных в поле iB*/
        binding.iB.addTextChangedListener(checkEditText(binding.iB) { value: Float? ->
            iB = value
            getCalculations(binding)
        })
        binding.iB.setOnFocusChangeListener { _, hasFocus ->
            //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
            uiFunc.checkFocusChange(binding.iB, hasFocus, true)
        }

        /**обработка ввода данных в поле iC*/
        binding.iC.addTextChangedListener(checkEditText(binding.iC) { value: Float? ->
            iC = value
            getCalculations(binding)
        })
        binding.iC.setOnFocusChangeListener { _, hasFocus ->
            //после потери фокуса проверяются введенные данные с корректировкой, если это необходимо
            uiFunc.checkFocusChange(binding.iC, hasFocus, true)
        }

        val toolbar: Toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            MainActivity.vibro(requireContext())
            activity!!.onBackPressed()
        }
        return binding.root
    }

    private fun getCalculations(binding: FragmentCurrentUnballanceCalcBinding) {
        try {
            val avarageVal: Float? = (iA!! + iB!! + iC!!) / 3f
            val maxDeviation: Float? = maxOf((avarageVal!! - iA!!), (avarageVal!! - iB!!), (avarageVal!! - iC!!))
            val unballance: Float? = (maxDeviation!! * 100f) / avarageVal
            binding.set1Res.text = valRound(unballance).toString()
        } catch (_: Exception){binding.set1Res.text = "-"}
    }

    override fun onResume() {
        super.onResume()
    }
}