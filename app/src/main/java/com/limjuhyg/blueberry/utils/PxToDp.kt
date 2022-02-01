package com.limjuhyg.blueberry.utils

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.pxToDp(px: Int): Float = px / resources.displayMetrics.density