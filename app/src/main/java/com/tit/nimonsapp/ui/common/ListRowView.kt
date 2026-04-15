package com.tit.nimonsapp.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tit.nimonsapp.R

/**
 * ListRowView -> komponen baris list buat families
 *
 * item list: 
 * - daftar family
 * - daftar member
 * - fitur lain yang bentuknya list (role, group, dll)
 *
 * Attribute XML:
 * app:rowPrimaryText : main tex (contoh: nama family / nama user)
 * app:rowSecondaryText : additional text (contoh: jumlah member / role)
 * app:rowRightType :element type di kanan:
 * 0 = tidak ada
 * 1 = icon pin
 * 2 = badge
 *
 * app:rowBadgeText -> teks di badge (contoh: "3 baru", "admin")
 *
 * Avatar:
 * Avatar bisa diakses langsung dari view buat diubah tampilannya:
 *
 * row.avatar.setLetter("F", color)
 * row.avatar.setImage { Coil.load(it, url) }
 *
 * Contoh pemakaian:
 * Family: "Keluarga Budi" + "5 anggota"
 * Member: "Andi" + "Admin"
 */
 
class ListRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    val avatar: AvatarView
    private val tvPrimary: TextView
    private val tvSecondary: TextView
    private val ivPin: ImageView
    private val tvBadge: TextView

    companion object {
        const val RIGHT_NONE = 0
        const val RIGHT_PIN = 1
        const val RIGHT_BADGE = 2
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_list_row, this, true)
        avatar = findViewById(R.id.row_avatar)
        tvPrimary = findViewById(R.id.tv_row_primary)
        tvSecondary = findViewById(R.id.tv_row_secondary)
        ivPin = findViewById(R.id.iv_pin)
        tvBadge = findViewById(R.id.tv_badge)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.ListRowView)
            tvPrimary.text = ta.getString(R.styleable.ListRowView_rowPrimaryText) ?: ""
            tvSecondary.text = ta.getString(R.styleable.ListRowView_rowSecondaryText) ?: ""
            val rightType = ta.getInt(R.styleable.ListRowView_rowRightType, RIGHT_NONE)
            val badgeText = ta.getString(R.styleable.ListRowView_rowBadgeText) ?: ""
            ta.recycle()
            setRightType(rightType, badgeText)
        }
    }

    var primaryText: String
        get() = tvPrimary.text.toString()
        set(v) { tvPrimary.text = v }

    var secondaryText: String
        get() = tvSecondary.text.toString()
        set(v) { tvSecondary.text = v }

    fun setRightType(type: Int, badgeText: String = "") {
        ivPin.visibility = if (type == RIGHT_PIN) View.VISIBLE else View.GONE
        tvBadge.visibility = if (type == RIGHT_BADGE) View.VISIBLE else View.GONE
        if (type == RIGHT_BADGE) tvBadge.text = badgeText
    }
}
