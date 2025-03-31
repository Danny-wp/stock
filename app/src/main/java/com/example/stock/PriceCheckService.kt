package com.example.stock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class PriceCheckService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        startPeriodicCheck()
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "股价提醒服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "后台股价监控中..."
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("股价监控运行中")
            .setContentText("正在监控设置的股价变动...")
            .setSmallIcon(R.drawable.ic_baseline_monitor_24)
            .build()
    }

    private fun startPeriodicCheck() {
        val workRequest = PeriodicWorkRequestBuilder<PriceCheckWorker>(
            15, TimeUnit.MINUTES
        ).apply {
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }.build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "priceCheck",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    companion object {
        private const val CHANNEL_ID = "stock_monitor_channel"
        private const val NOTIFICATION_ID = 101
    }
}
