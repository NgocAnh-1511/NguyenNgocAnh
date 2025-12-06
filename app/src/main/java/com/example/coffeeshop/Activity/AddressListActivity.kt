package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.AddressAdapter
import com.example.coffeeshop.Domain.AddressModel
import com.example.coffeeshop.Manager.AddressManager
import com.example.coffeeshop.R
import com.example.coffeeshop.Utils.ValidationUtils
import com.example.coffeeshop.Utils.dpToPx
import com.example.coffeeshop.databinding.ActivityAddressListBinding

class AddressListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressListBinding
    private lateinit var addressManager: AddressManager
    private lateinit var addressAdapter: AddressAdapter
    private var isSelectMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for header
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = systemBars.top + 8.dpToPx() // Đưa header xuống một chút
            v.layoutParams = layoutParams
            insets
        }

        addressManager = AddressManager(this)
        isSelectMode = intent.getBooleanExtra("selectMode", false)

        setupRecyclerView()
        setupClickListeners()
        loadAddresses()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAddresses.layoutManager = LinearLayoutManager(this)
        addressAdapter = AddressAdapter(
            mutableListOf(),
            onSelectClick = { address ->
                if (isSelectMode) {
                    // Return selected address
                    val intent = Intent()
                    intent.putExtra("selectedAddress", com.google.gson.Gson().toJson(address))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            },
            onEditClick = { address ->
                openEditAddressDialog(address)
            },
            onDeleteClick = { address ->
                showDeleteDialog(address)
            },
            onSetDefaultClick = { address ->
                if (addressManager.setDefaultAddress(address.id)) {
                    Toast.makeText(this, "Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show()
                    loadAddresses()
                }
            },
            isSelectMode = isSelectMode
        )
        binding.recyclerViewAddresses.adapter = addressAdapter
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.addAddressBtn.setOnClickListener {
            openEditAddressDialog(null)
        }
    }

    private fun loadAddresses() {
        val addresses = addressManager.getAllAddresses()
        
        if (addresses.isEmpty()) {
            binding.recyclerViewAddresses.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewAddresses.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            addressAdapter.updateList(addresses)
        }
    }

    private fun openEditAddressDialog(address: AddressModel?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_address, null)
        val nameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.nameEditText)
        val phoneEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.phoneEditText)
        val addressEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.addressEditText)
        val defaultCheckbox = dialogView.findViewById<android.widget.CheckBox>(R.id.defaultCheckbox)

        if (address != null) {
            nameEditText.setText(address.name)
            phoneEditText.setText(address.phone)
            addressEditText.setText(address.address)
            defaultCheckbox.isChecked = address.isDefault
        }

        AlertDialog.Builder(this)
            .setTitle(if (address == null) "Thêm địa chỉ mới" else "Chỉnh sửa địa chỉ")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()
                val addr = addressEditText.text.toString().trim()
                val isDefault = defaultCheckbox.isChecked

                // Validation tên
                val nameValidation = ValidationUtils.validateName(name)
                if (!nameValidation.first) {
                    Toast.makeText(this, nameValidation.second, Toast.LENGTH_SHORT).show()
                    nameEditText.error = nameValidation.second
                    return@setPositiveButton
                }

                // Validation số điện thoại
                val phoneValidation = ValidationUtils.validatePhone(phone)
                if (!phoneValidation.first) {
                    Toast.makeText(this, phoneValidation.second, Toast.LENGTH_SHORT).show()
                    phoneEditText.error = phoneValidation.second
                    return@setPositiveButton
                }

                // Validation địa chỉ
                if (addr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
                    addressEditText.error = "Vui lòng nhập địa chỉ"
                    return@setPositiveButton
                }

                val addressToSave = address?.copy(
                    name = name,
                    phone = phone,
                    address = addr,
                    isDefault = isDefault
                ) ?: AddressModel(
                    name = name,
                    phone = phone,
                    address = addr,
                    isDefault = isDefault
                )

                if (addressManager.saveAddress(addressToSave)) {
                    Toast.makeText(this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show()
                    loadAddresses()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteDialog(address: AddressModel) {
        AlertDialog.Builder(this)
            .setTitle("Xóa địa chỉ")
            .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
            .setPositiveButton("Xóa") { _, _ ->
                if (addressManager.deleteAddress(address.id)) {
                    Toast.makeText(this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show()
                    loadAddresses()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadAddresses()
    }
}

