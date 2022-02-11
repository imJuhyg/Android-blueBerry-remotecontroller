package com.limjuhyg.blueberry.views.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.adapter.LogRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.FragmentCommunicationLogBinding

class CommunicationLogFragment : Fragment() {
    private var _binding: FragmentCommunicationLogBinding? = null
    private val binding get() = _binding!!
    private var deviceName: String? = null
    private var logRecyclerViewAdapter: LogRecyclerViewAdapter? = null
    private var layoutBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceName = arguments?.getString("DEVICE_NAME")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCommunicationLogBinding.inflate(inflater, container, false)

        binding.deviceNameTextView.text = deviceName
        logRecyclerViewAdapter = LogRecyclerViewAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = logRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainLayout.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layoutBottom = binding.mainLayout.bottom
                binding.mainLayout.translationY = layoutBottom.toFloat()
                binding.mainLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun addLogItem(type: Int, time: Long, data: String) {
        logRecyclerViewAdapter!!.addItem(type, time, data)
        binding.recyclerView.smoothScrollToPosition(logRecyclerViewAdapter!!.itemCount-1)
    }

    fun show() {
        ObjectAnimator.ofFloat(binding.mainLayout, "translationY", 0f).apply {
            duration = 200
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationStart(animation, isReverse)
                    binding.mainLayout.translationY = layoutBottom.toFloat()
                    parentFragmentManager.beginTransaction().show(this@CommunicationLogFragment).commit()
                }
            })
            start()
        }
    }

    fun hide() {
        ObjectAnimator.ofFloat(binding.mainLayout, "translationY", layoutBottom.toFloat()).apply {
            duration = 200
            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    parentFragmentManager.beginTransaction().hide(this@CommunicationLogFragment).commit()
                }
            })
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logRecyclerViewAdapter?.clear()
        logRecyclerViewAdapter = null
        _binding = null
    }
}