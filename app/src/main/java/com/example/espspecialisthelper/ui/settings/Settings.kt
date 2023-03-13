package com.example.espspecialisthelper.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.espspecialisthelper.MainActivity
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentSettingsBinding

class Settings : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    companion object {
        const val LIGHT = "light"
        const val DARK = "dark"
        const val SYSTEM = "system"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
        val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val radioGroup = binding.radioGroup
        val editor = prefs.edit()

        when ((1..4).random()) {
            1 -> {
                binding.radioLightTheme.text = getString(R.string.theme_name_lite_1)
                binding.radioDarkTheme.text = getString(R.string.theme_name_dark_1)
            }
            2 -> {
                binding.radioLightTheme.text = getString(R.string.theme_name_lite_2)
                binding.radioDarkTheme.text = getString(R.string.theme_name_dark_2)
            }
            3 -> {
                binding.radioLightTheme.text = getString(R.string.theme_name_lite_3)
                binding.radioDarkTheme.text = getString(R.string.theme_name_dark_3)
            }
            4 -> {
                binding.radioLightTheme.text = getString(R.string.theme_name_lite_4)
                binding.radioDarkTheme.text = getString(R.string.theme_name_dark_4)
            }
        }

        binding.radioSystemTheme.text = getString(R.string.theme_system_theme)

        when (prefs.getString("appTheme", SYSTEM)) {
            LIGHT -> radioGroup.check(R.id.radioLightTheme)
            DARK -> radioGroup.check(R.id.radioDarkTheme)
            SYSTEM -> radioGroup.check(R.id.radioSystemTheme)
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioLightTheme -> {
                    MainActivity.reStartval = MainActivity.CHANGE_THEME
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString("appTheme", LIGHT).apply()
                }
                R.id.radioDarkTheme -> {
                    MainActivity.reStartval = MainActivity.CHANGE_THEME
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString("appTheme", DARK).apply()
                }
                R.id.radioSystemTheme -> {
                    MainActivity.reStartval = MainActivity.CHANGE_THEME
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    editor.putString("appTheme", SYSTEM).apply()
                }
            }
            //binding.settingsResult.visibility = View.VISIBLE

            MainActivity.vibro(requireContext())
        }

        return binding.root
    }

}