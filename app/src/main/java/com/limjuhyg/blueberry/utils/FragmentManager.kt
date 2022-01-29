package com.limjuhyg.blueberry.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.limjuhyg.blueberry.R

fun AppCompatActivity.addFragment(fragmentContainer: Int, fragment: Fragment, addBackStack: Boolean) {
    if(addBackStack) {
        supportFragmentManager.beginTransaction()
            .add(fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    } else {
        supportFragmentManager.beginTransaction()
            .add(fragmentContainer, fragment)
            .commit()
    }
}

fun AppCompatActivity.addFragmentWithAnimation(fragmentContainer: Int, fragment: Fragment, enter: Int, exit: Int, addBackStack: Boolean) {
    if(addBackStack) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit)
            .add(fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
    else {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit)
            .add(fragmentContainer, fragment)
            .commit()
    }
}

fun Fragment.addChildFragment(fragmentContainer: Int, fragment: Fragment, addBackStack: Boolean) {
    if(addBackStack) {
        childFragmentManager.beginTransaction()
            .add(fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
    else {
        childFragmentManager.beginTransaction()
            .add(fragmentContainer, fragment)
            .commit()
    }
}

fun Fragment.removeFragment(fragment: Fragment) {
    parentFragmentManager.beginTransaction()
        .remove(fragment)
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