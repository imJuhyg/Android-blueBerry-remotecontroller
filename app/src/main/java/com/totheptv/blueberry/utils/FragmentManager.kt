package com.totheptv.blueberry.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.addFragment(fragmentContainer: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .add(fragmentContainer, fragment)
        .commit()
}

fun AppCompatActivity.showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .show(fragment)
        .commit()
}

fun AppCompatActivity.hideFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .hide(fragment)
        .commit()
}