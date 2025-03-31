package com.example.stock

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val stockDao: StockDao,
    private val stockApi: StockApiService,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val stocks = stockDao.getAll()
            
            stocks.forEach { stock ->
                val currentPrice = getCurrentPrice(stock.symbol)
                val priceChange = calculatePriceChange(stock.purchasePrice, currentPrice)
                
                if (shouldTriggerAlert(stock, currentPrice, priceChange)) {
                    notificationHelper.sendTradeAlert(
                        stock.symbol,
                        currentPrice,
                        priceChange
                    )
                    Log.d("PriceAlert", "${stock.symbol} 触发交易提醒：当前价 ${currentPrice}，涨跌幅 ${"%.2f".format(priceChange)}%")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException -> Result.retry()
                else -> Result.failure()
            }
        }
    }

    private suspend fun getCurrentPrice(symbol: String): Double {
        return stockApi.getQuote(symbol).latestPrice
    }

    private fun calculatePriceChange(purchasePrice: Double, currentPrice: Double): Double {
        return ((currentPrice - purchasePrice) / purchasePrice) * 100
    }

    private fun shouldTriggerAlert(
        stock: StockEntity,
        currentPrice: Double,
        priceChange: Double
    ): Boolean {
        return currentPrice >= stock.targetHigh || 
               currentPrice <= stock.targetLow || 
               priceChange >= stock.targetUpPercent || 
               priceChange <= stock.targetDownPercent
    }
}
