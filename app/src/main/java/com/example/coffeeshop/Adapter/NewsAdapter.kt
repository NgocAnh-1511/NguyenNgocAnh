package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.NewsModel
import com.example.coffeeshop.databinding.ViewholderNewsBinding

class NewsAdapter(
    private val newsList: MutableList<NewsModel>
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = newsList[position]
        holder.binding.newsTitle.text = news.title
        holder.binding.newsDescription.text = news.description
        
        Glide.with(holder.itemView.context)
            .load(news.imageUrl)
            .into(holder.binding.newsImage)
    }

    override fun getItemCount(): Int = newsList.size

    fun updateList(newList: MutableList<NewsModel>) {
        newsList.clear()
        newsList.addAll(newList)
        notifyDataSetChanged()
    }
}

