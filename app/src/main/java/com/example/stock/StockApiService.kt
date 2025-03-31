package com.example.stock

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Alpha Vantage股票API服务接口
 */
interface StockApiService {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): GlobalQuoteResponse
    
    @GET("query")
    suspend fun searchStocks(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): SearchResponse
}

/**
 * 全球股票报价响应
 */
data class GlobalQuoteResponse(
    @Json(name = "Global Quote") val quote: StockQuote?
)

/**
 * 股票报价数据模型
 */
data class StockQuote(
    @Json(name = "01. symbol") val symbol: String,
    @Json(name = "02. open") val open: String,
    @Json(name = "03. high") val high: String,
    @Json(name = "04. low") val low: String,
    @Json(name = "05. price") val price: String,
    @Json(name = "06. volume") val volume: String,
    @Json(name = "07. latest trading day") val latestTradingDay: String,
    @Json(name = "08. previous close") val previousClose: String,
    @Json(name = "09. change") val change: String,
    @Json(name = "10. change percent") val changePercent: String
) {
    // 便捷方法获取价格
    fun getLatestPrice(): Double = price.toDoubleOrNull() ?: 0.0
    
    // 获取涨跌幅
    fun getChangePercent(): Double {
        val percentStr = changePercent.replace("%", "")
        return percentStr.toDoubleOrNull() ?: 0.0
    }
}

/**
 * 股票搜索响应
 */
data class SearchResponse(
    @Json(name = "bestMatches") val matches: List<StockSearchResult>?
)

/**
 * 股票搜索结果模型
 */
data class StockSearchResult(
    @Json(name = "1. symbol") val symbol: String,
    @Json(name = "2. name") val name: String,
    @Json(name = "3. type") val type: String,
    @Json(name = "4. region") val region: String,
    @Json(name = "5. marketOpen") val marketOpen: String,
    @Json(name = "6. marketClose") val marketClose: String,
    @Json(name = "7. timezone") val timezone: String,
    @Json(name = "8. currency") val currency: String,
    @Json(name = "9. matchScore") val matchScore: String
) 