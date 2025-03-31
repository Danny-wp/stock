package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.stock.R
import com.example.stock.StockEntity
import com.example.stock.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockDetailFragment : DialogFragment() {

    private val viewModel: MainViewModel by viewModels()
    
    private lateinit var symbolText: TextView
    private lateinit var currentPriceText: TextView
    private lateinit var changeText: TextView
    private lateinit var purchasePriceText: TextView
    private lateinit var profitText: TextView
    private lateinit var targetHighEdit: EditText
    private lateinit var targetLowEdit: EditText
    private lateinit var upPercentEdit: EditText
    private lateinit var downPercentEdit: EditText
    private lateinit var notificationSwitch: Switch
    private lateinit var favoriteSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    
    private var stock: StockEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stock_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        symbolText = view.findViewById(R.id.symbol_text)
        currentPriceText = view.findViewById(R.id.current_price_text)
        changeText = view.findViewById(R.id.change_text)
        purchasePriceText = view.findViewById(R.id.purchase_price_text)
        profitText = view.findViewById(R.id.profit_text)
        targetHighEdit = view.findViewById(R.id.target_high_edit)
        targetLowEdit = view.findViewById(R.id.target_low_edit)
        upPercentEdit = view.findViewById(R.id.up_percent_edit)
        downPercentEdit = view.findViewById(R.id.down_percent_edit)
        notificationSwitch = view.findViewById(R.id.notification_switch)
        favoriteSwitch = view.findViewById(R.id.favorite_switch)
        saveButton = view.findViewById(R.id.save_button)
        deleteButton = view.findViewById(R.id.delete_button)
        
        // 获取股票数据
        val symbol = arguments?.getString(ARG_SYMBOL) ?: ""
        loadStockData(symbol)
        
        // 设置按钮监听
        saveButton.setOnClickListener { saveChanges() }
        deleteButton.setOnClickListener { deleteStock() }
    }
    
    private fun loadStockData(symbol: String) {
        viewModel.getStockBySymbol(symbol).observe(viewLifecycleOwner) { stockData ->
            stock = stockData
            if (stockData != null) {
                updateUI(stockData)
            } else {
                Toast.makeText(context, "无法获取股票数据", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
    
    private fun updateUI(stock: StockEntity) {
        symbolText.text = stock.symbol
        currentPriceText.text = "当前价格: ¥${String.format("%.2f", stock.currentPrice)}"
        
        val priceChange = ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100
        val changeStr = if (priceChange >= 0) "+" else ""
        changeText.text = "变动: ${changeStr}${String.format("%.2f", priceChange)}%"
        
        purchasePriceText.text = "买入价格: ¥${String.format("%.2f", stock.purchasePrice)}"
        
        val profit = (stock.currentPrice - stock.purchasePrice) * stock.holdingShares
        val profitStr = if (profit >= 0) "+" else ""
        profitText.text = "收益: ${profitStr}${String.format("%.2f", profit)} 元"
        
        // 设置目标价格
        targetHighEdit.setText(String.format("%.2f", stock.targetHigh))
        targetLowEdit.setText(String.format("%.2f", stock.targetLow))
        
        // 设置目标百分比
        upPercentEdit.setText(String.format("%.1f", stock.targetUpPercent))
        downPercentEdit.setText(String.format("%.1f", stock.targetDownPercent))
        
        // 设置开关状态
        notificationSwitch.isChecked = stock.notificationEnabled
        favoriteSwitch.isChecked = stock.favorite
    }
    
    private fun saveChanges() {
        val stock = this.stock ?: return
        
        try {
            val targetHigh = targetHighEdit.text.toString().toDouble()
            val targetLow = targetLowEdit.text.toString().toDouble()
            val upPercent = upPercentEdit.text.toString().toDouble()
            val downPercent = downPercentEdit.text.toString().toDouble()
            
            // 更新价格提醒
            viewModel.updatePriceAlerts(stock.symbol, targetHigh, targetLow)
            
            // 更新百分比提醒
            viewModel.updatePercentAlerts(stock.symbol, upPercent, downPercent)
            
            // 更新通知和收藏状态
            viewModel.setNotificationEnabled(stock.symbol, notificationSwitch.isChecked)
            viewModel.setFavorite(stock.symbol, favoriteSwitch.isChecked)
            
            Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(context, "输入有误，请检查数据格式", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun deleteStock() {
        val stock = this.stock ?: return
        
        // 确认删除对话框
        ConfirmDialogFragment.newInstance(
            "确认删除",
            "确定要删除 ${stock.symbol} 吗？此操作不可撤销。"
        ) { confirmed ->
            if (confirmed) {
                viewModel.deleteStock(stock.symbol)
                Toast.makeText(context, "${stock.symbol} 已删除", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }.show(parentFragmentManager, "ConfirmDeleteDialog")
    }
    
    companion object {
        private const val ARG_SYMBOL = "symbol"
        
        fun newInstance(symbol: String) = StockDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SYMBOL, symbol)
            }
        }
    }
} 