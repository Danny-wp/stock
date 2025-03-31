package com.example.stock.di

import android.content.Context
import androidx.room.Room
import com.example.stock.StockDao
import com.example.stock.StockDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideStockDatabase(@ApplicationContext context: Context): StockDatabase {
        return Room.databaseBuilder(
            context,
            StockDatabase::class.java,
            StockDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 如果没有找到迁移脚本，重建数据库
            .build()
    }
    
    @Provides
    fun provideStockDao(database: StockDatabase): StockDao {
        return database.stockDao()
    }
} 