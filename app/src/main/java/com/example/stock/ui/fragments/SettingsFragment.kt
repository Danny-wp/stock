package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stock.BuildConfig
import com.example.stock.PriceCheckWorker
import com.example.stock.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private lateinit var notificationSwitch: Switch
    private lateinit var updateIntervalButton: Button
    private lateinit var apiKeyText: TextView
    private lateinit var updateIntervalText: TextView
    private lateinit var appVersionText: TextView
    
    private var currentUpdateInterval = 15 // 默认15分钟

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        notificationSwitch = view.findViewById(R.id.notification_switch)
        updateIntervalButton = view.findViewById(R.id.update_interval_button)
        apiKeyText = view.findViewById(R.id.api_key_text)
        updateIntervalText = view.findViewById(R.id.update_interval_text)
        appVersionText = view.findViewById(R.id.app_version_text)
        
        loadSettings()
        setupListeners()
    }
    
    private fun loadSettings() {
        // 从SharedPreferences加载设置
        val sharedPrefs = requireContext().getSharedPreferences("stock_settings", 0)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        currentUpdateInterval = sharedPrefs.getInt("update_interval", 15)
        
        // 显示API密钥（部分隐藏）
        apiKeyText.text = "API Key: ****" // 出于安全考虑不显示完整的API Key
        
        // 显示当前更新间隔
        updateIntervalText.text = "当前更新间隔: $currentUpdateInterval 分钟"
        
        // 显示应用版本
        appVersionText.text = "版本: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        
        // 设置通知开关状态
        notificationSwitch.isChecked = notificationsEnabled
    }
    
    private fun setupListeners() {
        // 通知开关监听
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val sharedPrefs = requireContext().getSharedPreferences("stock_settings", 0)
            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            
            Toast.makeText(
                context, 
                if (isChecked) "通知已开启" else "通知已关闭", 
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // 更新间隔按钮监听
        updateIntervalButton.setOnClickListener {
            // 显示选择更新间隔的对话框
            UpdateIntervalDialogFragment.newInstance(currentUpdateInterval) { interval ->
                currentUpdateInterval = interval
                
                // 保存设置
                val sharedPrefs = requireContext().getSharedPreferences("stock_settings", 0)
                sharedPrefs.edit().putInt("update_interval", interval).apply()
                
                // 更新显示
                updateIntervalText.text = "当前更新间隔: $interval 分钟"
                
                // 重新调度后台工作
                scheduleBackgroundWork(interval)
                
                Toast.makeText(context, "更新间隔已设置为 $interval 分钟", Toast.LENGTH_SHORT).show()
            }.show(parentFragmentManager, "UpdateIntervalDialog")
        }
    }
    
    private fun scheduleBackgroundWork(intervalMinutes: Int) {
        val workRequest = PeriodicWorkRequestBuilder<PriceCheckWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        ).build()
        
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "priceCheck",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
} 