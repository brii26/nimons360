package com.tit.nimonsapp.ui.common

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ViewInputFieldBinding

class InputFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewInputFieldBinding.inflate(LayoutInflater.from(context), this, true)

    val text: Editable? get() = binding.etContent.text

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.InputField, 0, 0).apply {
            try {
                val icon = getResourceId(R.styleable.InputField_leftIcon, 0)
                if (icon != 0) binding.ivLeftIcon.setImageResource(icon)

                val placeholder = getString(R.styleable.InputField_placeholderText)
                if (placeholder != null) binding.etContent.hint = placeholder

                val rightText = getString(R.styleable.InputField_rightActionText)
                if (rightText != null) {
                    binding.tvRightText.text = rightText
                    binding.tvRightText.visibility = View.VISIBLE
                }
            } finally {
                recycle()
            }
        }
    }

    fun setInputType(inputType: Int) {
        binding.etContent.inputType = inputType
    }

    fun addTextChangedListener(watcher: android.text.TextWatcher) {
        binding.etContent.addTextChangedListener(watcher)
    }

    fun setOnRightActionClick(listener: OnClickListener) {
        binding.tvRightText.setOnClickListener(listener)
    }
}
