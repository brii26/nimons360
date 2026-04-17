package com.tit.nimonsapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.network.WebSocketManager
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.repository.DeviceLocation
import com.tit.nimonsapp.data.repository.LocationRepository
import com.tit.nimonsapp.ui.common.UserInfoBottomSheet
import com.tit.nimonsapp.ui.map.components.MapMarkerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.markerview.MarkerView
import org.maplibre.android.plugins.markerview.MarkerViewManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var markerViewManager: MarkerViewManager

    private lateinit var locationRepository: LocationRepository
    private lateinit var webSocketRepository: WebSocketRepository
    private lateinit var viewModel: MapViewModel

    private val markers = mutableMapOf<Int, MarkerView>() // userId -> Marker
    private var currentUserMarker: MarkerView? = null

    private var currentUserMarkerView: MapMarkerView? = null

    private var userInfoBottomSheet: UserInfoBottomSheet? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocation) {
            startLocationUpdatesAndConnect()
        } else {
            showSnackbar("Location permission required for map feature")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        MapLibre.getInstance(requireContext())
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.mapView) ?: MapView(requireContext()).apply {
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
            layoutParams = params
            (view as FrameLayout).addView(this)
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationRepository = LocationRepository(requireContext())
        locationRepository.initialize()

        val webSocketManager = WebSocketManager(OkHttpClient.Builder().build())
        webSocketRepository = WebSocketRepository(webSocketManager)

        val token = getToken()

        val factory = MapViewModelFactory(requireActivity().application, webSocketRepository, token)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]

        observeViewModel()
        requestLocationPermission()
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        markerViewManager = MarkerViewManager(mapView, map)

        map.setStyle(
            Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")
        )
    }
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdatesAndConnect()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }


    @SuppressLint
    private fun startLocationUpdatesAndConnect() {
        locationRepository.startLocationUpdates()

        lifecycleScope.launch {
            locationRepository.currentLocation.collect { location ->
                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    viewModel.updateCurrentLocation(
                        location.latitude,
                        location.longitude,
                        location.rotation,
                        location.batteryLevel,
                        location.isCharging,
                        location.internetStatus,
                    )
                    updateCurrentUserMarker(location)
                }
            }
        }

        viewModel.connectWebSocket()

        lifecycleScope.launch {
            while (true) {
                delay(5000)
                viewModel.removeOfflineUsers()
            }
        }
    }

    private fun updateCurrentUserMarker(location: DeviceLocation) {
        if (!::mapLibreMap.isInitialized) return

        val position = LatLng(location.latitude, location.longitude)

        if (currentUserMarker == null) {
            // Buat UI kita dan simpan ke variabel referensi
            currentUserMarkerView = MapMarkerView(requireContext(), MapMarkerView.MarkerType.CURRENT_USER).apply {
                setMarkerData("M")
                setMarkerRotation(location.rotation.toFloat())
            }

            // Bungkus ke MarkerView MapLibre dan tampilkan ke peta
            currentUserMarker = MarkerView(position, currentUserMarkerView!!).apply {
                markerViewManager.addMarker(this)
            }
        } else {
            // Update posisi koordinat di amplop MapLibre
            currentUserMarker?.setLatLng(position)

            currentUserMarkerView?.setMarkerRotation(location.rotation.toFloat())
        }

        mapLibreMap.cameraPosition = CameraPosition.Builder()
            .target(position)
            .zoom(15.0)
            .build()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateOtherUsersMarkers(state.otherUsers)
                state.meta.errorMessage?.let { message ->
                    showSnackbar(message)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.selectedUserId?.let { userId ->
                    state.otherUsers[userId]?.let { user ->
                        showUserInfoBottomSheet(user)
                    }
                    viewModel.selectUser(null)
                }
            }
        }
    }

    private fun updateOtherUsersMarkers(users: Map<Int, UserOnMap>) {
        // guard lateinit
        if (!::markerViewManager.isInitialized) return

        val currentIds = markers.keys.toList()
        val newIds = users.keys

        currentIds.forEach { userId ->
            if (userId !in newIds) {
                markers[userId]?.let { markerViewManager.removeMarker(it) }
                markers.remove(userId)
            }
        }

        users.forEach { (userId, user) ->
            val position = LatLng(user.latitude, user.longitude)

            if (userId in markers) {
                markers[userId]?.setLatLng(position)
            } else {
                val markerView = MapMarkerView(requireContext(), MapMarkerView.MarkerType.OTHER_USER)
                markerView.setMarkerData(
                    user.fullName.take(1).uppercase(),
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                )

                val marker = MarkerView(position, markerView).apply {
                    markerViewManager.addMarker(this)
                }

                markers[userId] = marker

                markerView.setOnClickListener {
                    viewModel.selectUser(userId)
                }
            }
        }
    }

    private fun showUserInfoBottomSheet(user: UserOnMap) {
        dismissUserInfoBottomSheet()

        userInfoBottomSheet = UserInfoBottomSheet(requireContext()).apply {
            setUser(user)
            setOnDismissListener {
                userInfoBottomSheet = null
            }
            show()
        }
    }

    private fun dismissUserInfoBottomSheet() {
        userInfoBottomSheet?.dismiss()
        userInfoBottomSheet = null
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    // nanti pake token asli dari store
    private fun getToken(): String {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjMsImVtYWlsIjoiMTM1MjIwODRAc3RkLnN0ZWkuaXRiLmFjLmlkIiwiaWF0IjoxNzc2MzQ1NjMyLCJleHAiOjE3NzY5NTA0MzJ9.oP1HhfTm6rSXgRD8my96PQqtRWzL1ytaPf5NEwa-QKY"
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnectWebSocket()
        locationRepository.stopLocationUpdates()
        dismissUserInfoBottomSheet()
        mapView.onDestroy()
    }
}
