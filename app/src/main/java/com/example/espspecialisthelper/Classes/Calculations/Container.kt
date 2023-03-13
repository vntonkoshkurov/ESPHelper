package com.example.espspecialisthelper.Classes.Calculations

import androidx.recyclerview.widget.LinearSnapHelper
import com.example.espspecialisthelper.Adapters.PickerAdapter

class Container(
    var adapter: PickerAdapter? = null,
    var snapHelper: LinearSnapHelper = LinearSnapHelper()
)