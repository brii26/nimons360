package com.tit.nimonsapp.ui.common

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class ButtonPrimaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle,
) : MaterialButton(context, attrs, defStyleAttr)
