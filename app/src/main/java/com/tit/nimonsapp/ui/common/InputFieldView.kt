package com.tit.nimonsapp.ui.common

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tit.nimonsapp.R
import com.tit.nimonsapp.databinding.ViewInputFieldBinding
import androidx.annotation.DrawableRes
import android.graphics.Typeface

class InputFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewInputFieldBinding.inflate(LayoutInflater.from(context), this, true)

    val text: Editable? get() = binding.etContent.text

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.InputField, 0, 0)
        try {
            // Left Icon
            val icon = typedArray.getResourceId(R.styleable.InputField_leftIcon, 0)
            if (icon != 0) binding.ivLeftIcon.setImageResource(icon)

            // Placeholder
            val placeholder = typedArray.getString(R.styleable.InputField_placeholderText)
            if (placeholder != null) binding.etContent.hint = placeholder

            // Right Action (Text)
            val rightText = typedArray.getString(R.styleable.InputField_rightActionText)
            if (rightText != null) {
                binding.tvRightText.text = rightText
                binding.tvRightText.visibility = View.VISIBLE
            }

            // Right Action (Icon)
            val rightIconRes = typedArray.getResourceId(R.styleable.InputField_rightActionIcon, 0)
            if (rightIconRes != 0) {
                binding.ivRightIcon.setImageResource(rightIconRes)
                binding.ivRightIcon.visibility = View.VISIBLE
            }

            // Input Type
            val inputType = typedArray.getInt(R.styleable.InputField_android_inputType, -1)
            if (inputType != -1) {
               setInputType(inputType)
            } else {
				binding.etContent.typeface = Typeface.DEFAULT
			}
        } finally {
            typedArray.recycle()
        }
    }

    fun setRightActionIcon(@DrawableRes resId: Int) {
        binding.ivRightIcon.setImageResource(resId)
        binding.ivRightIcon.visibility = View.VISIBLE
        binding.tvRightText.visibility = View.GONE
    }

    fun setRightActionText(text: String) {
        binding.tvRightText.text = text
        binding.tvRightText.visibility = View.VISIBLE
        binding.ivRightIcon.visibility = View.GONE
    }

	fun setInputType(inputType: Int) {
		binding.etContent.inputType = inputType
		binding.etContent.typeface = Typeface.DEFAULT
		binding.etContent.setSelection(binding.etContent.text?.length ?: 0)
	}

    fun addTextChangedListener(watcher: android.text.TextWatcher) {
        binding.etContent.addTextChangedListener(watcher)
    }

    fun setOnRightActionClick(listener: OnClickListener) {
        binding.tvRightText.setOnClickListener(listener)
        binding.ivRightIcon.setOnClickListener(listener)
    }

	fun setOnInputFocusChangeListener(listener: OnFocusChangeListener) {
		binding.etContent.setOnFocusChangeListener { view, hasFocus ->
			binding.root.isActivated = hasFocus 
			listener.onFocusChange(view, hasFocus)
		}
	}

	fun setError(errorMessage: String?) {
    if (errorMessage != null) {
        this.isSelected = true 
        binding.tvError.text = errorMessage
        binding.tvError.visibility = View.VISIBLE
    } else {
        this.isSelected = false
        binding.tvError.visibility = View.GONE
    }
}
}