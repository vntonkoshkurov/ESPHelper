package com.example.espspecialisthelper.ui.about

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.espspecialisthelper.BuildConfig
import com.example.espspecialisthelper.R
import com.example.espspecialisthelper.databinding.FragmentAboutBinding

class About : Fragment() {

    private lateinit var binding: FragmentAboutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater)
        binding.aboutTxt.text = Html.fromHtml(getString(R.string.aboutApp), Html.FROM_HTML_MODE_LEGACY)
        binding.aboutTxt.movementMethod = LinkMovementMethod.getInstance()
        binding.versionTxt.text = getString(R.string.app_version) + BuildConfig.VERSION_NAME
        return binding.root
    }


}