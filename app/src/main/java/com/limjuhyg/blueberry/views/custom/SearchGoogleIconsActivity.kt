package com.limjuhyg.blueberry.views.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.BitmapDrawable
import android.net.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.limjuhyg.blueberry.viewmodels.IconStorageViewModel

class SearchGoogleIconsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchGoogleIconsBinding
    private val iconStorageViewModel by lazy { ViewModelProvider(this).get(IconStorageViewModel::class.java) }
    private var iconRecyclerViewAdapter: IconRecyclerViewAdapter? = null
    private var progressAnimator: ProgressCircleAnimator? = null
    private var fileReferences: List<StorageReference>? = null
    private var topNetworkStateViewInitY: Float = 0f
    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var onAvailableHandler: Handler? = Handler(Looper.getMainLooper())
    private var onLostHandler: Handler? = Handler(Looper.getMainLooper())

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            onAvailableHandler?.post {
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
            onLostHandler?.post {
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

        val orientation = intent.getStringExtra("ORIENTATION")
        if(orientation == "landscape") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

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
            if(it.isEmpty()) { // ???????????? ????????? ????????? ??????
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
                Toast.makeText(applicationContext, "??????????????? ????????????", Toast.LENGTH_SHORT).show()

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

        binding.searchEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(textView.text.length < 2) {
                Toast.makeText(applicationContext, "??? ??? ?????? ???????????? ?????????.", Toast.LENGTH_SHORT).show()

            } else if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                fileReferences?.let { references ->
                    val filter = binding.searchEditText.text.toString()
                    iconStorageViewModel.getIconUriList(references, filter)

                } ?: run { // ???????????? ????????? ???????????? ????????? ??????????????? ????????? ????????? ??????

                }
            }
            true
        }

        // click listener

        binding.btnFinish.setOnClickListener { finish() }

        binding.btnRefresh.setOnClickListener {
            if(getNetworkState()) {
                binding.networkGroup.visibility = View.GONE
                iconStorageViewModel.getFileReferences()
            }
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

    override fun onResume() {
        super.onResume()

        registerNetworkCallback(networkCallback)
    }

    private fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.iconRecyclerView.adapter = null
        iconRecyclerViewAdapter?.clearItem()
        iconRecyclerViewAdapter = null
        progressAnimator?.cancelAnimation()
        progressAnimator = null
        onAvailableHandler?.removeMessages(0)
        onLostHandler?.removeMessages(0)
        onAvailableHandler = null
        onLostHandler = null
    }
}