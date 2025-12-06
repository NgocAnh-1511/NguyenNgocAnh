package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.AddressModel
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ViewholderAddressBinding

class AddressAdapter(
    private val items: MutableList<AddressModel>,
    private val onSelectClick: (AddressModel) -> Unit,
    private val onEditClick: (AddressModel) -> Unit,
    private val onDeleteClick: (AddressModel) -> Unit,
    private val onSetDefaultClick: (AddressModel) -> Unit,
    private val isSelectMode: Boolean = false
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(val binding: ViewholderAddressBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderAddressBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = items[position]

        holder.binding.nameTxt.text = address.name
        holder.binding.phoneTxt.text = address.phone
        holder.binding.addressTxt.text = address.address

        // Show/hide default badge
        holder.binding.defaultBadge.visibility = if (address.isDefault) View.VISIBLE else View.GONE

        // Show/hide set default button
        holder.binding.setDefaultBtn.visibility = if (!address.isDefault && !isSelectMode) View.VISIBLE else View.GONE

        // Show/hide select button in select mode
        holder.binding.selectBtn.visibility = if (isSelectMode) View.VISIBLE else View.GONE

        // Hide edit/delete in select mode
        holder.binding.editBtn.visibility = if (isSelectMode) View.GONE else View.VISIBLE
        holder.binding.deleteBtn.visibility = if (isSelectMode) View.GONE else View.VISIBLE

        // Click listeners
        holder.binding.selectBtn.setOnClickListener {
            onSelectClick(address)
        }

        holder.binding.editBtn.setOnClickListener {
            onEditClick(address)
        }

        holder.binding.deleteBtn.setOnClickListener {
            onDeleteClick(address)
        }

        holder.binding.setDefaultBtn.setOnClickListener {
            onSetDefaultClick(address)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<AddressModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

