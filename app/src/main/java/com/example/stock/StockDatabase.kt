package com.example.stock

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StockEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    
    companion object {
        const val DATABASE_NAME = "stock_db"
        
        // 如果未来需要数据库迁移，可以在这里添加
        val MIGRATIONS = arrayOf<Migration>()
    }
}
