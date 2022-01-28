/*
package com.limjuhyg.blueberry.views

import android.graphics.Point
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentCustomizeCreateBinding
import com.limjuhyg.blueberry.utils.addChildFragment

class CustomizeCreateFragment : Fragment() {
    private lateinit var binding: FragmentCustomizeCreateBinding
    private var statusBarHeight = 0
    private var topPanelHeight = 0
    private var verticalGuidelineDefaultCoordinate = 0f
    private var horizontalGuidelineDefaultCoordinate = 0f
    private var verticalGuidelinePivot = 0f
    private var horizontalGuidelinePivot = 0f
    private var widgetDefaultWidth: Int = 0
    private var widgetDefaultHeight: Int = 0
    private val widgetsFragment by lazy { WidgetsFragment() }
    private val keyboard by lazy { requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val widgetInitialCoordinate by lazy { Point() }
    private val trashcanTopLeftCoordinate by lazy { Point() }
    private val trashcanBottomRightCoordinate by lazy { Point() }
    private var widgetCount: Int = 0
    private var isAddedVerticalGuideline: Boolean = false
    private var isAddedHorizontalGuideline: Boolean = false

    companion object {
        // Widget modification mode
        const val NONE = 0
        const val TRANSLATE = 1
        const val SCALE = 2

        // Widget default type

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCustomizeCreateBinding.inflate(layoutInflater)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // status bar 높이 구하기
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        binding.apply {
            addChildFragment(fragmentContainer.id, widgetsFragment)
            btnShowWidgetFragment.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.identityColor))

            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    topPanelHeight = topPanel.height
                    widgetDefaultWidth = mainLayout.width/5
                    widgetDefaultHeight = mainLayout.height/7
                    verticalGuidelineDefaultCoordinate = verticalGuideline.x
                    horizontalGuidelineDefaultCoordinate = horizontalGuideline.y
                    verticalGuidelinePivot = verticalGuideline.x + verticalGuideline.width/2
                    horizontalGuidelinePivot = horizontalGuideline.y + horizontalGuideline.height/2
                    widgetInitialCoordinate.set(customizeLayout.width/2, customizeLayout.height/10)
                    trashcanTopLeftCoordinate.set(trashcan.x.toInt(), trashcan.y.toInt())
                    trashcanBottomRightCoordinate.set(trashcan.x.toInt()+trashcan.width, trashcan.y.toInt()+trashcan.height)

                    mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

            verticalGuideline.setOnTouchListener(WidgetMotionEvent())
            horizontalGuideline.setOnTouchListener(WidgetMotionEvent())
        }
    }
}

 */