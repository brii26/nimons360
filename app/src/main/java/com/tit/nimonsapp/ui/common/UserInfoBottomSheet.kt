package com.tit.nimonsapp.ui.common

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tit.nimonsapp.R
import com.tit.nimonsapp.ui.map.UserOnMap

class UserInfoBottomSheet(context: Context) : BottomSheetDialog(context) {

    private val avatarView: AvatarView
    private val tvUserName: TextView
    private val tvUserEmail: TextView
    private val tvBatteryValue: TextView
    private val ivCharging: ImageView
    private val tvLocationValue: TextView
    private val tvWifiValue: TextView
    private val ivWifiIcon: ImageView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_user_info_bottom_sheet, null)
        setContentView(view)

        avatarView = view.findViewById(R.id.avatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvBatteryValue = view.findViewById(R.id.tvBatteryValue)
        ivCharging = view.findViewById(R.id.ivCharging)
        tvLocationValue = view.findViewById(R.id.tvLocationValue)
        tvWifiValue = view.findViewById(R.id.tvWifiValue)
        ivWifiIcon = view.findViewById(R.id.ivWifiIcon)

        // Close when clicking outside
        setOnDismissListener { }
    }

    fun setUser(user: UserOnMap) {
        // Set avatar with first letter of name
        avatarView.setLetter(
            user.fullName.take(1).uppercase(),
            ContextCompat.getColor(context, R.color.colorPrimary)
        )

        // Set name and email
        tvUserName.text = user.fullName
        tvUserEmail.text = user.email

        // Set battery
        tvBatteryValue.text = "${user.batteryLevel}%"

        // Set charging icon
        ivCharging.visibility = if (user.isCharging) View.VISIBLE else View.GONE

        // Set location
        tvLocationValue.text = String.format("%.3f, %.3f", user.latitude, user.longitude)

        // Set network status
        tvWifiValue.text = when (user.internetStatus) {
            "wifi" -> "WiFi"
            "mobile" -> "Mobile"
            else -> "Unknown"
        }

        // Set WiFi icon color based on status
        ivWifiIcon.setColorFilter(
            when (user.internetStatus) {
                "wifi" -> Color.parseColor("#4CAF50") // Green
                "mobile" -> Color.parseColor("#FF9800") // Orange
                else -> Color.parseColor("#9E9E9E") // Gray
            }
        )
    }

    override fun show() {
        super.show()
        // Make dismiss on touch outside
        setCancelable(true)
    }
}
