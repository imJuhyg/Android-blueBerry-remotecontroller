package com.limjuhyg.blueberry.views.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limjuhyg.blueberry.databinding.FragmentContactBinding
import java.lang.Exception

class ContactFragment : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)

        binding.btnMail.setOnClickListener {
            try {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    setPackage("com.google.android.gm")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("blueberry2service@gmail.com"))
                }

                val shareIntent = Intent.createChooser(intent, null)
                startActivity(shareIntent)

            } catch (e: Exception) {

            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}