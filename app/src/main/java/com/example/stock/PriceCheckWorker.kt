package com.example.stock

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val stockDao: StockDao,
    private val stockApi: StockApiService,
    private val notificationHelper: NotificationHelper,
    private val apiKey: String
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PriceCheckWorker"
        
        // 通知冷却时间: 6小时
        private const val NOTIFICATION_COOLDOWN_MS = 6 * 60 * 60 * 1000L
        
        // 股票更新批次大小 (Alpha Vantage免费API限制每分钟5次调用)
        private const val BATCH_SIZE = 5
        
        // API调用延迟（毫秒）
        private const val API_CALL_DELAY = 15000L // 15秒
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "开始检查股票价格...")
            val stocks = stockDao.getStocksWithNotifications()
            
            if (stocks.isEmpty()) {
                Log.d(TAG, "没有需要监控的股票")
                return@withContext Result.success()
            }
            
            Log.d(TAG, "需要监控的股票数量: ${stocks.size}")
            val updatedStocks = mutableListOf<StockEntity>()
            
            // 批次处理股票，避免超出Alpha Vantage API限制
            stocks.chunked(BATCH_SIZE).forEach { batch ->
                batch.forEach { stock ->
                    try {
                        processStock(stock)?.let { updatedStocks.add(it) }
                        // 延迟以避免超出API限制
                        delay(API_CALL_DELAY)
                    } catch (e: Exception) {
                        Log.e(TAG, "处理股票 ${stock.symbol} 时出错: ${e.message}")
                    }
                }
            }
            
            // 发送汇总通知
            if (updatedStocks.isNotEmpty()) {
                notificationHelper.sendPriceUpdate(updatedStocks)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "股票价格检查失败", e)
            when (e) {
                is IOException, is HttpException -> Result.retry()
                else -> Result.failure()
            }
        }
    }

    private suspend fun processStock(stock: StockEntity): StockEntity? {
        try {
            val response = stockApi.getQuote(symbol = stock.symbol, apiKey = apiKey)
            val quote = response.quote ?: return null
            
            val currentPrice = quote.getLatestPrice()
            val priceChange = calculatePriceChange(stock.purchasePrice, currentPrice)
            
            // 更新数据库中的当前价格
            val currentTime = System.currentTimeMillis()
            stockDao.updatePrice(stock.symbol, currentPrice, currentTime)
            
            val updatedStock = stock.copy(currentPrice = currentPrice)
            
            // 检查是否应该发送通知
            if (shouldSendNotification(updatedStock, currentPrice, priceChange)) {
                notificationHelper.sendTradeAlert(
                    stock.symbol,
                    currentPrice,
                    priceChange
                )
                Log.d(TAG, "${stock.symbol} 触发交易提醒：当前价 ${currentPrice}，涨跌幅 ${"%.2f".format(priceChange)}%")
            }
            
            return updatedStock
        } catch (e: Exception) {
            Log.e(TAG, "获取 ${stock.symbol} 的价格时出错: ${e.message}")
            return null
        }
    }

    private fun calculatePriceChange(purchasePrice: Double, currentPrice: Double): Double {
        return ((currentPrice - purchasePrice) / purchasePrice) * 100
    }

    private fun shouldSendNotification(
        stock: StockEntity,
        currentPrice: Double,
        priceChange: Double
    ): Boolean {
        if (!stock.notificationEnabled) {
            return false
        }
        
        // 检查通知冷却期
        val lastNotifyTime = stock.lastNotifyTime?.time ?: 0
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotifyTime < NOTIFICATION_COOLDOWN_MS) {
            return false
        }
        
        // 检查价格条件
        val triggerPriceAlert = currentPrice >= stock.targetHigh || currentPrice <= stock.targetLow
        val triggerPercentAlert = priceChange >= stock.targetUpPercent || priceChange <= stock.targetDownPercent
        
        return triggerPriceAlert || triggerPercentAlert
    }
}
