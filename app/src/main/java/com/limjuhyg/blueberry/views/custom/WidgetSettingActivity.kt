package com.limjuhyg.blueberry.views.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.databinding.ActivityWidgetSettingBinding
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.utils.addFragmentWithAnimation
import com.limjuhyg.blueberry.utils.pxToDp
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_CREATE_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_MODIFICATION_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.existCustomizeName
import com.limjuhyg.blueberry.views.fragments.WidgetListFragment
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class WidgetSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetSettingBinding
    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private var statusBarHeight = 0
    private var topPanelHeight = 0
    private var verticalGuidelineDefaultCoordinate = 0f
    private var horizontalGuidelineDefaultCoordinate = 0f
    private var verticalGuidelinePivot = 0f
    private var horizontalGuidelinePivot = 0f
    private var widgetDefaultWidth = 0
    private var widgetDefaultHeight = 0
    private val customWidgetList by lazy { ArrayList<CustomWidget>() }
    private val widgetListFragment by lazy { WidgetListFragment() }
    private val widgetInitialCoordinate by lazy { Point() }
    private val trashcanTopLeftCoordinate by lazy { Point() }
    private val trashcanBottomRightCoordinate by lazy { Point() }
    private var widgetCount: Int = 0
    private var isAddedVerticalGuideline: Boolean = false
    private var isAddedHorizontalGuideline: Boolean = false

    companion object {
        lateinit var activity: WidgetSettingActivity

        // Widget modification mode
        const val MODIFICATION_NONE = 0
        const val MODIFICATION_TRANSLATE = 1
        const val MODIFICATION_SCALE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activity = this
        overridePendingTransition(R.anim.to_left_from_right, R.anim.none)

        // Get status bar height
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        binding.apply {
            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    topPanelHeight = topPanel.height // 상단 패널 높이
                    widgetDefaultWidth = pxToDp(mainLayout.width/3).toInt() + 20 // 위젯 기본 가로 길이
                    widgetDefaultHeight = pxToDp(mainLayout.height/3).toInt() // 위젯 기본 세로 길이
                    widgetInitialCoordinate.set(customizeLayout.width/2-widgetDefaultWidth/2, customizeLayout.height/10) // 위젯 기본 좌표
                    verticalGuidelineDefaultCoordinate = verticalGuideline.x // 수직 가이드라인 기본 좌표
                    horizontalGuidelineDefaultCoordinate = horizontalGuideline.y // 수평 가이드라인 기본 좌표
                    verticalGuidelinePivot = verticalGuideline.x + verticalGuideline.width/2 // 수직 가이드라인 x 중심점
                    horizontalGuidelinePivot = horizontalGuideline.y + horizontalGuideline.height/2 // 수평 가이드라인 y 중심점
                    trashcanTopLeftCoordinate.set(trashcan.x.toInt(), trashcan.y.toInt()) // 휴지통 상단 왼쪽 모서리 좌표
                    trashcanBottomRightCoordinate.set(trashcan.x.toInt()+trashcan.width, trashcan.y.toInt()+trashcan.height) // 휴지통 하단 오른쪽 모서리 좌표

                    mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        // Create mode
        if(CustomizeNameSettingActivity.mode == CUSTOMIZE_CREATE_MODE) {
            addWidgetListFragment()
        }

        // Modification mode
        if(CustomizeNameSettingActivity.mode == CUSTOMIZE_MODIFICATION_MODE) {
            binding.shadowPanel.alpha = 0f
            customizeViewModel.getWidgets(existCustomizeName!!) // Load custom widget from DB
        }

        val widgetObserver = Observer<List<Widget>> {
            // Add custom widget to customizeLayout
            for(widget in it) {
                val customWidget = CustomWidget(this).apply {
                    widget.caption?.let { caption -> setWidgetCaption(caption) } ?: run { setCaptionVisibility(false) }
                    setWidgetData(widget.data)
                    setColorFilter(R.color.darkGray)
                    setWidgetImageBitmap(widget.icon)
                    setWidgetCoordination(widget.x, widget.y)
                    setOnTouchListener(WidgetMotionEvent())
                }
                binding.customizeLayout.addView(customWidget, widget.width, widget.height)
                customWidgetList.add(customWidget)
                ++widgetCount
            }
        }
        customizeViewModel.widgets.observe(this, widgetObserver)

        binding.verticalGuideline.setOnTouchListener(WidgetMotionEvent())
        binding.horizontalGuideline.setOnTouchListener(WidgetMotionEvent())
    }

    override fun onResume() {
        super.onResume()

        binding.btnBefore.setOnClickListener {
            finish()
        }

        // Show WidgetListFragment
        binding.btnShowWidgetFragment.setOnClickListener {
            if(widgetCount < 30) {
                addWidgetListFragment()
            }
            else {
                Toast.makeText(this, "최대 30개까지 생성할 수 있습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // Show(Hide) vertical guideline
        binding.btnVerticalGuideline.setOnClickListener {
            if(!isAddedVerticalGuideline) {
                isAddedVerticalGuideline = true
                binding.verticalGuideline.x = verticalGuidelineDefaultCoordinate
                binding.verticalGuideline.y = 0f
                verticalGuidelinePivot = binding.verticalGuideline.x + binding.verticalGuideline.width/2
                binding.verticalGuideline.visibility = View.VISIBLE
                binding.btnVerticalGuideline.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
            }
            else {
                isAddedVerticalGuideline = false
                binding.verticalGuideline.visibility = View.INVISIBLE
                binding.btnVerticalGuideline.clearColorFilter()
            }
        }

        // Show(Hide) horizontal guideline
        binding.btnHorizontalGuideline.setOnClickListener {
            if(!isAddedHorizontalGuideline) {
                isAddedHorizontalGuideline = true
                binding.horizontalGuideline.x = 0f
                binding.horizontalGuideline.y = horizontalGuidelineDefaultCoordinate
                horizontalGuidelinePivot = binding.horizontalGuideline.y + binding.horizontalGuideline.height/2
                binding.horizontalGuideline.visibility = View.VISIBLE
                binding.btnHorizontalGuideline.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
            }
            else {
                isAddedHorizontalGuideline = false
                binding.horizontalGuideline.visibility = View.INVISIBLE
                binding.btnHorizontalGuideline.clearColorFilter()
            }
        }

        // btn next
        binding.btnNext.setOnClickListener {
            tempCustomizeSettingData.setWidgetList(customWidgetList)
            val intent = Intent(this, CustomizeConnectSettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun enableTopPanel(boolean: Boolean) {
        binding.apply {
            btnBefore.isEnabled = boolean
            btnShowWidgetFragment.isEnabled = boolean
            btnVerticalGuideline.isEnabled = boolean
            btnHorizontalGuideline.isEnabled = boolean
            btnNext.isEnabled = boolean
        }
    }

    private fun addWidgetListFragment() {
        // shadow panel animation
        ObjectAnimator.ofFloat(binding.shadowPanel, "alpha", 0.5f).apply {
            duration = 250
            start()
        }

        binding.btnShowWidgetFragment.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
        enableTopPanel(false)
        // add fragment
        addFragmentWithAnimation(binding.fragmentContainer.id, widgetListFragment, R.anim.to_top_from_bottom_1, R.anim.none, false)
    }

    fun refreshView() {
        binding.apply {
            enableTopPanel(true)
            btnShowWidgetFragment.clearColorFilter()

            // shadow panel animation
            ObjectAnimator.ofFloat(binding.shadowPanel, "alpha", 0.0f).apply {
                duration = 250
                start()
            }
        }
    }

    fun createWidget(bitmap: Bitmap, caption: String, data: String) {
        refreshView()
        // Add custom widget to customizeLayout
        val customWidget = CustomWidget(this).apply {
            if(caption.isEmpty()) setCaptionVisibility(false)
            else setWidgetCaption(caption)
            setColorFilter(R.color.darkGray)
            setWidgetImageBitmap(bitmap)
            setWidgetData(data)
            setWidgetCoordination(widgetInitialCoordinate.x.toFloat(), widgetInitialCoordinate.y.toFloat())
            setOnTouchListener(WidgetMotionEvent())
        }

        binding.customizeLayout.addView(customWidget, widgetDefaultWidth, widgetDefaultHeight)
        customWidgetList.add(customWidget)
        ++widgetCount
    }

    private inner class WidgetMotionEvent : View.OnTouchListener {
        private var verticalGuidelineInitRawX = 0f
        private var horizontalGuidelineInitRawY = 0f
        private var widgetInitRawX = 0f
        private var widgetInitRawY = 0f
        private var widgetInitIntervalX = 0f
        private var widgetInitIntervalY = 0f
        private var widgetScale = 1f
        private var widgetModificationMode = MODIFICATION_NONE
        private var animator: AnimatorSet? = null
        private var isViewDeleteMode = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val pointerCount = if(event.pointerCount > 2) 2 else event.pointerCount
            val widgetPivotX = view.x + view.width/2
            val widgetPivotY = view.y + view.height/2

            // widget
            if(view is CustomWidget) {
                when(event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        view.bringToFront()

                        // trashcan animation
                        ObjectAnimator.ofFloat(binding.trashcan, "alpha", 1.0f).apply { // 휴지통 보이기
                            duration = 250
                            start()
                        }

                        // initial touch coordinate
                        if(pointerCount == 1) {
                            widgetModificationMode = MODIFICATION_TRANSLATE
                            widgetInitRawX = event.rawX
                            widgetInitRawY = event.rawY
                        }
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if(pointerCount == 2) {
                            widgetModificationMode = MODIFICATION_SCALE
                            widgetInitIntervalX = abs(event.getX(0) - event.getX(1))
                            widgetInitIntervalY = abs(event.getY(0) - event.getY(1))
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // data visible
                        view.setDataVisibility(true)

                        // widget translate
                        if(pointerCount == 1 && widgetModificationMode == MODIFICATION_TRANSLATE) {
                            view.x += (event.rawX - widgetInitRawX)
                            widgetInitRawX = event.rawX
                            view.y += (event.rawY - widgetInitRawY)
                            widgetInitRawY = event.rawY
                        }

                        // widget-guideline attach
                        if(pointerCount == 1 && isAddedVerticalGuideline) {
                            binding.apply {
                                when(event.rawX) {
                                    in verticalGuideline.x .. (verticalGuideline.x + verticalGuideline.width)
                                    -> view.x = verticalGuidelinePivot - view.width / 2
                                }
                            }
                        }
                        if(pointerCount == 1 && isAddedHorizontalGuideline) {
                            binding.apply {
                                when(event.rawY-(topPanelHeight+statusBarHeight)) {
                                    in horizontalGuideline.y .. horizontalGuideline.y+horizontalGuideline.height
                                    -> view.y = horizontalGuidelinePivot - view.height/2
                                }
                            }
                        }

                        // widget scale
                        if(pointerCount == 2 && widgetModificationMode == MODIFICATION_SCALE) {
                            val intervalX = abs(event.getX(0) - event.getX(1))
                            val intervalY = abs(event.getY(0) - event.getY(1))
                            if(intervalX > widgetInitIntervalX && intervalY > widgetInitIntervalY) { // 확대
                                view.scaleX *= 1.05f
                                view.scaleY *= 1.05f
                            }
                            else if(intervalX < widgetInitIntervalX && intervalY < widgetInitIntervalY) { // 축소
                                view.scaleX *= 0.95f
                                view.scaleY *= 0.95f
                            }
                            widgetScale = view.scaleX // 현재 뷰 스케일 저장
                        }

                        // widget delete
                        if(widgetPivotX > trashcanTopLeftCoordinate.x && widgetPivotX < trashcanBottomRightCoordinate.x
                            && widgetPivotY > trashcanTopLeftCoordinate.y && widgetPivotY < trashcanBottomRightCoordinate.y) { // 뷰가 휴지통 영역 내부일 때
                            if(!isViewDeleteMode) {
                                isViewDeleteMode = true

                                animator = AnimatorSet().apply {
                                    play(ObjectAnimator.ofFloat(view, "scaleX", 0.2f)).apply {
                                        with(ObjectAnimator.ofFloat(view, "scaleY", 0.2f))
                                        with(ObjectAnimator.ofFloat(binding.trashcan, "scaleX", 1.75f))
                                        with(ObjectAnimator.ofFloat(binding.trashcan, "scaleY", 1.75f))
                                    }
                                    duration = 100
                                    start()
                                }
                            }
                        }
                        else {
                            if(isViewDeleteMode) {
                                isViewDeleteMode = false

                                animator = AnimatorSet().apply {
                                    play(ObjectAnimator.ofFloat(view, "scaleX", widgetScale)).apply {
                                        with(ObjectAnimator.ofFloat(view, "scaleY", widgetScale))
                                        with(ObjectAnimator.ofFloat(binding.trashcan, "scaleX", 1f))
                                        with(ObjectAnimator.ofFloat(binding.trashcan, "scaleY", 1f))
                                        duration = 100
                                        start()
                                    }
                                }
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        view.setDataVisibility(false)

                        if(isViewDeleteMode) {
                            --widgetCount
                            binding.customizeLayout.removeView(view)
                            customWidgetList.remove(view)

                            animator = AnimatorSet().apply {
                                play(ObjectAnimator.ofFloat(binding.trashcan, "scaleX", 1f)).apply {
                                    with(ObjectAnimator.ofFloat(binding.trashcan, "scaleY", 1f))
                                    duration = 100
                                    start()
                                }
                            }
                        }

                        ObjectAnimator.ofFloat(binding.trashcan, "alpha", 0.0f).apply { // 휴지통 감추기
                            duration = 250
                            start()
                        }

                        widgetModificationMode = MODIFICATION_NONE
                    }
                }
            }

            // guidelines
            // vertical
            else if(view.id == binding.verticalGuideline.id) {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // initial touch coordinate
                        verticalGuidelineInitRawX = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        binding.apply {
                            // guideline translate
                            verticalGuideline.x += (event.rawX - verticalGuidelineInitRawX)
                            verticalGuidelineInitRawX = event.rawX

                            // set guideline pivot
                            verticalGuidelinePivot = verticalGuideline.x + verticalGuideline.width/2
                        }
                    }
                }
            }
            // horizontal
            else if(view.id == binding.horizontalGuideline.id) {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // initial touch coordinate
                        horizontalGuidelineInitRawY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        binding.apply {
                            // guideline translate
                            horizontalGuideline.y += (event.rawY - horizontalGuidelineInitRawY)
                            horizontalGuidelineInitRawY = event.rawY

                            // set guideline pivot
                            horizontalGuidelinePivot = horizontalGuideline.y + horizontalGuideline.height/2
                        }
                    }
                }
            }
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("debug", "WidgetsContainerActivity destroy")
    }
}