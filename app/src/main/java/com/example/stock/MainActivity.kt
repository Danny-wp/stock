package com.example.stock

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.stock.ui.fragments.AddStockFragment
import com.example.stock.ui.fragments.FavoritesFragment
import com.example.stock.ui.fragments.HomeFragment
import com.example.stock.ui.fragments.SettingsFragment
import com.example.stock.ui.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupBottomNavigation()
        
        // 如果是新打开的应用，默认显示首页
        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }
        
        // 启动后台服务
        startPriceCheckService()
    }
    
    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(HomeFragment())
                    true
                }
                R.id.nav_favorites -> {
                    showFragment(FavoritesFragment())
                    true
                }
                R.id.nav_add -> {
                    showFragment(AddStockFragment())
                    true
                }
                R.id.nav_settings -> {
                    showFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
        }
    }
    
    private fun startPriceCheckService() {
        val serviceIntent = Intent(this, PriceCheckService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refreshAllPrices()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 