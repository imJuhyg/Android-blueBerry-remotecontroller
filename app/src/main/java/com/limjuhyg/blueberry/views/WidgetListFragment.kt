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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.limjuhyg.blueberry.adapter.WidgetRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.FragmentWidgetListBinding
import com.limjuhyg.blueberry.utils.addDefaultWidgetItems
import com.limjuhyg.blueberry.utils.removeFragment
import com.limjuhyg.blueberry.views.WidgetsContainerFragment.Companion.CAPTION_VALUE
import com.limjuhyg.blueberry.views.WidgetsContainerFragment.Companion.DATA_VALUE
import com.limjuhyg.blueberry.views.WidgetsContainerFragment.Companion.BITMAP_DATA
import com.limjuhyg.blueberry.views.WidgetsContainerFragment.Companion.REQUEST_KEY_NO_DATA
import com.limjuhyg.blueberry.views.WidgetsContainerFragment.Companion.REQUEST_KEY_WIDGET_DATA

class WidgetListFragment : Fragment() {
    private lateinit var binding: FragmentWidgetListBinding
    private val widgetRecyclerViewAdapter by lazy { WidgetRecyclerViewAdapter(requireContext()) }
    private lateinit var gestureDetector: GestureDetector
    private val keyboard by lazy { requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }
    private var screenTop: Int = 0
    private var screenBottom: Int = 0
    private var layoutHeight: Int = 0
    private lateinit var bitmapWidget: Bitmap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWidgetListBinding.inflate(layoutInflater)

        Log.d("debug", "WidgetListFragment create")

        binding.widgetRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.widgetRecyclerView.adapter = widgetRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add default widgets
        addDefaultWidgetItems(widgetRecyclerViewAdapter)

        binding.apply {
            mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    screenTop = mainLayout.top
                    screenBottom = mainLayout.bottom
                    layoutHeight = mainLayout.height

                    mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

            // Attach animation
            mainLayout.translationY = screenBottom.toFloat()
            ObjectAnimator.ofFloat(mainLayout, "translationY", screenTop.toFloat()).apply {
                duration = 250
                start()
            }
        }

        // Fling gesture
        gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (velocityY > 0) {
                    editTextClear()
                    ObjectAnimator.ofFloat(binding.mainLayout, "translationY", screenBottom.toFloat()).apply {
                        duration = 200
                        start()
                        addListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                (parentFragment as WidgetsContainerFragment).hideFragmentContainerGroup()
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
        binding.horizontalBar.setOnTouchListener { view, event ->
            gestureDetector.onTouchEvent(event)

            when(event.action) {
                MotionEvent.ACTION_MOVE -> { // 스크롤뷰 움직이기
                    binding.mainLayout.translationY = event.rawY - layoutHeight
                    if(binding.mainLayout.y < screenTop) {
                        binding.mainLayout.y = screenTop.toFloat()
                    }
                }
                MotionEvent.ACTION_UP -> { // 원위치
                    binding.mainLayout.translationY = screenTop.toFloat()
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
                    outerFrame.isEnabled = false
                    widgetListGroup.visibility = View.GONE
                    widgetSettingGroup.visibility = View.VISIBLE
                    searchEditText.setText("")
                    captionEditText.requestFocus()
                    keyboard.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                    keyboard.showSoftInput(captionEditText, 0)
                }
            }
        })

        // outer frame
        binding.outerFrame.setOnClickListener {
            editTextClear()
            ObjectAnimator.ofFloat(binding.mainLayout, "translationY", screenBottom.toFloat()).apply {
                duration = 200
                start()
                addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        (parentFragment as WidgetsContainerFragment).hideFragmentContainerGroup()
                        removeFragment(this@WidgetListFragment)
                    }
                })
            }
        }

        // Widget data settings
        binding.btnClose.setOnClickListener {
            editTextClear()
            (parentFragment as WidgetsContainerFragment).hideFragmentContainerGroup()
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