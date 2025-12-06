package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.BannerModel
import com.example.coffeeshop.databinding.ViewholderBannerBinding

class BannerAdapter(
    private val banners: MutableList<BannerModel>
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = banners[position]
        Glide.with(holder.itemView.context)
            .load(banner.url)
            .into(holder.binding.bannerImage)
    }

    override fun getItemCount(): Int = banners.size

    fun updateList(newList: MutableList<BannerModel>) {
        banners.clear()
        banners.addAll(newList)
        notifyDataSetChanged()
    }
}

