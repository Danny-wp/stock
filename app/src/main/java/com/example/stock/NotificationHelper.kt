package com.example.stock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "股票提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "股票价格变动提醒通知"
                enableVibration(true)
            }
            
            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "股票更新",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "股票价格定期更新通知"
            }
            
            notificationManager.createNotificationChannels(listOf(alertChannel, updateChannel))
        }
    }
    
    /**
     * 发送股票交易提醒通知
     */
    fun sendTradeAlert(
        symbol: String,
        currentPrice: Double,
        priceChange: Double
    ) {
        val shouldBuy = shouldBuy(priceChange)
        val shouldSell = shouldSell(priceChange)
        
        val title = "交易提醒: $symbol"
        val message = buildString {
            append("当前价格: ¥${String.format("%.2f", currentPrice)}\n")
            append("变动: ${if (priceChange >= 0) "+" else ""}${String.format("%.2f", priceChange)}%\n")
            
            if (shouldBuy) {
                append("🟢 可能是买入时机")
            } else if (shouldSell) {
                append("🔴 可能是卖出时机")
            } else {
                append("📊 价格已触发您设置的提醒条件")
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("symbol", symbol)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("点击查看详情")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(symbol.hashCode(), notification)
    }
    
    /**
     * 发送股票价格更新通知
     */
    fun sendPriceUpdate(stocks: List<StockEntity>) {
        if (stocks.isEmpty()) return
        
        val message = buildString {
            stocks.take(5).forEach { stock ->
                val priceChange = ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100
                val changeStr = if (priceChange >= 0) "+" else ""
                append("${stock.symbol}: ¥${String.format("%.2f", stock.currentPrice)} ")
                append("(${changeStr}${String.format("%.2f", priceChange)}%)\n")
            }
            
            if (stocks.size > 5) {
                append("...以及其他 ${stocks.size - 5} 支股票")
            }
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("股票价格更新")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(PRICE_UPDATE_ID, notification)
    }
    
    private fun shouldBuy(priceChange: Double): Boolean {
        // 价格下跌超过5%可能是买入时机
        return priceChange <= -5.0
    }
    
    private fun shouldSell(priceChange: Double): Boolean {
        // 价格上涨超过10%可能是卖出时机
        return priceChange >= 10.0
    }
    
    companion object {
        private const val ALERT_CHANNEL_ID = "stock_alert_channel"
        private const val UPDATE_CHANNEL_ID = "stock_update_channel"
        private const val PRICE_UPDATE_ID = 1001
    }
} 