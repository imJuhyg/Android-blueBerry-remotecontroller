package com.limjuhyg.blueberry.views

import android.os.Bundle
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.adapter.WidgetRecyclerViewAdapter
import com.limjuhyg.blueberry.databinding.FragmentWidgetListBinding
import com.limjuhyg.blueberry.utils.addDefaultWidgetItems

class WidgetListFragment : Fragment() {
    private lateinit var binding: FragmentWidgetListBinding
    private val widgetRecyclerViewAdapter by lazy { WidgetRecyclerViewAdapter(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWidgetListBinding.inflate(layoutInflater)

        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.widgetRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.widgetRecyclerView.adapter = widgetRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add default widgets
        addDefaultWidgetItems(widgetRecyclerViewAdapter)
    }
}