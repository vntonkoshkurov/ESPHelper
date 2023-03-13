package com.example.espspecialisthelper.Classes

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.espspecialisthelper.R

class ComissionDialogStart : DialogFragment() {
    private val acivityType = arrayOf(getString(R.string.comis_startup_title),
        getString(R.string.comis_flow_appear_time_title), getString(R.string.comis_measurement_title))
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.comis_select_activity))
                .setSingleChoiceItems(acivityType, -1
                ) {
                    dialog, item ->

                }
                .setPositiveButton("OK") {
                        dialog, id ->  dialog.cancel()
                }
                .setNegativeButton(getString(R.string.cancel_title)) {
                        dialog, id ->  dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}