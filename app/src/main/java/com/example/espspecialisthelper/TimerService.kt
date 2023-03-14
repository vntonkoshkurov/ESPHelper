package com.example.espspecialisthelper

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.math.roundToInt

class TimerService : Service() {

    private val timer = Timer()
    private var timerString = "00:00:00"
    private var activityType = ""
    private lateinit var notification: Notification
    private lateinit var notificationBuilder: NotificationCompat.Builder

    companion object {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
        const val ACTIVITY_TYPE = "activityType"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        startMyOwnForeground()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        activityType = intent.getStringExtra(ACTIVITY_TYPE) ?: ""
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return START_NOT_STICKY
    }

    /*данный класс вызывается планировщиком таймера он не только увеличивает последовательность
    * секундомера, но и обовляет его показания в статусбаре*/
    private inner class TimeTask(private var time: Double) : TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATED)
            time++
            timerString = getTimeStringFromDouble(time)
            notification = notificationBuilder.setOngoing(true).setContentTitle("$activityType $timerString").build()
            startForeground(2, notification)
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
        }
    }

    /*при запуске сервиса в панели уведомлений появляется уведомление о запущенном процессе ВНР*/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val  NOTIFICATION_CHANNEL_ID = "ESPCommissioning"
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)!!
        manager!!.createNotificationChannel(chan)
        //создается намерение при нажатии на уведомление.
        //нам нужно, чтобы при нажатии на уведомление приложение открывалось в том же окне, где оно и свернулось
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        //добавляем в интент данные о необходимости открыть фрагмент ВНР при нажатии на фрагмент уведомления
        notificationIntent.putExtra("isServiseStarted", true)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0, notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        //для того, чтобы при нажатии на уведомление запускалась текущая активность, а не создавалась новая
        //а Manifest в MainActivity добавлена строка android:launchMode="singleTop"
        notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.about_pressed)
            .setContentTitle("$activityType $timerString")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(contentIntent)
            .build()
        startForeground(2, notification)
    }

    /*функции для преобразования значения времени в текст с форматом ЧЧ:ММ:СС*/
    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, minute: Int, second: Int): String = String.format("%02d:%02d:%02d", hour, minute, second)

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

}