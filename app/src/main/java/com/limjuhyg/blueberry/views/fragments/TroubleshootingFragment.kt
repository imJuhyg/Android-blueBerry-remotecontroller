package com.limjuhyg.blueberry.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentTroubleshootingBinding

class TroubleshootingFragment : Fragment() {
    private var _binding: FragmentTroubleshootingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTroubleshootingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}