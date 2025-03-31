package com.example.stock.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stock.R
import com.example.stock.ui.adapters.SearchResultAdapter
import com.example.stock.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddStockFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchAdapter: SearchResultAdapter
    
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_stock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.search_edit_text)
        searchButton = view.findViewById(R.id.search_button)
        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        setupRecyclerView()
        setupSearchField()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchResultAdapter { searchResult ->
            // 点击搜索结果，打开添加股票对话框
            AddStockDialogFragment.newInstance(
                symbol = searchResult.symbol,
                name = searchResult.name
            ).show(parentFragmentManager, "AddStockDialog")
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun setupSearchField() {
        // 添加文本变化监听，实现自动搜索
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 取消之前的搜索Job
                searchJob?.cancel()
                
                val query = s.toString().trim()
                if (query.length >= 2) {
                    // 延迟500毫秒再执行搜索，避免频繁请求API
                    searchJob = lifecycleScope.launch {
                        delay(500)
                        viewModel.searchStocks(query)
                    }
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })

        // 点击搜索按钮
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchStocks(query)
            } else {
                Toast.makeText(context, "请输入股票代码或名称", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // 观察搜索结果
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
        }

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
} 