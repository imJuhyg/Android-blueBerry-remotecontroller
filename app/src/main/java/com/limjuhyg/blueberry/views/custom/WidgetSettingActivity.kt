package com.limjuhyg.blueberry.views.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.customviews.CustomWidget
import com.limjuhyg.blueberry.dataclass.TempCustomizeSettingData
import com.limjuhyg.blueberry.models.room.entities.Widget
import com.limjuhyg.blueberry.utils.replaceFragmentWithAnimation
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_CREATE_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_MODIFICATION_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.oldCustomizeName
import com.limjuhyg.blueberry.views.fragments.WidgetListFragment
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class WidgetSettingActivity : AppCompatActivity() {
    private val mainLayout: ConstraintLayout by lazy { findViewById(R.id.main_layout) }
    private val topPanel: Group by lazy { findViewById(R.id.top_panel) }
    private val btnBefore: ImageButton by lazy { findViewById(R.id.btn_before) }
    private val btnShowWidgetFragment: ImageButton by lazy { findViewById(R.id.btn_show_widget_fragment) }
    private val btnVerticalGuideline: ImageButton by lazy { findViewById(R.id.btn_vertical_guideline) }
    private val btnHorizontalGuideline: ImageButton by lazy { findViewById(R.id.btn_horizontal_guideline) }
    private val btnRotation: ImageButton by lazy { findViewById(R.id.btn_rotation) }
    private val btnNext: Button by lazy { findViewById(R.id.btn_next) }
    private val customizeLayout: ConstraintLayout by lazy { findViewById(R.id.customize_layout) }
    private val trashcan: ImageView by lazy { findViewById(R.id.trashcan) }
    private val verticalGuideline: ConstraintLayout by lazy { findViewById(R.id.vertical_guideline) }
    private val horizontalGuideline: ConstraintLayout by lazy { findViewById(R.id.horizontal_guideline) }
    private val shadowPanel: View by lazy { findViewById(R.id.shadow_panel) }
    private val fragmentContainer: FrameLayout by lazy { findViewById(R.id.fragment_container) }

    private val tempCustomizeSettingData by lazy { TempCustomizeSettingData.getInstance() }
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private var statusBarHeight = 0
    private var topPanelHeight = 0 // 세로 모드 전용
    private var leftPanelWidth = 0 // 가로 모드 전용
    private var leftSpaceWidth = 0
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
    var orientation: String = "portrait"

    companion object {
        lateinit var activity: WidgetSettingActivity

        // Widget modification mode
        const val MODIFICATION_NONE = 0
        const val MODIFICATION_TRANSLATE = 1
        const val MODIFICATION_SCALE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = "portrait"
            setContentView(R.layout.activity_widget_setting)
        } else {
            orientation = "landscape"
            setContentView(R.layout.activity_widget_setting_landscape)
        }

        activity = this
        overridePendingTransition(R.anim.to_left_from_right, R.anim.none)

        // Get status bar height
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    leftPanelWidth = 0
                    topPanelHeight = topPanel.height // 상단 패널 높이
                } else {
                    leftSpaceWidth = statusBarHeight
                    leftPanelWidth = topPanel.width
                    topPanelHeight = 0 // 가로 모드일 경우
                }

                // 위젯 기본 가로 길이
                widgetDefaultWidth = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mainLayout.width/5 // 세로모드
                } else {
                    mainLayout.width/7 // 가로모드
                }
                // 위젯 기본 세로 길이
                widgetDefaultHeight = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mainLayout.height/7 // 세로모드
                } else {
                    mainLayout.height/5 // 가로모드
                }

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

        // Create mode
        if(CustomizeNameSettingActivity.mode == CUSTOMIZE_CREATE_MODE) {
            addWidgetListFragment()
        }

        // Modification mode
        if(CustomizeNameSettingActivity.mode == CUSTOMIZE_MODIFICATION_MODE) {
            shadowPanel.alpha = 0f
            btnRotation.visibility = View.GONE
            customizeViewModel.getOrientation(oldCustomizeName!!) // 화면 전환
            customizeViewModel.getWidgets(oldCustomizeName!!) // Load custom widget from DB
        }

        customizeViewModel.orientation.observe(this, { orientation ->
            if(orientation == "landscape") requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        })

        val widgetObserver = Observer<List<Widget>> {
            // Add existing widget to customizeLayout
            if(widgetCount == 0) {
                for(widget in it) {
                    val customWidget = CustomWidget(this).apply {
                        widget.caption?.let { caption -> setWidgetCaption(caption) } ?: run { setCaptionVisibility(false) }
                        setWidgetData(widget.data)
                        setColorFilter(R.color.darkGray)
                        setWidgetImageBitmap(widget.icon)
                        setWidgetScale(widget.scale)
                        setWidgetCoordination(widget.x, widget.y)
                        setOnTouchListener(WidgetMotionEvent())
                    }
                    customizeLayout.addView(customWidget, widget.width, widget.height)
                    customWidgetList.add(customWidget)
                    ++widgetCount
                }
            }
        }
        customizeViewModel.widgets.observe(this, widgetObserver)

        verticalGuideline.setOnTouchListener(WidgetMotionEvent())
        horizontalGuideline.setOnTouchListener(WidgetMotionEvent())

        // click listener
        btnBefore.setOnClickListener { finish() }

        // Show WidgetListFragment
        btnShowWidgetFragment.setOnClickListener {
            if(widgetCount < 30) {
                addWidgetListFragment()
            }
            else {
                Toast.makeText(applicationContext, "최대 30개까지 생성할 수 있습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // Show(Hide) vertical guideline
        btnVerticalGuideline.setOnClickListener {
            if(!isAddedVerticalGuideline) {
                isAddedVerticalGuideline = true
                verticalGuideline.x = verticalGuidelineDefaultCoordinate
                verticalGuideline.y = 0f
                verticalGuidelinePivot = verticalGuideline.x + verticalGuideline.width/2
                verticalGuideline.visibility = View.VISIBLE
                btnVerticalGuideline.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
            }
            else {
                isAddedVerticalGuideline = false
                verticalGuideline.visibility = View.INVISIBLE
                btnVerticalGuideline.clearColorFilter()
            }
        }

        // Show(Hide) horizontal guideline
        btnHorizontalGuideline.setOnClickListener {
            if(!isAddedHorizontalGuideline) {
                isAddedHorizontalGuideline = true
                horizontalGuideline.x = 0f
                horizontalGuideline.y = horizontalGuidelineDefaultCoordinate
                horizontalGuidelinePivot = horizontalGuideline.y + horizontalGuideline.height/2
                horizontalGuideline.visibility = View.VISIBLE
                btnHorizontalGuideline.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
            }
            else {
                isAddedHorizontalGuideline = false
                horizontalGuideline.visibility = View.INVISIBLE
                btnHorizontalGuideline.clearColorFilter()
            }
        }

        // btn next
        btnNext.setOnClickListener {
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                tempCustomizeSettingData.setOrientation("portrait")
            } else {
                tempCustomizeSettingData.setOrientation("landscape")
            }
            tempCustomizeSettingData.setWidgetList(customWidgetList)
            val intent = Intent(this, CustomizeConnectSettingActivity::class.java)
            startActivity(intent)
        }

        // rotation
        btnRotation.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok_cancel, null, false)
            val title: TextView = dialogView.findViewById(R.id.title)
            val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
            title.text = "설정된 데이터가 모두 지워집니다"
            subtitle.text = "화면전환하시겠습니까?"

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)
            builder.setCancelable(false)

            val alertDialog = builder.create()
            alertDialog.show()

            val okButton: Button = dialogView.findViewById(R.id.btn_ok)
            val cancelButton: Button = dialogView.findViewById(R.id.btn_cancel)
            okButton.setOnClickListener {
                requestedOrientation = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // 가로 모드로 변경
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 세로 모드로 변경
                }
                alertDialog.dismiss()
            }
            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }
        }
    }

    private fun enableTopPanel(boolean: Boolean) {
        btnBefore.isEnabled = boolean
        btnShowWidgetFragment.isEnabled = boolean
        btnVerticalGuideline.isEnabled = boolean
        btnHorizontalGuideline.isEnabled = boolean
        btnNext.isEnabled = boolean
        btnRotation.isEnabled = boolean
    }

    private fun addWidgetListFragment() {
        // shadow panel animation
        ObjectAnimator.ofFloat(shadowPanel, "alpha", 0.5f).apply {
            duration = 250
            start()
        }

        btnShowWidgetFragment.setColorFilter(ContextCompat.getColor(this, R.color.identityColor))
        enableTopPanel(false)
        // add fragment
        replaceFragmentWithAnimation(fragmentContainer.id, widgetListFragment, R.anim.to_top_from_bottom_1, R.anim.none, false)
    }

    fun refreshView() {
        enableTopPanel(true)
        btnShowWidgetFragment.clearColorFilter()

        // shadow panel animation
        ObjectAnimator.ofFloat(shadowPanel, "alpha", 0.0f).apply {
            duration = 250
            start()
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

        customizeLayout.addView(customWidget, widgetDefaultWidth, widgetDefaultHeight)
        customWidgetList.add(customWidget)
        ++widgetCount
    }

    private inner class WidgetMotionEvent : View.OnTouchListener {
        private var verticalGuidelineInitRawX = 0f
        private var horizontalGuidelineInitRawY = 0f
        private var widgetInitRawX = 0f
        private var widgetInitRawY = 0f
        private var lastPointerDistanceX = 0f
        private var lastPointerDistanceY = 0f
        private var widgetModificationMode = MODIFICATION_NONE
        private var animator: AnimatorSet? = null
        private var isViewDeleteMode = false
        private var lastWidgetScale = 1f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val pointerCount = if(event.pointerCount > 2) 2 else event.pointerCount
            val widgetPivotX = view.x + view.width/2
            val widgetPivotY = view.y + view.height/2

            // widget
            if(view is CustomWidget) {
                when(event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        view.bringToFront()

                        lastWidgetScale = view.scaleX // save current widget scale

                        // trashcan animation
                        ObjectAnimator.ofFloat(trashcan, "alpha", 1.0f).apply { // 휴지통 보이기
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
                            lastPointerDistanceX = abs(event.getX(0) - event.getX(1))
                            lastPointerDistanceY = abs(event.getY(0) - event.getY(1))
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        view.setDataVisibility(true) // data visible

                        // widget translate
                        if(pointerCount == 1 && widgetModificationMode == MODIFICATION_TRANSLATE) {
                            view.x += (event.rawX - widgetInitRawX)
                            widgetInitRawX = event.rawX
                            view.y += (event.rawY - widgetInitRawY)
                            widgetInitRawY = event.rawY
                        }

                        // widget-guideline attach
                        if(pointerCount == 1 && isAddedVerticalGuideline) {
                            when(event.rawX-(leftPanelWidth+leftSpaceWidth)) {
                                in verticalGuideline.x .. (verticalGuideline.x + verticalGuideline.width)
                                -> view.x = verticalGuidelinePivot - view.width / 2
                            }
                        }
                        if(pointerCount == 1 && isAddedHorizontalGuideline) {
                            when(event.rawY-(topPanelHeight+statusBarHeight)) {
                                in horizontalGuideline.y .. horizontalGuideline.y+horizontalGuideline.height
                                -> view.y = horizontalGuidelinePivot - view.height/2
                            }
                        }

                        // widget scale
                        if(pointerCount == 2 && widgetModificationMode == MODIFICATION_SCALE) {
                            val pointerDistanceX = abs(event.getX(0) - event.getX(1))
                            val pointerDistanceY = abs(event.getY(0) - event.getY(1))

                            if(pointerDistanceX > lastPointerDistanceX && pointerDistanceY > lastPointerDistanceY) { // 확대
                                view.scaleX *= 1.05f
                                view.scaleY *= 1.05f
                            }
                            else if(pointerDistanceX < lastPointerDistanceX && pointerDistanceY < lastPointerDistanceY) { // 축소
                                view.scaleX *= 0.95f
                                view.scaleY *= 0.95f
                            }
                        }

                        // widget delete
                        if(widgetPivotX > trashcanTopLeftCoordinate.x && widgetPivotX < trashcanBottomRightCoordinate.x
                            && widgetPivotY > trashcanTopLeftCoordinate.y && widgetPivotY < trashcanBottomRightCoordinate.y) {
                            // 뷰가 휴지통 영역 내부일 때
                            if(!isViewDeleteMode) {
                                isViewDeleteMode = true
                                animator = AnimatorSet().apply {
                                    play(ObjectAnimator.ofFloat(view, "scaleX", 0.2f)).apply {
                                        with(ObjectAnimator.ofFloat(view, "scaleY", 0.2f))
                                        with(ObjectAnimator.ofFloat(trashcan, "scaleX", 1.75f))
                                        with(ObjectAnimator.ofFloat(trashcan, "scaleY", 1.75f))
                                    }
                                    duration = 100
                                    start()
                                }
                            }
                        }
                        else {
                            // 뷰가 휴지통 영역 내부였다가 벗어났을 때
                            if(isViewDeleteMode) {
                                isViewDeleteMode = false
                                animator = AnimatorSet().apply {
                                    play(ObjectAnimator.ofFloat(view, "scaleX", lastWidgetScale)).apply {
                                        with(ObjectAnimator.ofFloat(view, "scaleY", lastWidgetScale))
                                        with(ObjectAnimator.ofFloat(trashcan, "scaleX", 1f))
                                        with(ObjectAnimator.ofFloat(trashcan, "scaleY", 1f))
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
                            customizeLayout.removeView(view)
                            customWidgetList.remove(view)

                            animator = AnimatorSet().apply {
                                play(ObjectAnimator.ofFloat(trashcan, "scaleX", 1f)).apply {
                                    with(ObjectAnimator.ofFloat(trashcan, "scaleY", 1f))
                                    duration = 100
                                    start()
                                }
                            }
                        }

                        ObjectAnimator.ofFloat(trashcan, "alpha", 0.0f).apply { // 휴지통 감추기
                            duration = 250
                            start()
                        }

                        widgetModificationMode = MODIFICATION_NONE
                    }
                }
            }

            // guidelines
            // vertical
            else if(view.id == verticalGuideline.id) {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // initial touch coordinate
                        verticalGuidelineInitRawX = event.rawX
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // guideline translate
                        verticalGuideline.x += (event.rawX - verticalGuidelineInitRawX)
                        verticalGuidelineInitRawX = event.rawX

                        // set guideline pivot
                        verticalGuidelinePivot = verticalGuideline.x + verticalGuideline.width/2
                    }
                }
            }
            // horizontal
            else if(view.id == horizontalGuideline.id) {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // initial touch coordinate
                        horizontalGuidelineInitRawY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // guideline translate
                        horizontalGuideline.y += (event.rawY - horizontalGuidelineInitRawY)
                        horizontalGuidelineInitRawY = event.rawY

                        // set guideline pivot
                        horizontalGuidelinePivot = horizontalGuideline.y + horizontalGuideline.height/2
                    }
                }
            }
            return true
        }
    }
}