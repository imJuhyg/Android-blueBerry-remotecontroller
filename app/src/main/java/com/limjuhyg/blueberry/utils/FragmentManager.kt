package com.limjuhyg.blueberry.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.addFragment(fragmentContainer: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .add(fragmentContainer, fragment)
        .commit()
}

fun Fragment.addChildFragment(fragmentContainer: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction()
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