package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.stock.R
import com.example.stock.ui.adapters.StockAdapter
import com.example.stock.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var stockAdapter: StockAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        emptyView = view.findViewById(R.id.empty_view)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        stockAdapter = StockAdapter(
            onFavoriteClick = { stock, isFavorite ->
                viewModel.setFavorite(stock.symbol, isFavorite)
            },
            onItemClick = { stock ->
                StockDetailFragment.newInstance(stock.symbol).show(
                    parentFragmentManager,
                    "StockDetailDialog"
                )
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stockAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAllPrices()
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            android.R.color.holo_green_light
        )
    }

    private fun observeViewModel() {
        // 观察收藏的股票数据
        viewModel.favoriteStocks.observe(viewLifecycleOwner) { stocks ->
            stockAdapter.submitList(stocks)
            
            // 如果收藏列表为空，显示空状态视图
            if (stocks.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }
    }
} 