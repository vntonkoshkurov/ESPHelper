package com.example.espspecialisthelper.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.espspecialisthelper.databinding.PickerBinding

class PickerAdapter(
    private val context: Context,
    private val range: MutableList<*>,
    private val holderRange: MutableList<PickerItemViewHolder>,
    private val getItemHolder: (PickerItemViewHolder, MutableList<PickerItemViewHolder>, Int) -> Unit,
    private val scroller: ((Int) -> Unit)? = null //это лямбда функция, которая помогает пошагово пролистать список до значения, которое еще не создано
) : RecyclerView.Adapter<PickerAdapter.PickerItemViewHolder>() {

    var isInit: Boolean = false //переменная, которая показывает завершение построения списка для функции прокручивания, чтобы та не срабатывала после
                                //полной инициализации фрагмента.

    class PickerItemViewHolder(_binding: PickerBinding) : RecyclerView.ViewHolder(_binding.root) {
        private val binding = _binding
        fun bind(pickerItem: String) {
            //if (pickerItem.length > 3) binding.freqSelector.minWidth = 150//binding.textView.width + 6
            binding.textView.text = pickerItem
        }

        fun getBind(): PickerBinding {
            return binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerItemViewHolder {
        val binding = PickerBinding.inflate(LayoutInflater.from(context), parent, false)
        return PickerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PickerItemViewHolder, position: Int) {
        val freqVal = range[position]
        holder.bind(freqVal.toString())
        //ниже адаптер передает в вызвавший его фрагмент через лямбду холдер, чтобы можно было работать с ним внутри фрагмента при необходимости
        getItemHolder(holder, holderRange, position)
        //ниже пользуемся полученной функцией в конструкторе класса, которая позволяет корректно прокрутить список до необходимого значения
        //после достижения необходимого значения из файла настроек статус isInit получит положительное значение и в дальнейшем
        //в использовании данной функции надобности не будет
        try {
            if (!isInit) scroller!!(position)
        } catch (_: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return range.size
    }
}