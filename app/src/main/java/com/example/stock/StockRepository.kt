package com.example.stock

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val stockDao: StockDao,
    private val stockApi: StockApiService,
    private val apiKey: String
) {
    // 获取所有股票的实时数据
    val allStocks: LiveData<List<StockEntity>> = stockDao.getAllLive()
    
    // 获取收藏的股票
    val favoriteStocks: LiveData<List<StockEntity>> = stockDao.getFavorites()
    
    // 获取单个股票的LiveData
    fun getStockBySymbolLive(symbol: String): LiveData<StockEntity?> {
        return stockDao.getBySymbolLive(symbol)
    }
    
    // 添加股票
    suspend fun addStock(
        symbol: String,
        purchasePrice: Double,
        targetHigh: Double,
        targetLow: Double,
        targetUpPercent: Double,
        targetDownPercent: Double,
        holdingShares: Int,
        notes: String
    ) = withContext(Dispatchers.IO) {
        try {
            // 获取当前股价
            val response = stockApi.getQuote(symbol = symbol, apiKey = apiKey)
            val quote = response.quote
            
            if (quote == null) {
                return@withContext Result.failure<StockEntity>(
                    IllegalStateException("无法获取股票数据")
                )
            }
            
            val stock = StockEntity(
                symbol = symbol,
                purchasePrice = purchasePrice,
                targetHigh = targetHigh,
                targetLow = targetLow,
                targetUpPercent = targetUpPercent,
                targetDownPercent = targetDownPercent,
                currentPrice = quote.getLatestPrice(),
                holdingShares = holdingShares,
                notes = notes
            )
            
            stockDao.insert(stock)
            Result.success(stock)
        } catch (e: Exception) {
            Result.failure<StockEntity>(e)
        }
    }
    
    // 更新股票信息
    suspend fun updateStock(stock: StockEntity) = withContext(Dispatchers.IO) {
        stockDao.update(stock)
    }
    
    // 删除股票
    suspend fun deleteStock(symbol: String) = withContext(Dispatchers.IO) {
        stockDao.deleteBySymbol(symbol)
    }
    
    // 更新价格预警条件
    suspend fun updatePriceAlerts(symbol: String, high: Double, low: Double) = withContext(Dispatchers.IO) {
        stockDao.updatePriceAlerts(symbol, high, low)
    }
    
    // 更新百分比预警条件
    suspend fun updatePercentAlerts(symbol: String, upPercent: Double, downPercent: Double) = withContext(Dispatchers.IO) {
        stockDao.updatePercentAlerts(symbol, upPercent, downPercent)
    }
    
    // 设置收藏状态
    suspend fun setFavorite(symbol: String, favorite: Boolean) = withContext(Dispatchers.IO) {
        stockDao.setFavorite(symbol, favorite)
    }
    
    // 设置通知状态
    suspend fun setNotificationEnabled(symbol: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        stockDao.setNotificationEnabled(symbol, enabled)
    }
    
    // 获取股票详情
    suspend fun getStockDetail(symbol: String): Result<StockEntity> = withContext(Dispatchers.IO) {
        try {
            val stock = stockDao.getBySymbol(symbol)
            if (stock != null) {
                Result.success(stock)
            } else {
                Result.failure(NoSuchElementException("找不到符号为 $symbol 的股票"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 获取股票实时价格
    suspend fun refreshStockPrice(symbol: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val response = stockApi.getQuote(symbol = symbol, apiKey = apiKey)
            val quote = response.quote ?: return@withContext Result.failure(
                IllegalStateException("无法获取股票数据")
            )
            
            val currentPrice = quote.getLatestPrice()
            
            // 更新数据库
            stockDao.updatePrice(symbol, currentPrice, System.currentTimeMillis())
            
            Result.success(currentPrice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 搜索股票
    suspend fun searchStocks(query: String): Result<List<StockSearchResult>> = withContext(Dispatchers.IO) {
        try {
            val response = stockApi.searchStocks(keywords = query, apiKey = apiKey)
            val results = response.matches ?: emptyList()
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 股票是否已存在
    suspend fun stockExists(symbol: String): Boolean = withContext(Dispatchers.IO) {
        stockDao.exists(symbol)
    }
} 