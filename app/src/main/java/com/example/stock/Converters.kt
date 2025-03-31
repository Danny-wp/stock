package com.example.stock

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room数据库类型转换器
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 