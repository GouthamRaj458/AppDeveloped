package com.implantaire.weatherforecast.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.implantaire.weatherforecast.databinding.CityViewholderBinding

class RecentSearchAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {
    private val recentSearches = mutableListOf<String>()
    fun submitList(recentSearches: List<String>) {
        this.recentSearches.clear()
        this.recentSearches.addAll(recentSearches)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CityViewholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.cityText.text = recentSearches[position]
        holder.binding.cityText.setOnClickListener {
            onItemClickListener.onItemClick(recentSearches[position])
        }
    }
    override fun getItemCount(): Int {
        return recentSearches.size
    }
    class ViewHolder(val binding: CityViewholderBinding) : RecyclerView.ViewHolder(binding.root)
    interface OnItemClickListener {
        fun onItemClick(searchValue: String)
    }
}
