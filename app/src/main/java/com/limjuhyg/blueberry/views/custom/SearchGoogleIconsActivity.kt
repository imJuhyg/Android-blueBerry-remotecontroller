package com.limjuhyg.blueberry.views.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.storage.StorageReference
import com.limjuhyg.blueberry.adapter.IconRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.ActivitySearchGoogleIconsBinding
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.getNetworkState
import com.limjuhyg.blueberry.utils.registerNetworkCallback
import com.limjuhyg.blueberry.utils.unregisterNetworkCallback
import com.limjuhyg.blueberry.viewmodels.IconStorageViewModel
import com.limjuhyg.blueberry.views.fragments.WidgetListFragment

class SearchGoogleIconsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchGoogleIconsBinding
    private val iconStorageViewModel by lazy { ViewModelProvider(this).get(IconStorageViewModel::class.java) }
    private var iconRecyclerViewAdapter: IconRecyclerViewAdapter? = null
    private var progressAnimator: ProgressCircleAnimator? = null
    private var fileReferences: List<StorageReference>? = null
    private var topNetworkStateViewInitY: Float = 0f

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Handler(Looper.getMainLooper()).post {
                ObjectAnimator.ofFloat(binding.topNetworkStateTextView, "y", topNetworkStateViewInitY).apply {
                    duration = 500
                    addListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            binding.topNetworkStateTextView.visibility = View.INVISIBLE
                        }
                    })
                    start()
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Handler(Looper.getMainLooper()).post {
                binding.topNetworkStateTextView.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(binding.topNetworkStateTextView, "y", 0f).apply {
                    duration = 500
                    start()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchGoogleIconsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.apply {
                    topNetworkStateViewInitY = topNetworkStateTextView.y - topNetworkStateTextView.height
                }
                binding.mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        iconRecyclerViewAdapter = IconRecyclerViewAdapter(this)
        val gridLayoutManager = GridLayoutManager(this, 3)
        binding.iconRecyclerView.layoutManager = gridLayoutManager
        binding.iconRecyclerView.adapter = iconRecyclerViewAdapter

        progressAnimator = ProgressCircleAnimator(
            binding.progressCircle1, binding.progressCircle2, binding.progressCircle3, 500
        )

        val onProgressObserver = Observer<Boolean> { onProgress ->
            if(onProgress) {
                binding.progressGroup.visibility = View.VISIBLE
                binding.searchEditText.isEnabled = false
                binding.iconRecyclerView.isEnabled = false
                progressAnimator?.startAnimation()

            } else {
                binding.progressGroup.visibility = View.GONE
                binding.searchEditText.isEnabled = true
                binding.iconRecyclerView.isEnabled = true
                progressAnimator?.cancelAnimation()
            }
        }
        iconStorageViewModel.onProgress.observe(this, onProgressObserver)

        val fileReferencesObserver = Observer<List<StorageReference>> {
            if(it.isEmpty()) { // 네트워크 에러가 발생한 경우
                binding.networkGroup.visibility = View.VISIBLE

            } else {
                fileReferences = it

                binding.searchEditText.post {
                    binding.searchEditText.isFocusableInTouchMode = true
                    binding.searchEditText.requestFocus()
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(binding.searchEditText, 0)
                }
            }
        }
        iconStorageViewModel.fileReferences.observe(this, fileReferencesObserver)

        val iconUriListObserver = Observer<ArrayList<Uri>> { list ->
            iconRecyclerViewAdapter!!.clearItem()
            if(list.isEmpty()) {
                Toast.makeText(this, "검색결과가 없습니다", Toast.LENGTH_SHORT).show()

            } else {
                for(uri in list) {
                    iconRecyclerViewAdapter!!.addItem(uri)
                }
            }
        }
        iconStorageViewModel.iconUriList.observe(this, iconUriListObserver)

        if(getNetworkState()) {
            iconStorageViewModel.getFileReferences()
        } else {
            binding.networkGroup.visibility = View.VISIBLE
        }
    }

    /*
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller: WindowInsetsController? = window.insetsController
            controller?.hide(WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }
    }
    */

    override fun onResume() {
        super.onResume()

        registerNetworkCallback(networkCallback)

        binding.btnFinish.setOnClickListener { finish() }

        binding.btnRefresh.setOnClickListener {
            if(getNetworkState()) {
                binding.networkGroup.visibility = View.GONE
                iconStorageViewModel.getFileReferences()
            }
        }

        binding.searchEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(textView.text.length < 2) {
                Toast.makeText(this, "두 자 이상 입력해야 합니다.", Toast.LENGTH_SHORT).show()

            } else if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                fileReferences?.let { references ->
                    val filter = binding.searchEditText.text.toString()
                    iconStorageViewModel.getIconUriList(references, filter)

                } ?: run { // 네트워크 에러가 발생하여 참조할 레퍼런스가 없는데 검색할 경우

                }
            }
            true
        }

        iconRecyclerViewAdapter!!.setItemClickListener(object: IconRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: ImageView, position: Int) {
                val drawable: BitmapDrawable = view.drawable as BitmapDrawable
                val bitmap = drawable.bitmap
                val intent = intent.putExtra("BITMAP", bitmap)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.iconRecyclerView.adapter = null
        iconRecyclerViewAdapter?.clearItem()
        iconRecyclerViewAdapter = null
    }
}