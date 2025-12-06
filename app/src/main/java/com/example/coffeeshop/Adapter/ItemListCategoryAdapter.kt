package com.example.coffeeshop.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Activity.DetailActivity
import com.example.coffeeshop.Domain.ItemsModel
import com.example.coffeeshop.Utils.formatVND
import com.example.coffeeshop.databinding.ViewholderItemListBinding
import com.google.gson.Gson

class ItemListCategoryAdapter(
    val items: MutableList<ItemsModel>,
    val onAddToCartClick: (ItemsModel) -> Unit
):
RecyclerView.Adapter<ItemListCategoryAdapter.ViewHolder>() {
    lateinit var context: Context



    class ViewHolder ( val binding: ViewholderItemListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemListCategoryAdapter.ViewHolder {
        context = parent.context
        val binding= ViewholderItemListBinding.
        inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemListCategoryAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.priceTxt.text = formatVND(item.price)
        holder.binding.subtitleTxt.text = item.extra.toString()

        Glide.with(context)
            .load(item.picUrl[0])
            .into(holder.binding.pic)

        // Click on item to open detail
        holder.binding.root.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            val itemJson = Gson().toJson(item)
            intent.putExtra("item", itemJson)
            ContextCompat.startActivity(context, intent, null)
        }

        // Click on add to cart button
        holder.binding.imageView6.setOnClickListener {
            onAddToCartClick(item)
        }

    }

    override fun getItemCount(): Int  = items.size
}