package com.example.stock.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.StockEntity

class StockAdapter(
    private val onFavoriteClick: (StockEntity, Boolean) -> Unit,
    private val onItemClick: (StockEntity) -> Unit
) : ListAdapter<StockEntity, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = getItem(position)
        holder.bind(stock)
    }

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symbolText: TextView = itemView.findViewById(R.id.symbol_text)
        private val priceText: TextView = itemView.findViewById(R.id.price_text)
        private val changeText: TextView = itemView.findViewById(R.id.change_text)
        private val sharesText: TextView = itemView.findViewById(R.id.shares_text)
        private val favoriteCheckBox: CheckBox = itemView.findViewById(R.id.favorite_checkbox)

        fun bind(stock: StockEntity) {
            symbolText.text = stock.symbol
            priceText.text = "¥${String.format("%.2f", stock.currentPrice)}"
            
            val changePercent = ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100
            val changeStr = if (changePercent >= 0) "+" else ""
            changeText.text = "${changeStr}${String.format("%.2f", changePercent)}%"
            
            // 根据涨跌设置颜色
            val textColor = if (changePercent >= 0) 
                ContextCompat.getColor(itemView.context, R.color.colorGreen)
            else 
                ContextCompat.getColor(itemView.context, R.color.colorRed)
            
            changeText.setTextColor(textColor)
            
            // 持有股数
            sharesText.text = "${stock.holdingShares} 股"
            
            // 设置收藏状态
            favoriteCheckBox.isChecked = stock.favorite
            
            // 设置监听器
            favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteClick(stock, isChecked)
            }
            
            // 点击整个条目
            itemView.setOnClickListener {
                onItemClick(stock)
            }
        }
    }
}

class StockDiffCallback : DiffUtil.ItemCallback<StockEntity>() {
    override fun areItemsTheSame(oldItem: StockEntity, newItem: StockEntity): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: StockEntity, newItem: StockEntity): Boolean {
        return oldItem == newItem
    }
} 