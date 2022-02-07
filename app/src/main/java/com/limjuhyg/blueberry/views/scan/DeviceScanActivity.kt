package com.limjuhyg.blueberry.views.scan

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.ActivityDeviceScanBinding
import com.limjuhyg.blueberry.adapter.DeviceRecyclerViewAdapter
import com.limjuhyg.blueberry.utils.ProgressCircleAnimator
import com.limjuhyg.blueberry.utils.addDeviceItem
import com.limjuhyg.blueberry.viewmodels.BluetoothScanPair

class DeviceScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceScanBinding
    private val scanPairViewModel by lazy { ViewModelProvider(this).get(BluetoothScanPair::class.java) }
    private val deviceRecyclerViewAdapter by lazy { DeviceRecyclerViewAdapter(this@DeviceScanActivity) }
    private lateinit var pairedDevices: ArrayList<BluetoothDevice>
    private val scannedDevices by lazy { ArrayList<BluetoothDevice>() }
    private var isPairingAvailable = true
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var hasPermission: Boolean = false
    private var searchProgressAnimator: ProgressCircleAnimator? = null
    private var pairingProgressAnimator: ProgressCircleAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.to_top_from_bottom_1, R.anim.none)

        // Progress animation
        searchProgressAnimator = ProgressCircleAnimator(
            binding.progressCircle3,
            binding.progressCircle2,
            binding.progressCircle1, 750)
        searchProgressAnimator!!.startAnimation()

        pairingProgressAnimator = ProgressCircleAnimator(
            binding.pairingProgressCircle1,
            binding.pairingProgressCircle2,
            binding.pairingProgressCircle3, 500
        )

        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(this@DeviceScanActivity)
        binding.deviceRecyclerView.adapter = deviceRecyclerViewAdapter

        // Check permission
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                hasPermission = true
                scanPairViewModel.startScan()
            }
            else showPermissionAlertDialog()
        }
        hasPermission = requestLocationPermission()

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
                binding.progressCircleGroup.visibility = View.VISIBLE
                binding.btnStopOrFind.text = getString(R.string.stop)
            } else { // 검색중이 아닐 때 / 검색이 끝났을 때
                binding.progressCircleGroup.visibility = View.INVISIBLE
                binding.btnStopOrFind.text = getString(R.string.find)
            }
        }
        scanPairViewModel.isDiscovering.observe(this, discoveringObserver)

        // PairProcess observer
        val pairDeviceObserver = Observer<Boolean> { result ->
            if(result) finish()// 페어링 성공 시 종료
            else {
                binding.apply {
                    scanViewLayout.visibility = View.VISIBLE
                    pairProcessLayout.visibility = View.INVISIBLE
                }
            }
        }
        scanPairViewModel.isBonded.observe(this, pairDeviceObserver)
        if(hasPermission) scanPairViewModel.startScan()
    }

    override fun onResume() {
        super.onResume()

        binding.apply {
            // Scan or Stop button
            btnStopOrFind.setOnClickListener { // 검색중일 때 버튼을 누르면
                if(btnStopOrFind.text == getString(R.string.stop)) {
                    scanPairViewModel.stopScan()
                }
                else if(btnStopOrFind.text == getString(R.string.find)) { // 검색중이지 않을 때 버튼을 누르면
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
                        pairingProgressAnimator!!.startAnimation()
                    }
                    scanPairViewModel.requestPair(scannedDevices[position])
                }
            }
        })
    }

    private fun requestLocationPermission(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // more than api level 28
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                false
            } else true
        } else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                false
            } else true
        }
    }

    private fun showPermissionAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok, null, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
        title.text = getString(R.string.require_permission)
        subtitle.text = getString(R.string.request_location_permission)

        val builder = AlertDialog.Builder(this).apply {
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

    override fun onDestroy() {
        super.onDestroy()
        scannedDevices.clear()
        deviceRecyclerViewAdapter.clear()
        searchProgressAnimator!!.cancelAnimation()
        pairingProgressAnimator!!.cancelAnimation()
        searchProgressAnimator = null
        pairingProgressAnimator = null
    }
}