package com.limjuhyg.blueberry.views.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.applications.MainApplication.Companion.FROM_CLIENT
import com.limjuhyg.blueberry.applications.MainApplication.Companion.TO_SERVER
import com.limjuhyg.blueberry.databinding.FragmentConnectWayBinding

class ConnectWayFragment : Fragment() {
    private var _binding: FragmentConnectWayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConnectWayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.toServerGroup.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("CONNECT_WAY", TO_SERVER)
            setFragmentResult("CONNECT_WAY_RESULT", bundle)
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        binding.fromClientGroup.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("CONNECT_WAY", FROM_CLIENT)
            setFragmentResult("CONNECT_WAY_RESULT", bundle)
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("fragment", " onDestroy")
    }
}