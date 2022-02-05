package com.limjuhyg.blueberry.views.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationBarView
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityMainBinding
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.utils.addFragment
import com.limjuhyg.blueberry.utils.hideFragment
import com.limjuhyg.blueberry.utils.showFragment
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.fragments.CustomizeListFragment
import com.limjuhyg.blueberry.views.fragments.MoreFragment
import com.limjuhyg.blueberry.views.fragments.PairedDevicesFragment

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val customizeFragment by lazy { CustomizeListFragment() }
    private var pairedDevicesFragment: PairedDevicesFragment? = null
    private var moreFragment: MoreFragment? = null

    companion object {
        const val BLUETOOTH_PERMISSION_CODE = 1000
    }

    // Test
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.appear, R.anim.none)
        binding.bottomNavView.setOnItemSelectedListener(this)

        addFragment(R.id.main_fragment_container, customizeFragment, false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        for(fragment in supportFragmentManager.fragments) hideFragment(fragment)
        when(item.itemId) {
            R.id.menu_customize -> {
                showFragment(customizeFragment)
            }

            R.id.menu_communication_test -> {
                pairedDevicesFragment?.let {
                    showFragment(it)
                } ?: run {
                    pairedDevicesFragment = PairedDevicesFragment()
                    addFragment(R.id.main_fragment_container, pairedDevicesFragment!!, false)
                }
            }

            R.id.menu_more -> {
                moreFragment?.let {
                    showFragment(it)
                } ?: run {
                    moreFragment = MoreFragment()
                    addFragment(R.id.main_fragment_container, moreFragment!!, false)
                }
            }
        }
        return true
    }
}