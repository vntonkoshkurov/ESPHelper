package com.example.espspecialisthelper.Classes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.espspecialisthelper.Adapters.PickerAdapter
import com.example.espspecialisthelper.MainActivity
import kotlin.math.abs

class UIFunctions(val context: Context) {

    private val RIGHT = 1
    private val LEFT = 0
    var currentThemeTextColor: Int = 0
    var statusOfAdapters: Boolean = false

    /** Ниже создается лямбда функция для заполнения массива объектов списк
    функция используется в качестве коллбека. Передается в адаптер прокручиваемых списков, чтобы заполнять массив
    из холдеров. Это необходимо для чтения выбранных данных в холдерах.
     */
    var getItemHolder: (
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
    var scrollHelper: (
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
    var listControl = { pos: Int, mList: MutableList<*>,
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
    var posInMutableList: (LinearLayoutManager) -> Int =
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
    var checkEditText: (EditText, (Float?) -> Unit) -> TextWatcher =
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

    /**лямбда обрабатывающая поле ввода текста, после потери фокуса. В текстовом поле не должно быть
     * нулевых значений, введенное число не должно заканчиваться на точку (если заканчивается на точку, точка
     * будет удалена)
     */
    var checkFocusChange: (EditText, Boolean, Boolean) -> Unit = {
        //получаемые аргументы: ссылка на текстовое поле, статус изменения фокуса,
        //и указатель возможности оставить нулевое значение в поле
            editText, hasFocus, canZero ->
        if (!hasFocus) {
            var str_tmp = ""
            var string = editText.text.toString()
            if (string.isNotEmpty()) {
                if (string[string.length - 1] == '.') {
                    for (char in string.indices - 1) str_tmp += string[char]
                    if (str_tmp == "0") {
                        editText.setText("")
                    } else {
                        editText.setText(str_tmp)
                    }
                } else if (string == "0" && !canZero) {
                    editText.setText("")
                }
            }
        }
    }

    //функция для реализации вибрации при каких-либо действиях. Используется при прокрутке селекторов
    private fun vibrator() {
        if (statusOfAdapters) MainActivity.vibro(context)
    }
}