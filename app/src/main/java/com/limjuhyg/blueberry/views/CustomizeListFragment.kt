package com.limjuhyg.blueberry.views

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.databinding.FragmentCustomizeListBinding
import com.limjuhyg.blueberry.adapter.CustomizeRecyclerViewAdapter

class CustomizeListFragment : Fragment() {
    private lateinit var binding: FragmentCustomizeListBinding
    private val customizeRecyclerViewAdapter by lazy { CustomizeRecyclerViewAdapter(requireContext()) }
    private lateinit var customSettingActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCustomizeListBinding.inflate(inflater, container, false)

        binding.customizeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.customizeRecyclerView.adapter = customizeRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            val intent = Intent(context, CustomizeSettingActivity::class.java)
            customSettingActivityLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        customizeRecyclerViewAdapter.clear()
    }
}