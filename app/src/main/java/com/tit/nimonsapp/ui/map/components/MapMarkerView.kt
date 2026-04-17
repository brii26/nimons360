package com.tit.nimonsapp.ui.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.tit.nimonsapp.R
import com.tit.nimonsapp.ui.common.AvatarView

/**
 * Custom marker view untuk di MapLibre
 */
class MapMarkerView @JvmOverloads constructor(
    context: Context,
    private val markerType: MarkerType = MarkerType.OTHER_USER,
) : FrameLayout(context) {

    private val avatarView: AvatarView?
    private var letter = ""
    private var avatarColor = 0

    enum class MarkerType {
        CURRENT_USER,
        OTHER_USER,
    }

    init {
        val layoutId = when (markerType) {
            MarkerType.CURRENT_USER -> R.layout.view_map_marker_current_user
            MarkerType.OTHER_USER -> R.layout.view_map_marker_other_user
        }

        LayoutInflater.from(context).inflate(layoutId, this, true)

        avatarView = findViewById(R.id.marker_avatar)

        // Set container size yang cukup besar agar panah tidak kepotong
        val size = when (markerType) {
            MarkerType.CURRENT_USER -> 80f.dpToPx(context).toInt()
            MarkerType.OTHER_USER -> 48f.dpToPx(context).toInt()
        }
        layoutParams = LayoutParams(size, size)
        
        // Pastikan tidak ada clipping di level parent
        clipChildren = false
        clipToPadding = false
        
        isClickable = true
        isFocusable = true
    }

    fun setMarkerData(
        letter: String,
        color: Int = ContextCompat.getColor(context, R.color.colorPrimary),
    ) {
        this.letter = letter.take(1).uppercase()
        this.avatarColor = color
        avatarView?.setLetter(this.letter, this.avatarColor)
    }

    fun setMarkerRotation(rotation: Float) {
        if (markerType == MarkerType.CURRENT_USER) {
            val arrow = findViewById<ImageView>(R.id.iv_arrow)
            arrow?.rotation = rotation
        }
    }

    fun toBitmap(): Bitmap {
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(spec, spec)
        layout(0, 0, measuredWidth, measuredHeight)

        val bitmap = Bitmap.createBitmap(
            if (measuredWidth > 0) measuredWidth else 1,
            if (measuredHeight > 0) measuredHeight else 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    private fun Float.dpToPx(context: Context): Float {
        return this * context.resources.displayMetrics.density
    }
}
