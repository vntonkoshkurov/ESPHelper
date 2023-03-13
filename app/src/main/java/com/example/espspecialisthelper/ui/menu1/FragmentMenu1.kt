package com.example.espspecialisthelper.ui.menu1

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
import com.example.espspecialisthelper.Classes.UIFunctions
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.databinding.FragmentFragmentMenu1Binding
import com.example.espspecialisthelper.ui.checkstartup.CheckStartup
import com.example.espspecialisthelper.ui.currentunballance.CurrentUnballanceCalc
import com.example.espspecialisthelper.ui.electrical.ElectricalCalc

class FragmentMenu1() : Fragment() {

    private lateinit var binding: FragmentFragmentMenu1Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FM1", "FM1 onCreateView")
        binding = FragmentFragmentMenu1Binding.inflate(inflater)
        binding.button1.setOnClickListener {
            MainActivity.vibro(requireContext())
            //открываем фрагмент с расчетами
            (activity as MainActivity).pushFragments(MainActivity.TAB_M1, ElectricalCalc(), true)
        }
        binding.button2.setOnClickListener {
            MainActivity.vibro(requireContext())
            //открываем фрагмент с расчетами
            (activity as MainActivity).pushFragments(MainActivity.TAB_M1, CheckStartup(), true)
        }
        binding.button3.setOnClickListener {
            MainActivity.vibro(requireContext())
            //открываем фрагмент с расчетами
            (activity as MainActivity).pushFragments(
                MainActivity.TAB_M1,
                CurrentUnballanceCalc(),
                true
            )
        }
        return binding.root
    }

}