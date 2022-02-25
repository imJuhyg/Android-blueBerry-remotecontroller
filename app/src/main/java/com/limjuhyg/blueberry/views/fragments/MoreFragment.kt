package com.limjuhyg.blueberry.views.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!
    private val menuViewList by lazy { ArrayList<TextView>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(layoutInflater, container, false)

        menuViewList.apply {
            add(binding.learning)
            add(binding.troubleshooting)
            add(binding.example)
            add(binding.contact)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.learning.setOnClickListener { view ->
            setMenuTracerAnimation(view) // Animation
            setSelectedMenuColor(view) // Set text color

        }
        binding.troubleshooting.setOnClickListener { view ->
            setMenuTracerAnimation(view) // Animation
            setSelectedMenuColor(view) // Set text color

        }
        binding.example.setOnClickListener { view ->
            setMenuTracerAnimation(view) // Animation
            setSelectedMenuColor(view) // Set text color

        }
        binding.contact.setOnClickListener { view ->
            setMenuTracerAnimation( view) // Animation
            setSelectedMenuColor(view) // Set text color

        }
    }

    private fun setMenuTracerAnimation(view: View) {
        val currentWidth = binding.menuTracer.width
        val newWidth = view.width

        val setPositionAnimator =
            ObjectAnimator.ofFloat(binding.menuTracer, "x", view.x).setDuration(100)

        val setWidthAnimator =
            ValueAnimator.ofInt(currentWidth, newWidth).setDuration(100)

        setWidthAnimator.addUpdateListener { animator ->
            val animationValue: Int = animator.animatedValue as Int
            val layoutParams = binding.menuTracer.layoutParams
            layoutParams.width = animationValue
            binding.menuTracer.layoutParams = layoutParams
        }

        AnimatorSet().apply {
            play(setPositionAnimator).with(setWidthAnimator)
            start()
        }
    }

    private fun setSelectedMenuColor(view: View) {
        val selectedView: TextView = view as TextView
        for(menuView in menuViewList) {
            menuView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        }
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.customBlack))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}