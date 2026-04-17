package com.tit.nimonsapp.ui.families

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.databinding.ItemFamilyBinding

class FamiliesAdapter(
    private val onPinClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit,
) : ListAdapter<GetFamiliesResponseDto, FamiliesAdapter.ViewHolder>(DiffCallback) {

    private var pinnedIds: List<Int> = emptyList()

    fun setPinnedIds(ids: List<Int>) {
        pinnedIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFamilyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val family = getItem(position)
        holder.bind(family, pinnedIds.contains(family.id))
    }

    inner class ViewHolder(private val binding: ItemFamilyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(family: GetFamiliesResponseDto, isPinned: Boolean) {
            binding.familyName.text = family.name
            
            // Gunakan Coil langsung ke ImageView (Hapus ComposeView di XML nanti)
            binding.ivFamilyIcon.load(family.iconUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_app)
                error(R.drawable.ic_app)
                transformations(RoundedCornersTransformation(32f))
            }
            
            binding.pinButton.setImageResource(
                if (isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin_outline
            )
            
            binding.pinButton.setOnClickListener { onPinClick(family.id) }
            binding.root.setOnClickListener { onItemClick(family.id) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GetFamiliesResponseDto>() {
        override fun areItemsTheSame(oldItem: GetFamiliesResponseDto, newItem: GetFamiliesResponseDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GetFamiliesResponseDto, newItem: GetFamiliesResponseDto): Boolean {
            return oldItem == newItem
        }
    }
}
