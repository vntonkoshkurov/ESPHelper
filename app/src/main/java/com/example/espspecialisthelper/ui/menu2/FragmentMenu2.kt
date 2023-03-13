package com.example.espspecialisthelper.ui.menu2

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.databinding.FragmentFragmentMenu2Binding
import com.example.espspecialisthelper.ui.comissioning.Comissioning
import com.example.espspecialisthelper.ui.downholeeqpt.DownholeEqptCalc
import com.example.espspecialisthelper.ui.electrical.ElectricalCalc
import com.example.espspecialisthelper.ui.volume.VolumeTubings

class FragmentMenu2() : Fragment() {

    private lateinit var binding: FragmentFragmentMenu2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FM2", "FM2 onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FM2", "FM2 onCreateView")
        binding = FragmentFragmentMenu2Binding.inflate(inflater)
        //обработка нажатия на первую кнопку меню
        binding.button1.setOnClickListener {
            vibrator()
            (activity as MainActivity).pushFragments(MainActivity.TAB_M2, DownholeEqptCalc(), true)
        }
        binding.button2.setOnClickListener {
            vibrator()
            (activity as MainActivity).pushFragments(MainActivity.TAB_M2, VolumeTubings(), true)
        }
        binding.button3.setOnClickListener {
            vibrator()
            (activity as MainActivity).pushFragments(MainActivity.TAB_M2, Comissioning(), true)
        }

        return binding.root
    }

    private fun vibrator() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(5)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FM2", "FM2 onResume")
    }

    override fun onPause() {
        Log.d("FM2", "FM2 onPause")
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d("FM2", "FM2 onStop")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("FM12", "FM2 onDetach")
    }

}