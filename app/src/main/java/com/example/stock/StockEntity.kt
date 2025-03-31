package com.example.stock

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val purchasePrice: Double,
    val targetHigh: Double,
    val targetLow: Double,
    val currentPrice: Double = 0.0,
    val lastNotifyTime: Date? = null,
    val holdingShares: Int
)
