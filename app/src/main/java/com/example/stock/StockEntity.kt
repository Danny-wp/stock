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
    val targetUpPercent: Double = 10.0,  // 默认涨幅10%触发提醒
    val targetDownPercent: Double = -5.0,  // 默认跌幅5%触发提醒
    val currentPrice: Double = 0.0,
    val lastNotifyTime: Date? = null,
    val holdingShares: Int,
    val notes: String = "",  // 股票备注信息
    val favorite: Boolean = false,  // 是否为收藏股票
    val notificationEnabled: Boolean = true  // 是否开启通知
)
