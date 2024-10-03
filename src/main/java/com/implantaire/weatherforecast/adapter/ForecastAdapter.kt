package com.implantaire.weatherforecast.adapter

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.implantaire.weatherforecast.databinding.ForecastActivityBinding
import com.implantaire.weatherforecast.models.ForeCastResponseApi
import java.text.SimpleDateFormat

class ForecastAdapter:RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {
    private lateinit var binding:ForecastActivityBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastAdapter.ViewHolder {
        val inflater= LayoutInflater.from(parent.context)
        binding= ForecastActivityBinding.inflate(inflater,parent,false)
        return ViewHolder()
    }
    override fun onBindViewHolder(holder: ForecastAdapter.ViewHolder, position: Int) {
       val binding = ForecastActivityBinding.bind(holder.itemView)
        val date=SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(differ.currentList[position].dtTxt.toString())
        val calendar=Calendar.getInstance()
        calendar.time=date
        val dayOfWeekName=when(calendar.get(Calendar.DAY_OF_WEEK)){
            1->"Sun"
            2->"Mon"
            3->"Tues"
            4->"Wed"
            5->"Thur"
            6->"Fri"
            7->"Sat"
            else->"Sun"
        }
        binding.day.text=dayOfWeekName
        val hour=calendar.get(Calendar.HOUR_OF_DAY)
        val amPm=if(hour<12) "AM" else "PM"
        val hour12=calendar.get(Calendar.HOUR)
        binding.hour.text= buildString {
            append(hour12.toString())
            append(amPm)
        }
        binding.temp.text= buildString {
            append(differ.currentList[position].main?.temp?.let { Math.round(it) }.toString())
            append("°")
        }
        val icon=when(differ.currentList[position].weather?.get(0)?.icon.toString()){
            "01d","01n"->"sunny"
            "02d","02n"->"cloudy_sunny"
            "03d","03n"->"cloudy_sunny"
            "04d","04n"->"cloudy"
            "09d","09n"->"rainy"
            "10d","10n"->"rainy"
            "11d","11n"->"storm"
            "13d","13n"->"snow"
            "50d","50n"->"windy"
            else->"sunny"
        }
        val drawableResId:Int=binding.root.resources.getIdentifier(
            icon,
            "drawable",binding.root.context.packageName
        )
        Glide.with(binding.root.context)
            .load(drawableResId)
            .into(binding.pic)
    }
    inner class ViewHolder:RecyclerView.ViewHolder(binding.root)
    override fun getItemCount()=differ.currentList.size
    private val differCallback=object :DiffUtil.ItemCallback<ForeCastResponseApi.data>(){
        override fun areItemsTheSame(
            oldItem: ForeCastResponseApi.data,
            newItem: ForeCastResponseApi.data
        ): Boolean {
            return  oldItem==newItem
        }
        override fun areContentsTheSame(
            oldItem: ForeCastResponseApi.data,
            newItem: ForeCastResponseApi.data
        ): Boolean {
           return oldItem==newItem
        }
    }
    val differ=AsyncListDiffer(this,differCallback)
}