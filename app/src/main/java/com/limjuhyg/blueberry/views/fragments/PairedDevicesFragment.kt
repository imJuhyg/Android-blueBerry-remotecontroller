package com.limjuhyg.blueberry.views.fragments

/**
 * 페어링된 디바이스를 표시하는 프래그먼트
 *
 * 사용 위치
 * 1. MainActivity: '테스트' 버튼을 누르면 생성
 * 2. CustomizeConnectSettingActivity: 커스텀 만들기 - 마지막 연결 설정 단계에서 표시
 *
 * MainActivity 에서 호출했다면 테스트 모드로 사용
 * CustomizeConnectSettings 에서 호출했다면 연결 설정에 사용
 */

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentPairedDevicesBinding
import com.limjuhyg.blueberry.adapter.DeviceRecyclerViewAdapter
import com.limjuhyg.blueberry.applications.MainApplication
import com.limjuhyg.blueberry.utils.addDeviceItem
import com.limjuhyg.blueberry.viewmodels.BluetoothScanPair
import com.limjuhyg.blueberry.views.custom.CustomizeConnectSettingActivity
import com.limjuhyg.blueberry.views.main.MainActivity
import com.limjuhyg.blueberry.views.scan.DeviceScanActivity
import com.limjuhyg.blueberry.views.chat.BluetoothChatActivity

class PairedDevicesFragment : Fragment() {
    private var _binding: FragmentPairedDevicesBinding? = null
    private val binding get() = _binding!!
    private val scanPairViewModel by lazy { ViewModelProvider(this).get(BluetoothScanPair::class.java) }
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private var deviceRecyclerViewAdapter: DeviceRecyclerViewAdapter? = null
    private lateinit var bluetoothDevices: ArrayList<BluetoothDevice>
    private lateinit var requestBluetoothEnable: ActivityResultLauncher<Intent>
    private lateinit var requestMultiplePermissions : ActivityResultLauncher<Array<String>>
    private var hasPermission: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)

        deviceRecyclerViewAdapter = DeviceRecyclerViewAdapter(requireContext())

        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.to_top_from_bottom_2)
        binding.pairedDeviceRecyclerView.animation = animation

        // click listener
        // Paired device click event
        deviceRecyclerViewAdapter!!.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedItem = deviceRecyclerViewAdapter!!.getItem(position) // 사용자가 선택한 디바이스 저장

                for(device in bluetoothDevices) { // 페어링된 디바이스 목록에서
                    if(device.address == selectedItem.address) { // 선택한 디바이스의 주소와 일치하는 디바이스를 찾아
                        /**
                         * 이 프래그먼트가 MainActivity 소유의 프래그먼트라면
                         * 테스트 모드를 실행한다.
                         */
                        if(activity is MainActivity) { // Run bluetooth connection testing mode
                            val intent = Intent(activity, BluetoothChatActivity::class.java)
                            intent.putExtra("BLUETOOTH_DEVICE", device)
                            startActivity(intent)
                        }
                        // Or Complete bluetooth setting
                        /**
                         * 커스텀 만들기 - 연결 설정 액티비티 소유의 프래그먼트라면
                         * 함수 호출을 통해 디바이스 이름과 주소를 주고 종료
                         */
                        else if(activity is CustomizeConnectSettingActivity) {
                            (activity as CustomizeConnectSettingActivity)
                                .setBluetoothDevice(selectedItem.name ?: selectedItem.address, selectedItem.address)
                        }

                        break
                    }
                }
            }
        })

        // scan button click event
        binding.btnSearch.setOnClickListener {
            if(hasPermission) {
                val intent = Intent(activity, DeviceScanActivity::class.java)
                startActivity(intent)
            }
            else {
                showPermissionAlertDialog()
            }
        }

        // refresh button click event
        binding.btnRefresh.setOnClickListener {
            deviceRecyclerViewAdapter!!.clear()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                hasPermission = checkPermission()
            }

            // Request bluetooth enable
            if(hasPermission && !bluetoothAdapter!!.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetoothEnable.launch(intent)
            }

            else if(hasPermission) {
                scanPairViewModel.getPairedDevices()
                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.to_top_from_bottom_2)
                binding.pairedDeviceRecyclerView.animation = animation
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bluetooth permission
        requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if(it.value == true) hasPermission = true
            }
            if(hasPermission) {
                if(!bluetoothAdapter!!.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(intent)
                }
            } else showPermissionAlertDialog()
        }

        // Request bluetooth enable
        requestBluetoothEnable = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_CANCELED) {
                Toast.makeText(requireContext(), "블루투스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) // api 31 이상에서 블루투스 퍼미션 요청
            hasPermission = requestBluetoothPermission()

        // Request bluetooth enable
        /**
         * api 31 미만에서는 이미 퍼미션을 가지고 있음
         */
        if(hasPermission && !bluetoothAdapter!!.isEnabled) { // 퍼미션이 있고 블루투스가 활성화되지 않았다면
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothEnable.launch(intent) // 블루투스 활성화 요청
        }

        /**
         * BluetoothScanPair 뷰모델을 통해 페어링된 디바이스들 저장
         */
        val pairedDevicesObserver = Observer<ArrayList<BluetoothDevice>> {
            bluetoothDevices = it
            for(device in bluetoothDevices) {
                // 리사이클러뷰에 페어링된 디바이스 등록
                addDeviceItem(device.name, device.address, deviceRecyclerViewAdapter!!)
            }
            refreshView()
        }
        scanPairViewModel.pairedDevices.observe(viewLifecycleOwner, pairedDevicesObserver)
    }

    override fun onResume() {
        super.onResume()

        deviceRecyclerViewAdapter!!.clear()
        if(hasPermission) { // 퍼미션이 있으면
            scanPairViewModel.getPairedDevices() // 뷰모델을 통해 페어링된 디바이스 불러오기
        }
    }

    private fun showPermissionAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok, null, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
        title.text = getString(R.string.require_permission)
        subtitle.text = getString(R.string.request_connect_permission)

        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
            setCancelable(false)
        }
        val alertDialog = builder.create()
        alertDialog.show()

        val buttonOk: Button = dialogView.findViewById(R.id.btn_ok)
        buttonOk.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun refreshView() {
        binding.apply {
            if(bluetoothDevices.isEmpty()) {
                pairedDeviceRecyclerView.visibility = View.GONE
                noPairedDevicesTextView.visibility = View.VISIBLE
            } else {
                noPairedDevicesTextView.visibility = View.GONE
                pairedDeviceRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(31)
    private fun checkPermission(): Boolean { // 퍼미션이 있는지만 확인
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        for(permission in permissions) {
            if(ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    @RequiresApi(31)
    private fun requestBluetoothPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        val requirePermissions = ArrayList<String>()

        for(permission in permissions) { // 퍼미션 체크
            if(ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requirePermissions.add(permission)
            }
        }

        return if(requirePermissions.size > 0) { // 퍼미션이 없다면
            requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            ) // 퍼미션 요청
            false
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.pairedDeviceRecyclerView.adapter = null
        deviceRecyclerViewAdapter?.clear()
        deviceRecyclerViewAdapter = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothDevices.clear()
    }
}