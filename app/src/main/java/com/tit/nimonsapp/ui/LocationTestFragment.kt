package com.tit.nimonsapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.repository.LocationRepository
import kotlinx.coroutines.launch

class LocationTestFragment : Fragment() {

    private lateinit var locationRepo: LocationRepository

    private lateinit var tvLat: TextView
    private lateinit var tvLng: TextView
    private lateinit var tvRotation: TextView
    private lateinit var tvBattery: TextView
    private lateinit var tvCharging: TextView
    private lateinit var tvInternet: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocation) {
            startLocationUpdatesAndObserve()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationRepo = LocationRepository(requireContext())
        locationRepo.initialize()

        tvLat = view.findViewById(R.id.tvLat)
        tvLng = view.findViewById(R.id.tvLng)
        tvRotation = view.findViewById(R.id.tvRotation)
        tvBattery = view.findViewById(R.id.tvBattery)
        tvCharging = view.findViewById(R.id.tvCharging)
        tvInternet = view.findViewById(R.id.tvInternet)
        btnStart = view.findViewById(R.id.btnStart)
        btnStop = view.findViewById(R.id.btnStop)

        btnStart.setOnClickListener {
            checkAndRequestPermission()
        }

        btnStop.setOnClickListener {
            locationRepo.stopLocationUpdates()
        }
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdatesAndObserve()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startLocationUpdatesAndObserve() {
        locationRepo.startLocationUpdates()
        observeLocation()
    }

    private fun observeLocation() {
        lifecycleScope.launch {
            locationRepo.currentLocation.collect { location ->
                tvLat.text = "Latitude: ${location.latitude}"
                tvLng.text = "Longitude: ${location.longitude}"
                tvRotation.text = "Rotation: ${"%.1f".format(location.rotation)}°"
                tvBattery.text = "Battery: ${location.batteryLevel}%"
                tvCharging.text = "Charging: ${if (location.isCharging) "Yes" else "No"}"
                tvInternet.text = "Internet: ${location.internetStatus}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationRepo.stopLocationUpdates()
    }
}
