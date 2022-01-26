package com.totheptv.blueberry.views

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.totheptv.blueberry.R
import com.totheptv.blueberry.databinding.FragmentCustomizeBinding
import com.totheptv.blueberry.recyclerviews.CustomizeRecyclerViewAdapter

class CustomizeFragment : Fragment() {
    private lateinit var binding: FragmentCustomizeBinding
    private val customizeRecyclerViewAdapter by lazy { CustomizeRecyclerViewAdapter(requireContext()) }
    private lateinit var customSettingActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCustomizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.customizeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.customizeRecyclerView.adapter = customizeRecyclerViewAdapter

        customSettingActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                // TODO 완성하면 customName, 컴포넌트 xml, deviceName?, deviceAddress 가져오기
            }
        }
    }

    // TODO replaceView: 커스텀이 있으면 리사이클러 뷰 표시, 없으면 텍스트 표시

    override fun onResume() {
        super.onResume()

        // 커스텀 추가
        binding.btnAdd.setOnClickListener {
            val intent = Intent(context, CustomSettingActivity::class.java)
            customSettingActivityLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        customizeRecyclerViewAdapter.clear()
    }
}