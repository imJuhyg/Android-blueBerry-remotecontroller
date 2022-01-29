package com.limjuhyg.blueberry.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.databinding.FragmentWidgetsContainerBinding
import com.limjuhyg.blueberry.utils.addChildFragment
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class WidgetsContainerFragment : Fragment() {
    private lateinit var binding: FragmentWidgetsContainerBinding
    private var statusBarHeight = 0
    private var topPanelHeight = 0
    private var verticalGuidelineDefaultCoordinate = 0f
    private var horizontalGuidelineDefaultCoordinate = 0f
    private var verticalGuidelinePivot = 0f
    private var horizontalGuidelinePivot = 0f
    private var widgetDefaultWidth: Int = 0
    private var widgetDefaultHeight: Int = 0
    private val widgetListFragment by lazy { WidgetListFragment() }
    private val widgetInitialCoordinate by lazy { Point() }
    private val trashcanTopLeftCoordinate by lazy { Point() }
    private val trashcanBottomRightCoordinate by lazy { Point() }
    private var widgetCount: Int = 0
    private var isAddedVerticalGuideline: Boolean = false
    private var isAddedHorizontalGuideline: Boolean = false

    companion object {
        // Request Keys
        const val REQUEST_KEY_NO_DATA = "NO_DATA"
        const val REQUEST_KEY_WIDGET_DATA = "WIDGET_DATA"

        // Bundle Keys
        const val BITMAP_DATA = "BITMAP_DATA"
        const val DATA_VALUE = "DATA_VALUE"
        const val CAPTION_VALUE = "CAPTION_VALUE"

        // Widget modification mode
        const val NONE = 0
        const val TRANSLATE = 1
        const val SCALE = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWidgetsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addChildFragment(binding.fragmentContainer.id, widgetListFragment, false)
        binding.btnShowWidgetFragment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.identityColor))

        // status bar 높이 구하기
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        binding.apply {
            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    topPanelHeight = topPanel.height
                    widgetDefaultWidth = mainLayout.width/5 // TODO 디폴트 크기를 dp단위로 변경
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

    fun hideFragmentContainerGroup() {
        binding.apply {
            fragmentContainerGroup.visibility = View.GONE
            btnShowWidgetFragment.isEnabled = true
            btnShowWidgetFragment.clearColorFilter()
        }
    }

    fun createWidget(bitmap: Bitmap, caption: String, data: String) {
        hideFragmentContainerGroup()
        // Add custom widget
        val customWidget = CustomWidget(requireContext()).apply {
            if(caption.isEmpty()) setCaptionVisibility(false)
            else setWidgetCaption(caption)
            setColorFilter(R.color.darkGray)
            setWidgetImageBitmap(bitmap)
            setWidgetData(data)
            setWidgetCoordination(widgetInitialCoordinate.x.toFloat()-width/2, widgetInitialCoordinate.y.toFloat())
            setOnTouchListener(WidgetMotionEvent())
        }
        binding.fragmentContainerGroup.visibility = View.GONE
        binding.customizeLayout.addView(customWidget, widgetDefaultWidth, widgetDefaultHeight)
        ++widgetCount
    }

    override fun onResume() {
        super.onResume()

        // Show WidgetListFragment
        binding.btnShowWidgetFragment.setOnClickListener {
            if(widgetCount < 2) {
                binding.fragmentContainerGroup.visibility = View.VISIBLE
                binding.btnShowWidgetFragment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.identityColor))
                addChildFragment(binding.fragmentContainer.id, widgetListFragment, false)
                it.isEnabled = false
            }
            else {
                Toast.makeText(requireContext(), "최대 30개까지 생성할 수 있습니다", Toast.LENGTH_SHORT).show()
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
                binding.btnVerticalGuideline.setColorFilter(ContextCompat.getColor(requireContext(), R.color.identityColor))
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
                binding.btnHorizontalGuideline.setColorFilter(ContextCompat.getColor(requireContext(), R.color.identityColor))
            }
            else {
                isAddedHorizontalGuideline = false
                binding.horizontalGuideline.visibility = View.INVISIBLE
                binding.btnHorizontalGuideline.clearColorFilter()
            }
        }
    }

    private inner class WidgetMotionEvent : View.OnTouchListener {
        private var verticalGuidelineInitRawX = 0f
        private var horizontalGuidelineInitRawY = 0f
        private var widgetInitRawX = 0f
        private var widgetInitRawY = 0f
        private var widgetInitIntervalX = 0f
        private var widgetInitIntervalY = 0f
        private var widgetScale = 1f
        private var widgetModificationMode = NONE
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
                            widgetModificationMode = TRANSLATE
                            widgetInitRawX = event.rawX
                            widgetInitRawY = event.rawY
                        }
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if(pointerCount == 2) {
                            widgetModificationMode = SCALE
                            widgetInitIntervalX = abs(event.getX(0) - event.getX(1))
                            widgetInitIntervalY = abs(event.getY(0) - event.getY(1))
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // data visible
                        view.setDataVisibility(true)

                        // widget translate
                        if(pointerCount == 1 && widgetModificationMode == TRANSLATE) {
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
                        if(pointerCount == 2 && widgetModificationMode == SCALE) {
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

                        widgetModificationMode = NONE
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
                            Log.d("V", verticalGuidelinePivot.toString())
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
        Log.d("debug", "WidgetsContainerFragment destroy")
    }
}