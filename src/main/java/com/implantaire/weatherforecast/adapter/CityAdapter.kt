package com.implantaire.weatherforecast.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.implantaire.weatherforecast.databinding.CityViewholderBinding
import com.implantaire.weatherforecast.models.CityResponseApi

class CityAdapter:RecyclerView.Adapter<CityAdapter.ViewHolder>() {
    private lateinit var binding: CityViewholderBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityAdapter.ViewHolder {
        val inflater= LayoutInflater.from(parent.context)
        binding= CityViewholderBinding.inflate(inflater,parent,false)
        return ViewHolder()
    }
    override fun onBindViewHolder(holder: CityAdapter.ViewHolder, position: Int) {
        binding = CityViewholderBinding.bind(holder.itemView)
        binding.cityText.text = differ.currentList[position].name
        binding.root.setOnClickListener {
            val intent = Intent()
            intent.putExtra("lat", differ.currentList[position].lat)
            intent.putExtra("lon", differ.currentList[position].lon)
            intent.putExtra("name", differ.currentList[position].name)
            (binding.root.context as Activity).setResult(Activity.RESULT_OK, intent)
            (binding.root.context as Activity).finish()
        }
    }
    inner class ViewHolder:RecyclerView.ViewHolder(binding.root)
    override fun getItemCount()=differ.currentList.size
    private val differCallback=object :DiffUtil.ItemCallback<CityResponseApi.CityResponseApiItem>(){
        override fun areItemsTheSame(
            oldItem: CityResponseApi.CityResponseApiItem,
            newItem: CityResponseApi.CityResponseApiItem
        ): Boolean {
            return  oldItem==newItem
        }
        override fun areContentsTheSame(
            oldItem: CityResponseApi.CityResponseApiItem,
            newItem: CityResponseApi.CityResponseApiItem
        ): Boolean {
            return oldItem==newItem
        }
    }
    val differ=AsyncListDiffer(this,differCallback)
}