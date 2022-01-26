package com.totheptv.blueberry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.navigation.NavigationBarView
import com.totheptv.blueberry.databinding.ActivityMainBinding
import com.totheptv.blueberry.utils.addFragment
import com.totheptv.blueberry.utils.hideFragment
import com.totheptv.blueberry.utils.showFragment
import com.totheptv.blueberry.views.CustomizeFragment
import com.totheptv.blueberry.views.MoreFragment
import com.totheptv.blueberry.views.PairedDevicesFragment

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val customizeFragment by lazy { CustomizeFragment() }
    private var pairedDevicesFragment: PairedDevicesFragment? = null
    private var moreFragment: MoreFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.disappear, R.anim.none)
        binding.bottomNavView.setOnItemSelectedListener(this)

        addFragment(R.id.main_fragment_container, customizeFragment)
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
                    addFragment(R.id.main_fragment_container, pairedDevicesFragment!!)
                }
            }

            R.id.menu_more -> {
                moreFragment?.let {
                    showFragment(it)
                } ?: run {
                    moreFragment = MoreFragment()
                    addFragment(R.id.main_fragment_container, moreFragment!!)
                }
            }
        }

        return true
    }
}