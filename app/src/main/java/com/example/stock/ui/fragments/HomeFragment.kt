package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var stockAdapter: StockAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)

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
                // 打开股票详情页面
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

        // 设置下拉刷新的颜色
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            android.R.color.holo_green_light
        )
    }

    private fun observeViewModel() {
        // 观察所有股票数据
        viewModel.allStocks.observe(viewLifecycleOwner) { stocks ->
            stockAdapter.submitList(stocks)
        }

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }

        // 观察操作状态
        viewModel.operationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MainViewModel.OperationStatus.Success -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.OperationStatus.Error -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
                is MainViewModel.OperationStatus.Warning -> {
                    Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
} 