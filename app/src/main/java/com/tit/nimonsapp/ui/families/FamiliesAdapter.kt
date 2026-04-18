package com.tit.nimonsapp.ui.families

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ItemFamilyBinding
import kotlin.math.abs

class FamiliesAdapter(
    private val onPinClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit,
) : ListAdapter<FamilyItem, FamiliesAdapter.ViewHolder>(DiffCallback) {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).family.id.toLong()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder = ViewHolder(ItemFamilyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: List<Any>,
    ) {
        if (payloads.isEmpty()) {
            holder.bind(getItem(position))
        } else {
            holder.bindPinState(getItem(position).isPinned)
        }
    }

    inner class ViewHolder(
        private val binding: ItemFamilyBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FamilyItem) {
            binding.familyName.text = item.family.name
            val bgColors = listOf(
                "#FFCDD2", "#FFF9C4", "#C8E6C9", "#BBDEFB",
                "#E1BEE7", "#FFE0B2", "#B2EBF2", "#F8BBD0",
            )
            val hex = bgColors[abs(item.family.iconUrl.hashCode()) % bgColors.size]
            binding.iconBgCard.setCardBackgroundColor(Color.parseColor(hex))
            binding.ivFamilyIcon.load(item.family.iconUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_app)
                error(R.drawable.ic_app)
            }
            bindPinState(item.isPinned)
            binding.pinButton.setOnClickListener { onPinClick(item.family.id) }
            binding.root.setOnClickListener { onItemClick(item.family.id) }
        }

        fun bindPinState(isPinned: Boolean) {
            binding.pinButton.setImageResource(
                if (isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin_outline,
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FamilyItem>() {
        private const val PAYLOAD_PIN = "pin"

        override fun areItemsTheSame(
            oldItem: FamilyItem,
            newItem: FamilyItem,
        ): Boolean = oldItem.family.id == newItem.family.id

        override fun areContentsTheSame(
            oldItem: FamilyItem,
            newItem: FamilyItem,
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: FamilyItem,
            newItem: FamilyItem,
        ): Any? = if (oldItem.family == newItem.family && oldItem.isPinned != newItem.isPinned) PAYLOAD_PIN else null
    }
}
