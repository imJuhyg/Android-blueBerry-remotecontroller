package com.limjuhyg.blueberry.views.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
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
    private lateinit var requestMultiplePermissions : ActivityResultLauncher<Array<String>>
    private var hasPermission: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPairedDevicesBinding.inflate(inflater, container, false)

        deviceRecyclerViewAdapter = DeviceRecyclerViewAdapter(requireContext())

        binding.pairedDeviceRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.pairedDeviceRecyclerView.adapter = deviceRecyclerViewAdapter
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.to_top_from_bottom_2)
        binding.pairedDeviceRecyclerView.animation = animation

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            hasPermission = requestBluetoothPermission()

        // Request bluetooth enable
        if(hasPermission && !bluetoothAdapter!!.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(intent)
        }

        val pairedDevicesObserver = Observer<ArrayList<BluetoothDevice>> {
            bluetoothDevices = it
            for(device in bluetoothDevices)
                addDeviceItem(device.name, device.address, deviceRecyclerViewAdapter!!)
            refreshView()
        }
        scanPairViewModel.pairedDevices.observe(viewLifecycleOwner, pairedDevicesObserver)
    }

    override fun onResume() {
        super.onResume()

        deviceRecyclerViewAdapter!!.clear()
        if(hasPermission) scanPairViewModel.getPairedDevices()

        // Paired device click event
        deviceRecyclerViewAdapter!!.setOnItemClickListener(object: DeviceRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedItem = deviceRecyclerViewAdapter!!.getItem(position)

                for(device in bluetoothDevices) {
                    if(device.address == selectedItem.address) {
                        if(activity is MainActivity) { // Run bluetooth connection testing mode
                            val intent = Intent(activity, BluetoothChatActivity::class.java)
                            intent.putExtra("BLUETOOTH_DEVICE", device)
                            startActivity(intent)
                        }
                        // Or Complete bluetooth setting
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
            if(hasPermission) {
                scanPairViewModel.getPairedDevices()
                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.to_top_from_bottom_2)
                binding.pairedDeviceRecyclerView.animation = animation
            }

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

    // already permission has -> return true
    // else -> return false
    @RequiresApi(31)
    private fun requestBluetoothPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        val requirePermissions = ArrayList<String>()

        for(permission in permissions) {
            if(ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requirePermissions.add(permission)
            }
        }

        return if(requirePermissions.size > 0) {
            requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            )
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