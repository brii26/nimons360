package com.tit.nimonsapp.ui.createfamily

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ViewIconPickerItemBinding

class IconPickerAdapter(
    private val icons: List<String>,
    private val onIconSelected: (String) -> Unit
) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

    private var selectedUrl: String = icons.firstOrNull() ?: ""

    fun setSelected(url: String) {
        selectedUrl = url
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewIconPickerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(icons[position])
    }

    override fun getItemCount() = icons.size

    inner class ViewHolder(private val binding: ViewIconPickerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            binding.ivIconItem.load(url)
            
            // Highlight if selected
            binding.root.isSelected = url == selectedUrl
            
            binding.root.setOnClickListener {
                onIconSelected(url)
                setSelected(url)
            }
        }
    }
}
