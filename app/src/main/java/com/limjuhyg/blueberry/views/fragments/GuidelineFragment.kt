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
            val uri = Uri.parse("https://github.com/imJuhyg/FirebaseStorage-multi-search-sample")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}