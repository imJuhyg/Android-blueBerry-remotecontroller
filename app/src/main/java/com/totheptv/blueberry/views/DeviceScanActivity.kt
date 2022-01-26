package com.totheptv.blueberry.views

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.totheptv.blueberry.MainApplication
import com.totheptv.blueberry.R
import com.totheptv.blueberry.databinding.ActivityDeviceScanBinding
import com.totheptv.blueberry.recyclerviews.DeviceRecyclerViewAdapter
import com.totheptv.blueberry.utils.addDeviceItem
import com.totheptv.blueberry.viewmodels.PairProcessViewModel
import com.totheptv.blueberry.viewmodels.ScanProcessViewModel

class DeviceScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceScanBinding
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val scanProcessViewModel by lazy { ViewModelProvider(this).get(ScanProcessViewModel::class.java) }
    private val pairProcessViewModel by lazy { ViewModelProvider(this).get(PairProcessViewModel::class.java) }
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(this@DeviceScanActivity) }
    private lateinit var bluetoothDevices: ArrayList<BluetoothDevice>
    private var isPairingAvailable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        bluetoothDevices = ArrayList()
        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this@DeviceScanActivity)
        binding.deviceRecyclerView.adapter = deviceRecyclerViewAdapter

        // Observer
        // scanned device observer
        val scanDeviceObserver = Observer<BluetoothDevice>() { device ->
            bluetoothDevices.add(device)
            addDeviceItem(device.name, device.address, this.deviceRecyclerViewAdapter)
        }
        scanProcessViewModel.mutableBluetoothDevice.observe(this, scanDeviceObserver)

        // discovering observer
        val discoveringObserver = Observer<Boolean>() { isDiscovering ->
            // UI update
            if(isDiscovering) { // 검색중일 때
                binding.progressBar.visibility = View.VISIBLE
                binding.btnStopOrSearch.text = getString(R.string.stop)
            } else { // 검색중이 아닐 때 / 검색이 끝났을 때
                binding.progressBar.visibility = View.GONE
                binding.btnStopOrSearch.text = getString(R.string.search)
            }
        }
        scanProcessViewModel.isDiscovering.observe(this, discoveringObserver)

        // PairProcess observer
        val pairDeviceObserver = Observer<Boolean> { result ->
            if(result) { // 페어링 성공 시 종료
                setResult(RESULT_OK)
                finish()
            }
            else {
                binding.apply {
                    scanViewLayout.visibility = View.VISIBLE
                    pairProcessLayout.visibility = View.GONE
                }
            }
        }
        pairProcessViewModel.isBonded.observe(this, pairDeviceObserver)
        scanProcessViewModel.startScan()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            // Scan or Stop button
            btnStopOrSearch.setOnClickListener {
                // 검색중일 때 버튼을 누르면
                if(btnStopOrSearch.text == getString(R.string.stop)) {
                    scanProcessViewModel.stopScan()
                }
                // 검색중이지 않을 때 버튼을 누르면
                else if(btnStopOrSearch.text == getString(R.string.search)) {
                    bluetoothDevices.clear()
                    deviceRecyclerViewAdapter.clear()
                    scanProcessViewModel.startScan()
                }
            }
        }

        // Pair process
        deviceRecyclerViewAdapter.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                isPairingAvailable = true
                val selectedItem = deviceRecyclerViewAdapter.getItem(position)

                for(device in bluetoothDevices) {
                    if(device.address == selectedItem.address) {
                        isPairingAvailable = false
                        Toast.makeText(this@DeviceScanActivity, getString(R.string.already_paired_device), Toast.LENGTH_SHORT).show()
                    }
                }

                if(isPairingAvailable) {
                    for(device in bluetoothDevices) {
                        if(device.address == selectedItem.address) {
                            binding.apply {
                                scanViewLayout.visibility = View.GONE
                                pairProcessLayout.visibility = View.VISIBLE
                            }
                            pairProcessViewModel.requestPair(device)
                            break
                        }
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        bluetoothAdapter!!.cancelDiscovery()
        bluetoothDevices.clear()
        deviceRecyclerViewAdapter.clear()
    }
}