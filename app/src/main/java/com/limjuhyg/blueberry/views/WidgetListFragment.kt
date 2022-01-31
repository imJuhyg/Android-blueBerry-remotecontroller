package com.limjuhyg.blueberry.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.limjuhyg.blueberry.MainApplication
import com.limjuhyg.blueberry.adapter.WidgetRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.FragmentWidgetListBinding
import com.limjuhyg.blueberry.utils.addDefaultWidgetItems
import com.limjuhyg.blueberry.utils.removeFragment

class WidgetListFragment : Fragment() {
    private lateinit var binding: FragmentWidgetListBinding
    private val widgetRecyclerViewAdapter by lazy { WidgetRecyclerViewAdapter(requireContext()) }
    private lateinit var gestureDetector: GestureDetector
    private val keyboard by lazy { requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }
    private var statusBarHeight: Int = 0
    private var windowHeight: Int = 0
    private var mainLayoutTop: Int = 0
    private var mainLayoutBottom: Int = 0
    private var mainLayoutHeight: Int = 0
    private lateinit var bitmapWidget: Bitmap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWidgetListBinding.inflate(layoutInflater)

        binding.widgetRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.widgetRecyclerView.adapter = widgetRecyclerViewAdapter

        // 스크린 높이 구하기
        windowHeight = MainApplication.instance.getWindowHeight()

        // status bar 높이 구하기
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add default widgets
        addDefaultWidgetItems(widgetRecyclerViewAdapter)

        binding.apply {
            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mainLayoutTop = mainLayout.top
                    mainLayoutBottom = mainLayout.bottom
                    mainLayoutHeight = mainLayout.height

                    mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        // Fling gesture
        gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (velocityY > 0) {
                    editTextClear()
                    ObjectAnimator.ofFloat(binding.mainLayout, "translationY", mainLayoutBottom.toFloat()).apply {
                        duration = 200
                        start()
                        addListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                (parentFragment as WidgetsContainerFragment).refreshView()
                                removeFragment(this@WidgetListFragment)
                            }
                        })
                    }
                }
                return true
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        // horizontal bar interaction
        binding.horizontalBar.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)

            when(event.action) {
                MotionEvent.ACTION_MOVE -> { // 위젯 리스트 움직이기
                    binding.mainLayout.translationY = event.rawY - statusBarHeight - (windowHeight - mainLayoutHeight)

                    if(binding.mainLayout.y < mainLayoutTop) {
                        binding.mainLayout.y = mainLayoutTop.toFloat()
                    }
                }
                MotionEvent.ACTION_UP -> { // 원위치
                    binding.mainLayout.translationY = mainLayoutTop.toFloat()
                }
            }
            true
        }

        // Widget click
        widgetRecyclerViewAdapter.setOnItemClickListener(object: WidgetRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val widgetItem = widgetRecyclerViewAdapter.getItem(position)
                bitmapWidget = widgetItem.image

                binding.apply {
                    widgetListGroup.visibility = View.GONE
                    widgetSettingGroup.visibility = View.VISIBLE
                    searchEditText.setText("")
                    captionEditText.requestFocus()
                    keyboard.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                    keyboard.showSoftInput(captionEditText, 0)
                }
            }
        })

        // Widget data settings
        binding.btnClose.setOnClickListener {
            editTextClear()
            (parentFragment as WidgetsContainerFragment).refreshView()
            removeFragment(this@WidgetListFragment)
        }

        binding.btnOk.setOnClickListener {
            if(binding.dataEditText.text.isNotEmpty()) {
                val caption = binding.captionEditText.text.toString()
                val data = binding.dataEditText.text.toString()

                editTextClear()
                (parentFragment as WidgetsContainerFragment).createWidget(bitmapWidget, caption, data)
                removeFragment(this@WidgetListFragment)
            }
            else {
                Toast.makeText(requireContext(), "데이터에 값을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun editTextClear() {
        binding.apply {
            searchEditText.setText("")
            captionEditText.setText("")
            dataEditText.setText("")
            keyboard.hideSoftInputFromWindow(captionEditText.windowToken, 0)
            keyboard.hideSoftInputFromWindow(dataEditText.windowToken, 0)
            keyboard.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("debug", "WidgetListFragment destroy")

        widgetRecyclerViewAdapter.clear()
    }
}