package com.limjuhyg.blueberry.views.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentTroubleshootingBinding
import com.limjuhyg.blueberry.views.troubleshootings.RaspberryTroubleshootingActivity

class TroubleshootingFragment : Fragment() {
    private var _binding: FragmentTroubleshootingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTroubleshootingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.caseRaspberryConnectFailGroup.setOnClickListener {
            val intent = Intent(requireContext(), RaspberryTroubleshootingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}