package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.stock.R
import com.example.stock.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddStockDialogFragment : DialogFragment() {

    private val viewModel: MainViewModel by viewModels()
    
    private lateinit var symbolText: TextView
    private lateinit var nameText: TextView
    private lateinit var purchasePriceEdit: EditText
    private lateinit var sharesEdit: EditText
    private lateinit var targetHighEdit: EditText
    private lateinit var targetLowEdit: EditText
    private lateinit var upPercentEdit: EditText
    private lateinit var downPercentEdit: EditText
    private lateinit var notesEdit: EditText
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_stock_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        symbolText = view.findViewById(R.id.symbol_text)
        nameText = view.findViewById(R.id.name_text)
        purchasePriceEdit = view.findViewById(R.id.purchase_price_edit)
        sharesEdit = view.findViewById(R.id.shares_edit)
        targetHighEdit = view.findViewById(R.id.target_high_edit)
        targetLowEdit = view.findViewById(R.id.target_low_edit)
        upPercentEdit = view.findViewById(R.id.up_percent_edit)
        downPercentEdit = view.findViewById(R.id.down_percent_edit)
        notesEdit = view.findViewById(R.id.notes_edit)
        cancelButton = view.findViewById(R.id.cancel_button)
        addButton = view.findViewById(R.id.add_button)
        
        // 设置默认值
        upPercentEdit.setText("10.0")
        downPercentEdit.setText("-5.0")
        
        // 获取参数
        val symbol = arguments?.getString(ARG_SYMBOL) ?: ""
        val name = arguments?.getString(ARG_NAME) ?: ""
        
        symbolText.text = symbol
        nameText.text = name
        
        // 设置按钮监听
        cancelButton.setOnClickListener { dismiss() }
        addButton.setOnClickListener { addStock() }
        
        // 检查股票是否已存在
        lifecycleScope.launch {
            if (symbol.isNotEmpty() && viewModel.stockExists(symbol)) {
                Toast.makeText(context, "股票已存在于您的列表中", Toast.LENGTH_LONG).show()
                dismiss()
            }
        }
    }
    
    private fun addStock() {
        val symbol = symbolText.text.toString()
        
        try {
            val purchasePrice = purchasePriceEdit.text.toString().toDouble()
            val shares = sharesEdit.text.toString().toInt()
            val targetHigh = targetHighEdit.text.toString().toDouble()
            val targetLow = targetLowEdit.text.toString().toDouble()
            val upPercent = upPercentEdit.text.toString().toDouble()
            val downPercent = downPercentEdit.text.toString().toDouble()
            val notes = notesEdit.text.toString()
            
            if (purchasePrice <= 0 || shares <= 0) {
                Toast.makeText(context, "请输入有效的价格和数量", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 添加股票
            viewModel.addStock(
                symbol = symbol,
                purchasePrice = purchasePrice,
                targetHigh = targetHigh,
                targetLow = targetLow,
                targetUpPercent = upPercent,
                targetDownPercent = downPercent,
                holdingShares = shares,
                notes = notes
            )
            
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(context, "输入有误，请检查数据格式", Toast.LENGTH_LONG).show()
        }
    }
    
    companion object {
        private const val ARG_SYMBOL = "symbol"
        private const val ARG_NAME = "name"
        
        fun newInstance(symbol: String, name: String) = AddStockDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SYMBOL, symbol)
                putString(ARG_NAME, name)
            }
        }
    }
} 