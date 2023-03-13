package com.example.espspecialisthelper

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import kotlin.math.roundToInt

class TimerServiceHelper (val textView: TextView, val context: Context, val fragmentActivity: FragmentActivity) {
    var timerStarted = false
    var time = 0.0

    private var serviseIntent: Intent = Intent(context, TimerService::class.java)
    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            textView.text = getTimeStringFromDouble(time)
        }
    }

    init {
        fragmentActivity.registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    fun startTimer(string: String) {
        serviseIntent.putExtra(TimerService.TIME_EXTRA, time)
        serviseIntent.putExtra(TimerService.ACTIVITY_TYPE, string)
        fragmentActivity.startService(serviseIntent)
        timerStarted = true
    }

    fun stopTimer() {
        serviseIntent.putExtra(TimerService.TIME_EXTRA, time)
        fragmentActivity.stopService(serviseIntent)
        timerStarted = false
        time = 0.0
        textView.text = getTimeStringFromDouble(time)
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, minute: Int, second: Int): String = String.format("%02d:%02d:%02d", hour, minute, second)

    //функция для определения работы сервиса в фоне
    fun isMyServiceRunning(): Boolean {
        val serviceClass = TimerService::class.java
        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /*пример исходной функции по проверке работы сервиса
    fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    * */
}