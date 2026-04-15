package com.tit.nimonsapp.ui.common

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.tit.nimonsapp.R

class ButtonPrimaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ButtonPrimary, 0, 0).apply {
            try {
                if (hasValue(R.styleable.ButtonPrimary_buttonColor)) {
                    val color = getColor(R.styleable.ButtonPrimary_buttonColor, 0)
                    setBackgroundColor(color)
                }
                if (hasValue(R.styleable.ButtonPrimary_buttonTextColor)) {
                    val textColor = getColor(R.styleable.ButtonPrimary_buttonTextColor, 0)
                    setTextColor(textColor)
                }
            } finally {
                recycle()
            }
        }
    }
}
