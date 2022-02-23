package com.limjuhyg.blueberry.views.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.adapter.WidgetRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.FragmentWidgetListBinding
import com.limjuhyg.blueberry.utils.addDefaultWidgetItems
import com.limjuhyg.blueberry.utils.removeFragment
import com.limjuhyg.blueberry.views.custom.SearchGoogleIconsActivity
import com.limjuhyg.blueberry.views.custom.WidgetSettingActivity

class WidgetListFragment : Fragment() {
    private var _binding: FragmentWidgetListBinding? = null
    private val binding get() = _binding!!
    private var widgetRecyclerViewAdapter: WidgetRecyclerViewAdapter? = null
    private lateinit var gestureDetector: GestureDetector
    private val keyboard by lazy { requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }
    private var statusBarHeight: Int = 0
    private var windowHeight: Int = 0
    private var mainLayoutTop: Int = 0
    private var mainLayoutBottom: Int = 0
    private var mainLayoutHeight: Int = 0
    private var bitmapWidget: Bitmap? = null
    private var googleIcon: Bitmap? = null
    private var isFling = false
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWidgetListBinding.inflate(layoutInflater)

        widgetRecyclerViewAdapter = WidgetRecyclerViewAdapter(requireContext())
        binding.widgetRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.widgetRecyclerView.adapter = widgetRecyclerViewAdapter

        // 스크린 높이 구하기
        windowHeight = MainApplication.instance.getWindowHeight()

        // status bar 높이 구하기
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if(resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

        // Google Icons 에서 받아오기
        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                val intent = it.data
                bitmapWidget = null
                googleIcon = intent?.getParcelableExtra("BITMAP")
                showWidgetSettings()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add default widgets
        addDefaultWidgetItems(widgetRecyclerViewAdapter!!)

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
                    isFling = true
                    editTextClear()
                    ObjectAnimator.ofFloat(binding.mainLayout, "translationY", mainLayoutBottom.toFloat()).apply {
                        duration = 200
                        start()
                        addListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                fragmentFinish()
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
                    if(!isFling) {
                        ObjectAnimator.ofFloat(binding.mainLayout, "translationY", mainLayoutTop.toFloat()).apply {
                            duration = 150
                            start()
                        }
                    }
                }
            }
            true
        }

        // Widget click
        widgetRecyclerViewAdapter!!.setOnItemClickListener(object: WidgetRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if(position == 0) {
                    val intent = Intent(requireContext(), SearchGoogleIconsActivity::class.java)
                    activityLauncher.launch(intent)

                } else {
                    val widgetItem = widgetRecyclerViewAdapter!!.getItem(position)
                    bitmapWidget = widgetItem.image
                    showWidgetSettings()
                }
            }
        })

        // Widget data settings
        binding.btnClose.setOnClickListener {
            fragmentFinish()
        }

        binding.btnOk.setOnClickListener {
            if(binding.dataEditText.text.isNotEmpty()) {
                val caption = binding.captionEditText.text.toString()
                val data = binding.dataEditText.text.toString()

                (activity as WidgetSettingActivity).createWidget(bitmapWidget ?: googleIcon!!, caption, data)
                fragmentFinish()
            }
            else {
                Toast.makeText(requireContext(), "데이터에 값을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWidgetSettings() {
        binding.apply {
            widgetListGroup.visibility = View.GONE
            widgetSettingGroup.visibility = View.VISIBLE
            captionEditText.requestFocus()
            keyboard.showSoftInput(captionEditText, 0)
        }
    }

    fun editTextClear() {
        binding.apply {
            captionEditText.setText("")
            dataEditText.setText("")
            keyboard.hideSoftInputFromWindow(captionEditText.windowToken, 0)
            keyboard.hideSoftInputFromWindow(dataEditText.windowToken, 0)
        }
    }

    fun fragmentFinish() {
        editTextClear()
        (activity as WidgetSettingActivity).refreshView()
        removeFragment(this@WidgetListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.widgetRecyclerView.adapter = null
        widgetRecyclerViewAdapter?.clear()
        widgetRecyclerViewAdapter = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isFling = false
        bitmapWidget = null
        googleIcon = null
    }
}