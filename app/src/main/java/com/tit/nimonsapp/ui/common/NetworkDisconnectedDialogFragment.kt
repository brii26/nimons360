package com.tit.nimonsapp.ui.common

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NetworkDisconnectedDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("No internet connection")
            .setMessage("You are currently offline. Some features may not work.")
            .setPositiveButton("OK", null)
            .create()

    override fun onStart() {
        super.onStart()
        isCancelable = true
        dialog?.setCanceledOnTouchOutside(true)
    }

    companion object {
        const val TAG = "network_disconnected_dialog"
    }
}
