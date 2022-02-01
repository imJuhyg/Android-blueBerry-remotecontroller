package com.limjuhyg.blueberry.views.fragments

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.databinding.FragmentPairedDevicesBinding
import com.limjuhyg.blueberry.adapter.DeviceRecyclerViewAdapter
import com.limjuhyg.blueberry.utils.addDeviceItemWithAnimation
import com.limjuhyg.blueberry.viewmodels.BluetoothScanPair
import com.limjuhyg.blueberry.views.scan.DeviceScanActivity
import com.limjuhyg.blueberry.views.testing.BluetoothChatActivity

class PairedDevicesFragment : Fragment() {
    private var _binding: FragmentPairedDevicesBinding? = null
    private val binding get() = _binding!!
    private val scanPairViewModel by lazy { ViewModelProvider(this).get(BluetoothScanPair::class.java) }
    private var deviceRecyclerViewAdapter: DeviceRecyclerViewAdapter? = null
    private lateinit var bluetoothDevices: ArrayList<BluetoothDevice>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)

        deviceRecyclerViewAdapter = DeviceRecyclerViewAdapter(requireContext())

        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pairedDevicesObserver = Observer<ArrayList<BluetoothDevice>> {
            bluetoothDevices = it
            for(device in bluetoothDevices)
                addDeviceItemWithAnimation(device.name, device.address, deviceRecyclerViewAdapter!!)
            refreshView()
        }
        scanPairViewModel.pairedDevices.observe(viewLifecycleOwner, pairedDevicesObserver)
        scanPairViewModel.getPairedDevices()

    }

    override fun onResume() {
        super.onResume()

        deviceRecyclerViewAdapter!!.clear()
        scanPairViewModel.getPairedDevices()

        // Paired device click event
        // TODO 호출한 액티비티 구분해서 로직 처리
        deviceRecyclerViewAdapter!!.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedItem = deviceRecyclerViewAdapter!!.getItem(position)

                for(device in bluetoothDevices) {
                    if(device.address == selectedItem.address) {
                        val intent = Intent(activity, BluetoothChatActivity::class.java)
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
            startActivity(intent)
        }

        // refresh button click event
        binding.btnRefresh.setOnClickListener {
            deviceRecyclerViewAdapter!!.clear()
            scanPairViewModel.getPairedDevices()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.pairedDeviceRecyclerView.adapter = null
        deviceRecyclerViewAdapter = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothDevices.clear()
    }
}