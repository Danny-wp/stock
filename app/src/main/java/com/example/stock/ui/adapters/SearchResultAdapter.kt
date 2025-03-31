package com.example.stock.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.StockSearchResult

class SearchResultAdapter(
    private val onItemClick: (StockSearchResult) -> Unit
) : ListAdapter<StockSearchResult, SearchResultAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result)
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symbolText: TextView = itemView.findViewById(R.id.symbol_text)
        private val nameText: TextView = itemView.findViewById(R.id.name_text)
        private val regionText: TextView = itemView.findViewById(R.id.region_text)

        fun bind(result: StockSearchResult) {
            symbolText.text = result.symbol
            nameText.text = result.name
            regionText.text = "${result.region} | ${result.currency}"
            
            itemView.setOnClickListener {
                onItemClick(result)
            }
        }
    }
}

class SearchResultDiffCallback : DiffUtil.ItemCallback<StockSearchResult>() {
    override fun areItemsTheSame(oldItem: StockSearchResult, newItem: StockSearchResult): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: StockSearchResult, newItem: StockSearchResult): Boolean {
        return oldItem == newItem
    }
} 