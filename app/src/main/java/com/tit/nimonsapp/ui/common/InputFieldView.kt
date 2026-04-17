package com.tit.nimonsapp.ui.common

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.InputField, 0, 0)
        try {
            val icon = typedArray.getResourceId(R.styleable.InputField_leftIcon, 0)
            if (icon != 0) {
                binding.ivLeftIcon.setImageResource(icon)
            } else {
                binding.ivLeftIcon.visibility = View.GONE
            }

            val placeholder = typedArray.getString(R.styleable.InputField_placeholderText)
            if (placeholder != null) binding.etContent.hint = placeholder

            val rightText = typedArray.getString(R.styleable.InputField_rightActionText)
            if (rightText != null) {
                binding.tvRightText.text = rightText
                binding.tvRightText.visibility = View.VISIBLE
            }

            val rightIconRes = typedArray.getResourceId(R.styleable.InputField_rightActionIcon, 0)
            if (rightIconRes != 0) {
                binding.ivRightIcon.setImageResource(rightIconRes)
                binding.ivRightIcon.visibility = View.VISIBLE
            }

            val inputType = typedArray.getInt(R.styleable.InputField_android_inputType, -1)
            if (inputType != -1) {
                setInputType(inputType)
            } else {
                binding.etContent.typeface = Typeface.DEFAULT
            }

            val label = typedArray.getString(R.styleable.InputField_labelText)
            if (!label.isNullOrEmpty()) {
                binding.tvLabel.text = label
                binding.tvLabel.visibility = View.VISIBLE
                val lp = binding.containerInput.layoutParams as? android.view.ViewGroup.MarginLayoutParams
                lp?.topMargin = (8 * resources.displayMetrics.density).toInt()
                binding.containerInput.layoutParams = lp
            }

            val clearable = typedArray.getBoolean(R.styleable.InputField_clearable, false)
            if (clearable) setupClearButton()
        } finally {
            typedArray.recycle()
        }

        setupLabelFocusColor()
    }

    private fun setupClearButton() {
        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ivClear.visibility = if (s.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            }
        })
        binding.ivClear.setOnClickListener {
            binding.etContent.text?.clear()
        }
    }

    private fun setupLabelFocusColor() {
        if (binding.tvLabel.visibility != View.VISIBLE) return
        val colorDefault = ContextCompat.getColor(context, R.color.section_label)
        val colorFocused = ContextCompat.getColor(context, R.color.input_focused)
        binding.etContent.setOnFocusChangeListener { view, hasFocus ->
            binding.tvLabel.setTextColor(if (hasFocus) colorFocused else colorDefault)
            binding.root.isActivated = hasFocus
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
            binding.tvLabel.setTextColor(
                if (hasFocus && binding.tvLabel.visibility == View.VISIBLE)
                    ContextCompat.getColor(context, R.color.input_focused)
                else
                    ContextCompat.getColor(context, R.color.section_label)
            )
            binding.root.isActivated = hasFocus
            listener.onFocusChange(view, hasFocus)
        }
    }

    fun setError(errorMessage: String?) {
        if (errorMessage != null) {
            isSelected = true
            binding.tvError.text = errorMessage
            binding.tvError.visibility = View.VISIBLE
        } else {
            isSelected = false
            binding.tvError.visibility = View.GONE
        }
    }
}
