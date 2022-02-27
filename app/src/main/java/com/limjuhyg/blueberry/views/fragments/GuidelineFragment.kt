package com.limjuhyg.blueberry.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentGuidlineBinding
import com.limjuhyg.blueberry.views.guidelines.AppGuidelineActivity
import com.limjuhyg.blueberry.views.guidelines.ArduinoGuidelineActivity
import com.limjuhyg.blueberry.views.guidelines.RaspberryGuidelineActivity

class GuidelineFragment : Fragment() {
    private var _binding: FragmentGuidlineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGuidlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.appGuidelineGroup.setOnClickListener {
            val intent = Intent(requireContext(), AppGuidelineActivity::class.java)
            startActivity(intent)
        }

        binding.raspberryGuidelineGroup.setOnClickListener {
            val intent = Intent(requireContext(), RaspberryGuidelineActivity::class.java)
            startActivity(intent)
        }

        binding.arduinoGuidelineGroup.setOnClickListener {
            val intent = Intent(requireContext(), ArduinoGuidelineActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}