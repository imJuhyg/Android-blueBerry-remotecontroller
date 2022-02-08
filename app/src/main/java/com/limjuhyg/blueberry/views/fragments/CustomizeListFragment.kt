package com.limjuhyg.blueberry.views.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.limjuhyg.blueberry.R
import com.limjuhyg.blueberry.databinding.FragmentCustomizeListBinding
import com.limjuhyg.blueberry.adapter.CustomizeRecyclerViewAdapter
import com.limjuhyg.blueberry.models.room.entities.Customize
import com.limjuhyg.blueberry.viewmodels.CustomizeViewModel
import com.limjuhyg.blueberry.views.custom.CustomizeCommunicationActivity
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_CREATE_MODE
import com.limjuhyg.blueberry.views.custom.CustomizeNameSettingActivity.Companion.CUSTOMIZE_MODIFICATION_MODE

class CustomizeListFragment : Fragment() {
    private var _binding: FragmentCustomizeListBinding? = null
    private val binding get() = _binding!!
    private val customizeViewModel by lazy { ViewModelProvider(this).get(CustomizeViewModel::class.java) }
    private var customizeRecyclerViewAdapter: CustomizeRecyclerViewAdapter? = null
    private lateinit var customizeList: ArrayList<Customize>
    private var isButtonClickable = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCustomizeListBinding.inflate(inflater, container, false)

        customizeRecyclerViewAdapter = CustomizeRecyclerViewAdapter(requireContext())
        binding.customizeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.customizeRecyclerView.adapter = customizeRecyclerViewAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user customize
        val customizeObserver = Observer<List<Customize>> {
            customizeList = ArrayList(it)
            customizeRecyclerViewAdapter!!.clear()
            for(customize in it) {
                customizeRecyclerViewAdapter!!.addItem(
                    customize.customizeName,
                    customize.deviceAddress?.let{ ContextCompat.getDrawable(requireContext(), R.drawable.icon_remote_device_48) }
                        ?: run{ ContextCompat.getDrawable(requireContext(), R.drawable.icon_device_unknown_48)},
                    customize.deviceName,
                    customize.deviceAddress
                )
            }
            customizeRecyclerViewAdapter!!.notifyDataSetChanged()
            refreshView()
        }
        customizeViewModel.customizeList.observe(viewLifecycleOwner, customizeObserver)
    }

    override fun onResume() {
        super.onResume()

        binding.customizeRecyclerView.scrollToPosition(0)
        customizeViewModel.getAllCustomize()

        // Create custom
        binding.btnAdd.setOnClickListener {
            val intent = Intent(context, CustomizeNameSettingActivity::class.java)
            intent.putExtra("MODE", CUSTOMIZE_CREATE_MODE)
            startActivity(intent)
        }

        // View click listener
        customizeRecyclerViewAdapter!!.setOnViewClickListener(object: CustomizeRecyclerViewAdapter.OnViewClickListener {
            override fun onViewClick(view: View, position: Int) {
                val selectedItem = customizeRecyclerViewAdapter!!.getItem(position)
                val intent = Intent(requireContext(), CustomizeCommunicationActivity::class.java)
                intent.putExtra("CUSTOMIZE_NAME", selectedItem.customizeName)
                intent.putExtra("DEVICE_ADDRESS", selectedItem.deviceAddress)
                startActivity(intent)
            }
        })

        // Button(setting, delete) click listener
        customizeRecyclerViewAdapter!!.setOnButtonClickListener(object: CustomizeRecyclerViewAdapter.OnButtonClickListener {
            override fun onButtonClick(view: View, position: Int) {
                if(isButtonClickable) {
                    val item = customizeRecyclerViewAdapter!!.getItem(position)

                    isButtonClickable = false
                    view.postDelayed({isButtonClickable = true}, 500)

                    if(view.id == R.id.btn_customize_setting_change) {
                        val intent = Intent(requireContext(), CustomizeNameSettingActivity::class.java)
                        intent.putExtra("MODE", CUSTOMIZE_MODIFICATION_MODE)
                        intent.putExtra("CUSTOMIZE_NAME", item.customizeName)
                        startActivity(intent)
                    }

                    if(view.id == R.id.btn_customize_delete) {
                        val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog_ok_cancel, null, false)
                        val title: TextView = dialogView.findViewById(R.id.title)
                        val subtitle: TextView = dialogView.findViewById(R.id.subtitle)
                        title.text = getString(R.string.customize_delete_message)
                        subtitle.text = getString(R.string.customize_delete_message_2)

                        val builder = AlertDialog.Builder(requireContext()).apply {
                            setView(dialogView)
                            setCancelable(false)
                        }
                        val alertDialog = builder.create()
                        alertDialog.show()

                        val buttonOk: Button = dialogView.findViewById(R.id.btn_ok)
                        buttonOk.setOnClickListener { // Delete customize
                            customizeViewModel.deleteCustomize(item.customizeName) // Delete customize from DB
                            customizeRecyclerViewAdapter!!.removeItem(position) // Delete item from Recycler view
                            customizeList.removeAt(position) // Delete from ArrayList
                            alertDialog.dismiss()
                            refreshView()
                        }

                        val buttonCancel: Button = dialogView.findViewById(R.id.btn_cancel)
                        buttonCancel.setOnClickListener { // Cancel
                            alertDialog.dismiss()
                        }
                    }
                }
            }
        })
    }

    private fun refreshView() {
        if(customizeList.isNotEmpty()) {
            binding.noCustomizeTextView.visibility = View.GONE
            binding.customizeRecyclerView.visibility = View.VISIBLE
        }
        else {
            binding.customizeRecyclerView.visibility = View.GONE
            binding.noCustomizeTextView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.customizeRecyclerView.adapter = null
        customizeRecyclerViewAdapter = null
        _binding = null
    }
}