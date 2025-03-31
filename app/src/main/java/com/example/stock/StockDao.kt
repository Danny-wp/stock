package com.example.stock

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StockDao {
    @Insert
    suspend fun insert(stock: StockEntity)

    @Update
    suspend fun update(stock: StockEntity)

    @Query("UPDATE stocks SET highAlert = :high, lowAlert = :low WHERE id = :id")
    suspend fun setAlerts(id: Int, high: Float, low: Float)

    @Query("DELETE FROM stocks WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM stocks WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)

    @Query("SELECT * FROM stocks")
    suspend fun getAllStocks(): List<StockEntity>

    @Query("SELECT * FROM stocks WHERE id = :id")
    suspend fun getStockById(id: Int): StockEntity?

    @Query("SELECT * FROM stocks WHERE highAlert > 0 OR lowAlert > 0")
    suspend fun getMonitoredStocks(): List<StockEntity>
}
