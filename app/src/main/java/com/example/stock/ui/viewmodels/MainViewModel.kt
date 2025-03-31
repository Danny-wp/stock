package com.example.stock.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.StockEntity
import com.example.stock.StockRepository
import com.example.stock.StockSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {
    
    // 所有股票数据
    val allStocks = repository.allStocks
    
    // 收藏的股票
    val favoriteStocks = repository.favoriteStocks
    
    // 搜索结果
    private val _searchResults = MutableLiveData<List<StockSearchResult>>()
    val searchResults: LiveData<List<StockSearchResult>> = _searchResults
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // 操作结果状态
    private val _operationStatus = MutableLiveData<OperationStatus>()
    val operationStatus: LiveData<OperationStatus> = _operationStatus
    
    // 获取单个股票的LiveData
    fun getStockBySymbol(symbol: String): LiveData<StockEntity?> {
        return repository.getStockBySymbolLive(symbol)
    }
    
    // 添加股票
    fun addStock(
        symbol: String,
        purchasePrice: Double,
        targetHigh: Double,
        targetLow: Double,
        targetUpPercent: Double = 10.0,
        targetDownPercent: Double = -5.0,
        holdingShares: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addStock(
                    symbol = symbol,
                    purchasePrice = purchasePrice,
                    targetHigh = targetHigh,
                    targetLow = targetLow,
                    targetUpPercent = targetUpPercent,
                    targetDownPercent = targetDownPercent,
                    holdingShares = holdingShares,
                    notes = notes
                )
                
                result.onSuccess {
                    _operationStatus.value = OperationStatus.Success("股票成功添加")
                }.onFailure { error ->
                    _operationStatus.value = OperationStatus.Error("添加失败: ${error.message}")
                }
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("添加失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 删除股票
    fun deleteStock(symbol: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteStock(symbol)
                _operationStatus.value = OperationStatus.Success("股票已删除")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("删除失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 更新价格预警
    fun updatePriceAlerts(symbol: String, high: Double, low: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updatePriceAlerts(symbol, high, low)
                _operationStatus.value = OperationStatus.Success("价格预警已更新")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("更新失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 更新涨跌百分比预警
    fun updatePercentAlerts(symbol: String, upPercent: Double, downPercent: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updatePercentAlerts(symbol, upPercent, downPercent)
                _operationStatus.value = OperationStatus.Success("百分比预警已更新")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("更新失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 设置收藏状态
    fun setFavorite(symbol: String, favorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.setFavorite(symbol, favorite)
            } catch (e: Exception) {
                _errorMessage.value = "设置收藏失败: ${e.message}"
            }
        }
    }
    
    // 设置通知状态
    fun setNotificationEnabled(symbol: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.setNotificationEnabled(symbol, enabled)
            } catch (e: Exception) {
                _errorMessage.value = "设置通知失败: ${e.message}"
            }
        }
    }
    
    // 搜索股票
    fun searchStocks(query: String) {
        if (query.length < 2) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.searchStocks(query)
                result.onSuccess { stocks ->
                    _searchResults.value = stocks
                }.onFailure { error ->
                    _errorMessage.value = "搜索失败: ${error.message}"
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 刷新所有股票价格
    fun refreshAllPrices() {
        viewModelScope.launch {
            _isLoading.value = true
            val stocks = withContext(Dispatchers.IO) {
                repository.allStocks.value ?: emptyList()
            }
            
            var successCount = 0
            var failureCount = 0
            
            stocks.forEach { stock ->
                try {
                    val result = repository.refreshStockPrice(stock.symbol)
                    if (result.isSuccess) {
                        successCount++
                    } else {
                        failureCount++
                    }
                } catch (e: Exception) {
                    failureCount++
                }
            }
            
            if (failureCount == 0) {
                _operationStatus.value = OperationStatus.Success("所有价格刷新成功")
            } else {
                _operationStatus.value = OperationStatus.Warning("成功: $successCount, 失败: $failureCount")
            }
            
            _isLoading.value = false
        }
    }
    
    // 检查股票是否已存在
    suspend fun stockExists(symbol: String): Boolean {
        return repository.stockExists(symbol)
    }
    
    // 操作结果状态类
    sealed class OperationStatus {
        data class Success(val message: String) : OperationStatus()
        data class Error(val message: String) : OperationStatus()
        data class Warning(val message: String) : OperationStatus()
    }
} 