package com.example.stock

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: StockEntity)

    @Update
    suspend fun update(stock: StockEntity)

    @Query("UPDATE stocks SET currentPrice = :price, lastNotifyTime = :time WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, price: Double, time: Long)

    @Query("UPDATE stocks SET targetHigh = :high, targetLow = :low WHERE symbol = :symbol")
    suspend fun updatePriceAlerts(symbol: String, high: Double, low: Double)

    @Query("UPDATE stocks SET targetUpPercent = :upPercent, targetDownPercent = :downPercent WHERE symbol = :symbol")
    suspend fun updatePercentAlerts(symbol: String, upPercent: Double, downPercent: Double)

    @Query("UPDATE stocks SET favorite = :favorite WHERE symbol = :symbol")
    suspend fun setFavorite(symbol: String, favorite: Boolean)

    @Query("UPDATE stocks SET notificationEnabled = :enabled WHERE symbol = :symbol")
    suspend fun setNotificationEnabled(symbol: String, enabled: Boolean)

    @Query("DELETE FROM stocks WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)

    @Query("SELECT * FROM stocks")
    suspend fun getAll(): List<StockEntity>

    @Query("SELECT * FROM stocks")
    fun getAllLive(): LiveData<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE favorite = 1")
    fun getFavorites(): LiveData<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getBySymbol(symbol: String): StockEntity?

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    fun getBySymbolLive(symbol: String): LiveData<StockEntity?>

    @Query("SELECT * FROM stocks WHERE notificationEnabled = 1")
    suspend fun getStocksWithNotifications(): List<StockEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM stocks WHERE symbol = :symbol LIMIT 1)")
    suspend fun exists(symbol: String): Boolean
}
