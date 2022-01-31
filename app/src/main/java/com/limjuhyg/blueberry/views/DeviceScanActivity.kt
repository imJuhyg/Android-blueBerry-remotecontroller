package com.limjuhyg.blueberry.views

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityDeviceScanBinding
import com.limjuhyg.blueberry.adapter.DeviceRecyclerViewAdapter
import com.limjuhyg.blueberry.utils.addDeviceItem
import com.limjuhyg.blueberry.viewmodels.BluetoothScanPair

class DeviceScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceScanBinding
    private val scanPairViewModel by lazy { ViewModelProvider(this).get(BluetoothScanPair::class.java) }
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(this@DeviceScanActivity) }
    private lateinit var pairedDevices: ArrayList<BluetoothDevice>
    private val scannedDevices by lazy { ArrayList<BluetoothDevice>() }
    private var isPairingAvailable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this@DeviceScanActivity)
        binding.deviceRecyclerView.adapter = deviceRecyclerViewAdapter

        // Observer
        // paired devices observer
        val pairedDevicesObserver = Observer<ArrayList<BluetoothDevice>> {
            pairedDevices = it
        }
        scanPairViewModel.pairedDevices.observe(this, pairedDevicesObserver)
        scanPairViewModel.getPairedDevices()

        // scanned device observer
        val scanDeviceObserver = Observer<BluetoothDevice> { device ->
            scannedDevices.add(device)
            addDeviceItem(device.name, device.address, this.deviceRecyclerViewAdapter)
        }
        scanPairViewModel.foundDevice.observe(this, scanDeviceObserver)

        // discovering observer
        val discoveringObserver = Observer<Boolean> { isDiscovering ->
            // UI update
            if(isDiscovering) { // 검색중일 때
                binding.progressBar.visibility = View.VISIBLE
                binding.btnStopOrSearch.text = getString(R.string.stop)
            } else { // 검색중이 아닐 때 / 검색이 끝났을 때
                binding.progressBar.visibility = View.GONE
                binding.btnStopOrSearch.text = getString(R.string.search)
            }
        }
        scanPairViewModel.isDiscovering.observe(this, discoveringObserver)

        // PairProcess observer
        val pairDeviceObserver = Observer<Boolean> { result ->
            if(result) finish()// 페어링 성공 시 종료
            else {
                binding.apply {
                    scanViewLayout.visibility = View.VISIBLE
                    pairProcessLayout.visibility = View.GONE
                }
            }
        }
        scanPairViewModel.isBonded.observe(this, pairDeviceObserver)
        scanPairViewModel.startScan()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            // Scan or Stop button
            btnStopOrSearch.setOnClickListener {
                // 검색중일 때 버튼을 누르면
                if(btnStopOrSearch.text == getString(R.string.stop)) {
                    scanPairViewModel.stopScan()
                }
                // 검색중이지 않을 때 버튼을 누르면
                else if(btnStopOrSearch.text == getString(R.string.search)) {
                    scannedDevices.clear()
                    deviceRecyclerViewAdapter.clear()
                    scanPairViewModel.startScan()
                }
            }
        }

        // Pair process
        deviceRecyclerViewAdapter.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                isPairingAvailable = true
                val selectedItem = deviceRecyclerViewAdapter.getItem(position)

                for(pairedDevice in pairedDevices) {
                    if(pairedDevice.address == selectedItem.address) {
                        isPairingAvailable = false
                        Toast.makeText(this@DeviceScanActivity, getString(R.string.already_paired_device), Toast.LENGTH_SHORT).show()
                    }
                }

                if(isPairingAvailable) {
                    binding.apply {
                        scanViewLayout.visibility = View.GONE
                        pairProcessLayout.visibility = View.VISIBLE
                    }
                    scanPairViewModel.requestPair(scannedDevices[position])
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        scannedDevices.clear()
        deviceRecyclerViewAdapter.clear()
    }
}