package com.totheptv.blueberry.views

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.totheptv.blueberry.MainApplication
import com.totheptv.blueberry.databinding.FragmentPairedDevicesBinding
import com.totheptv.blueberry.recyclerviews.DeviceRecyclerViewAdapter
import com.totheptv.blueberry.utils.addDeviceItemWithAnimation

class PairedDevicesFragment : Fragment() {
    private lateinit var binding: FragmentPairedDevicesBinding
    private val bluetoothAdapter by lazy { MainApplication.instance.bluetoothAdapter }
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(requireContext()) }
    private lateinit var bluetoothDevices: ArrayList<BluetoothDevice>
    private lateinit var scanActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter

        // scan activity result launcher
        scanActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                deviceRecyclerViewAdapter.clear()
                getPairedDevices()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPairedDevices()

        // Paired device click event
        deviceRecyclerViewAdapter.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedItem = deviceRecyclerViewAdapter.getItem(position)

                for(device in bluetoothDevices) {
                    if(device.address == selectedItem.address) {
                        val intent = Intent(activity, ClientCommunicationActivity::class.java)
                        intent.putExtra("BLUETOOTH_DEVICE", device)
                        startActivity(intent)

                        break
                    }
                }
            }
        })

        // scan button click event
        binding.btnSearch.setOnClickListener {
            val intent = Intent(activity, DeviceScanActivity::class.java)
            scanActivityResultLauncher.launch(intent)
        }

        // refresh button click event
        binding.btnRefresh.setOnClickListener {
            deviceRecyclerViewAdapter.clear()
            getPairedDevices()
        }
    }

    private fun replaceView() {
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

    private fun getPairedDevices() {
        bluetoothDevices = ArrayList(bluetoothAdapter!!.bondedDevices)
        for(device in bluetoothDevices)
            addDeviceItemWithAnimation(device.name, device.address, deviceRecyclerViewAdapter)
        replaceView()
    }

    override fun onDestroy() {
        super.onDestroy()

        deviceRecyclerViewAdapter.clear()
        bluetoothDevices.clear()
    }
}