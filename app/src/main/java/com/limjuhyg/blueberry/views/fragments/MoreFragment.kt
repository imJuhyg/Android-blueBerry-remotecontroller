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
import com.limjuhyg.blueberry.utils.addChildFragment
import com.limjuhyg.blueberry.utils.hideChildFragment
import com.limjuhyg.blueberry.utils.showChildFragment

class MoreFragment : Fragment() {
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!
    private val menuViewList by lazy { ArrayList<TextView>() }
    private val guidelineFragment by lazy { GuidelineFragment() }
    private var troubleshootingFragment: TroubleshootingFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(layoutInflater, container, false)

        menuViewList.apply {
            add(binding.guideline)
            add(binding.troubleshooting)
            add(binding.example)
            add(binding.contact)
        }

        addChildFragment(binding.fragmentContainer.id, guidelineFragment, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.guideline.setOnClickListener { view ->
            onMenuItemSelected(view)
            setMenuTracerAnimation(view) // Animation
            setSelectedMenuColor(view) // Set text color

        }
        binding.troubleshooting.setOnClickListener { view ->
            onMenuItemSelected(view)
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

    private fun onMenuItemSelected(view: View) {
        for(fragment in childFragmentManager.fragments) hideChildFragment(fragment)
        when(view.id) {
            binding.guideline.id -> {
                showChildFragment(guidelineFragment)
            }

            binding.troubleshooting.id -> {
                troubleshootingFragment?.let { showChildFragment(troubleshootingFragment!!) } ?: run {
                    troubleshootingFragment = TroubleshootingFragment()
                    addChildFragment(binding.fragmentContainer.id, troubleshootingFragment!!, false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}