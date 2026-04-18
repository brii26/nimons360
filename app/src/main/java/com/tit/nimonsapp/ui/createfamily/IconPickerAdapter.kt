package com.tit.nimonsapp.ui.createfamily

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ViewIconPickerItemBinding
import kotlin.math.abs

class IconPickerAdapter(
    private val icons: List<String>,
    private val onIconSelected: (String) -> Unit,
) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {
    private var selectedUrl: String = icons.firstOrNull() ?: ""

    fun setSelected(url: String) {
        selectedUrl = url
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            ViewIconPickerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(icons[position])
    }

    override fun getItemCount() = icons.size

    inner class ViewHolder(
        private val binding: ViewIconPickerItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            binding.ivIconItem.load(url)
            binding.root.background = buildItemBackground(url)
            binding.root.isSelected = url == selectedUrl
            binding.root.setOnClickListener {
                onIconSelected(url)
                setSelected(url)
            }
        }

        private fun buildItemBackground(url: String): StateListDrawable {
            val ctx = binding.root.context
            val density = ctx.resources.displayMetrics.density
            val radius = 8f * density
            val strokePx = (2f * density).toInt()

            val bgColors = listOf(
                "#FFCDD2", "#FFF9C4", "#C8E6C9", "#BBDEFB",
                "#E1BEE7", "#FFE0B2", "#B2EBF2", "#F8BBD0",
            )
            val iconColor = Color.parseColor(bgColors[abs(url.hashCode()) % bgColors.size])
            val green = ContextCompat.getColor(ctx, R.color.nimons_green)
            val greenContainer = ContextCompat.getColor(ctx, R.color.nimons_green_container)

            val selected = GradientDrawable().apply {
                setColor(greenContainer)
                cornerRadius = radius
                setStroke(strokePx, green)
            }
            val normal = GradientDrawable().apply {
                setColor(iconColor)
                cornerRadius = radius
            }

            return StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_selected), selected)
                addState(intArrayOf(), normal)
            }
        }
    }
}
