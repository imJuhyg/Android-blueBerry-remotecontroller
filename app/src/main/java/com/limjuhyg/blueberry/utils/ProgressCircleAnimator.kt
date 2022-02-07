package com.limjuhyg.blueberry.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

class ProgressCircleAnimator constructor(
    private val view1: View,
    private val view2: View,
    private val view3: View,
    private val duration: Long
) {
    private var animatorSet: AnimatorSet? = null
    private val delay: Long = duration/2

    private val set1: AnimatorSet = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view1, "scaleX", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }).with(ObjectAnimator.ofFloat(view1, "scaleY", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        })
    }

    private val set2: AnimatorSet = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view2, "scaleX", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }).with(ObjectAnimator.ofFloat(view2, "scaleY", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }).after(delay)
    }

    private val set3: AnimatorSet = AnimatorSet().apply {
        play(ObjectAnimator.ofFloat(view3, "scaleX", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }).with(ObjectAnimator.ofFloat(view3, "scaleY", 1.5f).apply {
            duration = this@ProgressCircleAnimator.duration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }).after(delay*2)
    }

    fun startAnimation() {
        animatorSet = AnimatorSet().apply {
            play(set1).with(set2).with(set3)
        }

        if(animatorSet!!.isRunning) animatorSet!!.cancel()
        animatorSet!!.start()
    }

    fun cancelAnimation() {
        animatorSet?.let {
            if(it.isRunning) it.cancel()
        }
    }
}