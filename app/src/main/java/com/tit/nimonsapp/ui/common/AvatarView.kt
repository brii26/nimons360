package com.tit.nimonsapp.ui.common

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tit.nimonsapp.R

class AvatarView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : ConstraintLayout(context, attrs, defStyleAttr) {
        private val ivImage: ImageView
        private val tvLetter: TextView

        private var shape: Int = 0

        init {
            LayoutInflater.from(context).inflate(R.layout.view_avatar, this, true)
            ivImage = findViewById(R.id.iv_avatar_image)
            tvLetter = findViewById(R.id.tv_avatar_letter)

            attrs?.let {
                val ta = context.obtainStyledAttributes(it, R.styleable.AvatarView)
                shape = ta.getInt(R.styleable.AvatarView_avatarShape, 0)
                val letter = ta.getString(R.styleable.AvatarView_avatarText) ?: ""
                val bgColor =
                    ta.getColor(
                        R.styleable.AvatarView_avatarBgColor,
                        context.getColor(android.R.color.darker_gray),
                    )
                ta.recycle()
                applyBackground(bgColor)
                tvLetter.text = letter.take(1).uppercase()
            }
        }

        fun setLetter(
            letter: String,
            bgColor: Int,
        ) {
            ivImage.visibility = GONE
            tvLetter.visibility = VISIBLE
            tvLetter.text = letter.take(1).uppercase()
            applyBackground(bgColor)
        }

        fun setImage(loader: (ImageView) -> Unit) {
            tvLetter.visibility = GONE
            ivImage.visibility = VISIBLE
            loader(ivImage)
            applyBackground(android.graphics.Color.TRANSPARENT)
        }

        private fun applyBackground(bgColor: Int) {
            val radius = if (shape == 1) dpToPx(8f) else measuredWidth / 2f
            val drawable =
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(bgColor)
                    cornerRadius = radius
                }
            background = drawable
            if (this.shape == 0) {
                addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                    val size = minOf(right - left, bottom - top) / 2f
                    (background as? GradientDrawable)?.cornerRadius = size
                }
            }
        }

        private fun dpToPx(dp: Float) = dp * resources.displayMetrics.density
    }
