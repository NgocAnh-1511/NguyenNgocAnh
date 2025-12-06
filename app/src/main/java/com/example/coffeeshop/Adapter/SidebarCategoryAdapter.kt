package com.example.coffeeshop.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ViewholderSidebarCategoryBinding

class SidebarCategoryAdapter(
    private val items: MutableList<CategoryModel>,
    private val onCategoryClick: (CategoryModel) -> Unit
) : RecyclerView.Adapter<SidebarCategoryAdapter.ViewHolder>() {

    lateinit var context: Context
    private var selectedPosition = 0

    class ViewHolder(val binding: ViewholderSidebarCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderSidebarCategoryBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = items[position]

        holder.binding.categoryNameTxt.text = category.title

        // Set selection state
        if (position == selectedPosition) {
            holder.binding.root.setBackgroundColor(context.getColor(R.color.cream))
            holder.binding.categoryNameTxt.setTextColor(context.getColor(R.color.brown))
            holder.binding.categoryNameTxt.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            holder.binding.root.setBackgroundColor(context.getColor(R.color.white))
            holder.binding.categoryNameTxt.setTextColor(context.getColor(R.color.darkBrown))
            holder.binding.categoryNameTxt.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        holder.binding.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: MutableList<CategoryModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}

