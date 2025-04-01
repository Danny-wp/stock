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
    
    private lateinit var bottomNav: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            var fragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> fragment = HomeFragment()
                R.id.nav_favorites -> fragment = FavoritesFragment()
                R.id.nav_add -> fragment = AddStockFragment()
                R.id.nav_settings -> fragment = SettingsFragment()
            }
            
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }
        
        // 默认显示主页
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
        
        // 启动后台服务
        startPriceCheckService()
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