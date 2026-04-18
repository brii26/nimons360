package com.tit.nimonsapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.tit.nimonsapp.MainActivity
import com.tit.nimonsapp.R
import com.tit.nimonsapp.data.repository.DeviceLocation
import com.tit.nimonsapp.data.repository.LocationRepository
import com.tit.nimonsapp.ui.common.AvatarView
import com.tit.nimonsapp.ui.common.UserInfoBottomSheet
import com.tit.nimonsapp.ui.map.components.MapMarkerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.markerview.MarkerView
import org.maplibre.android.plugins.markerview.MarkerViewManager

class MapFragment :
    Fragment(),
    OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var markerViewManager: MarkerViewManager

    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: MapViewModel

    private val markers = mutableMapOf<Int, MarkerView>()
    private var currentUserMarker: MarkerView? = null
    private var currentUserMarkerView: MapMarkerView? = null

    private var userInfoBottomSheet: UserInfoBottomSheet? = null
    private lateinit var mapSearchEditText: EditText

    private lateinit var cardAvatar: AvatarView
    private lateinit var cardName: TextView
    private lateinit var cardEmail: TextView
    private lateinit var cardBattery: TextView
    private lateinit var cardCharging: ImageView
    private lateinit var cardLocation: TextView
    private lateinit var cardWifi: TextView
    private lateinit var cardWifiIcon: ImageView

    private var locationCollectionJob: Job? = null
    private var offlineCleanupJob: Job? = null
    private var isMapStyleReady = false

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (fineLocation) {
                viewModel.setGpsPermissionGranted(true)
                startLocationUpdates()
            } else {
                viewModel.setGpsPermissionGranted(false)
                showSnackbar("Location permission required - enable to share your position")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        MapLibre.getInstance(requireContext())
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapSearchEditText =
            view.findViewById<EditText>(R.id.et_search)
                ?: view.findViewById<View>(R.id.map_search_bar)?.findViewById(R.id.et_search)
                ?: EditText(requireContext())

        mapView =
            view.findViewById(R.id.mapView) ?: MapView(requireContext()).apply {
                val params =
                    FrameLayout.LayoutParams(
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        locationRepository = LocationRepository(requireContext())
        locationRepository.initialize()

        val factory = MapViewModelFactory(requireActivity().application, MainActivity.webSocketRepository)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]

        val cardView = view.findViewById<View>(R.id.map_user_card)
        cardAvatar = cardView.findViewById(R.id.map_card_avatar)
        cardName = cardView.findViewById(R.id.map_card_name)
        cardEmail = cardView.findViewById(R.id.map_card_email)
        cardBattery = cardView.findViewById(R.id.map_card_battery)
        cardCharging = cardView.findViewById(R.id.map_card_charging)
        cardLocation = cardView.findViewById(R.id.map_card_location)
        cardWifi = cardView.findViewById(R.id.map_card_wifi)
        cardWifiIcon = cardView.findViewById(R.id.map_card_wifi_icon)

        observeViewModel()
        setupSearchBar()

        viewModel.loadCurrentUserProfile()
        viewModel.loadFamilyMembers()
        viewModel.startObservingWebSocket()

        requestLocationPermission()
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        map.setStyle(
            Style.Builder().fromUri("https://tiles.basemaps.cartocdn.com/gl/voyager-gl-style/style.json"),
        ) {
            markerViewManager = MarkerViewManager(mapView, map)
            isMapStyleReady = true

            map.uiSettings.isLogoEnabled = false
            map.uiSettings.isAttributionEnabled = true

            map.uiSettings.setCompassGravity(Gravity.BOTTOM or Gravity.END)
            map.uiSettings.setCompassMargins(0, 0, 40, 240)

            updateOtherUsersMarkers(viewModel.getFilteredUsers())
            val currentLocation = viewModel.uiState.value.currentLocation
            if (currentLocation.latitude != 0.0 && currentLocation.longitude != 0.0) {
                updateCurrentUserMarker(currentLocation)
            }
        }
    }

    private fun requestLocationPermission() {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.setGpsPermissionGranted(true)
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRepository.startLocationUpdates()

        if (locationCollectionJob == null) {
            locationCollectionJob =
                viewLifecycleOwner.lifecycleScope.launch {
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
        }

        if (offlineCleanupJob == null) {
            offlineCleanupJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    while (true) {
                        delay(5000)
                        viewModel.removeOfflineUsers()
                    }
                }
        }
    }

    private fun updateCurrentUserMarker(location: DeviceLocation) {
        if (!::mapLibreMap.isInitialized) return
        if (!::markerViewManager.isInitialized) return
        if (!isMapStyleReady) return

        val position = LatLng(location.latitude, location.longitude)
        val profile = viewModel.uiState.value.currentUserProfile
        val initial = profile?.fullName?.take(1)?.uppercase() ?: "M"

        if (currentUserMarker == null) {
            currentUserMarkerView =
                MapMarkerView(requireContext(), MapMarkerView.MarkerType.CURRENT_USER).apply {
                    setMarkerData(initial)
                    setMarkerRotation(location.rotation.toFloat())
                }

            currentUserMarker =
                MarkerView(position, currentUserMarkerView!!).apply {
                    markerViewManager.addMarker(this)
                }

            currentUserMarkerView?.setOnClickListener {
                showMyInfoBottomSheet()
            }

            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0))
        } else {
            currentUserMarker?.setLatLng(position)
            currentUserMarkerView?.setMarkerRotation(location.rotation.toFloat())
            currentUserMarkerView?.setMarkerData(initial)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (isMapStyleReady) {
                    updateOtherUsersMarkers(viewModel.getFilteredUsers())
                }
                state.meta.errorMessage?.let { message ->
                    showSnackbar(message)
                }
                bindUserCard(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
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

    private fun setupSearchBar() {
        mapSearchEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}

                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateSearchQuery(s?.toString() ?: "")
                }
            },
        )
    }

    private fun updateOtherUsersMarkers(users: Map<Int, UserOnMap>) {
        if (!::mapLibreMap.isInitialized) return
        if (!::markerViewManager.isInitialized) return
        if (!isMapStyleReady) return
        if (!isAdded || view == null) return

        val currentIds = markers.keys.toList()
        val newIds = users.keys
        val myId =
            viewModel.uiState.value.currentUserProfile
                ?.id

        currentIds.forEach { userId ->
            if (userId !in newIds) {
                markers[userId]?.let { markerViewManager.removeMarker(it) }
                markers.remove(userId)
            }
        }

        users.forEach { (userId, user) ->
            if (userId == myId) return@forEach

            val position = LatLng(user.latitude, user.longitude)

            if (userId in markers) {
                markers[userId]?.setLatLng(position)
            } else {
                val markerView =
                    MapMarkerView(requireContext(), MapMarkerView.MarkerType.OTHER_USER).apply {
                        setMarkerData(
                            user.fullName.take(1).uppercase(),
                            getMarkerColor(userId),
                        )
                    }

                val marker =
                    MarkerView(position, markerView).apply {
                        markerViewManager.addMarker(this)
                    }

                markers[userId] = marker

                markerView.setOnClickListener {
                    viewModel.selectUser(userId)
                }
            }
        }
    }

    private fun bindUserCard(state: MapUiState) {
        val profile = state.currentUserProfile
        val location = state.currentLocation

        val name = profile?.fullName ?: ""
        val email = profile?.email ?: ""

        cardAvatar.setLetter(
            name.take(1).uppercase().ifBlank { "?" },
            ContextCompat.getColor(requireContext(), R.color.profile_avatar_blue),
        )
        cardName.text = if (name.length > 30) name.take(30) + "…" else name
        cardEmail.text = if (email.length > 35) email.take(35) + "…" else email

        cardBattery.text = "${location.batteryLevel}%"
        cardCharging.visibility = if (location.isCharging) View.VISIBLE else View.GONE

        val lat = location.latitude
        val lng = location.longitude
        cardLocation.text = if (lat == 0.0 && lng == 0.0) "—" else String.format("%.3f,\n%.3f", lat, lng)

        cardWifi.text = when (location.internetStatus) {
            "wifi" -> "WiFi"
            "mobile" -> "Mobile"
            else -> "—"
        }
        cardWifiIcon.setColorFilter(
            when (location.internetStatus) {
                "wifi" -> ContextCompat.getColor(requireContext(), R.color.nimons_green)
                "mobile" -> Color.parseColor("#FF9800")
                else -> ContextCompat.getColor(requireContext(), R.color.section_label)
            },
        )
    }

    private fun getMarkerColor(userId: Int): Int {
        val colors =
            arrayOf(
                R.color.avatar_1,
                R.color.avatar_2,
                R.color.avatar_3,
                R.color.avatar_4,
                R.color.avatar_5,
                R.color.avatar_6,
                R.color.avatar_7,
                R.color.avatar_8,
                R.color.avatar_9,
            )
        val colorRes = colors[Math.abs(userId) % colors.size]
        return ContextCompat.getColor(requireContext(), colorRes)
    }

    private fun showUserInfoBottomSheet(user: UserOnMap) {
        dismissUserInfoBottomSheet()

        userInfoBottomSheet =
            UserInfoBottomSheet(requireContext()).apply {
                setUser(user)
                setOnDismissListener {
                    userInfoBottomSheet = null
                }
                show()
            }
    }

    private fun showMyInfoBottomSheet() {
        dismissUserInfoBottomSheet()

        val currentUser = viewModel.uiState.value.currentLocation
        val profile = viewModel.uiState.value.currentUserProfile
        val myUserOnMap =
            UserOnMap(
                userId = profile?.id ?: 0,
                fullName = profile?.fullName ?: "Me",
                email = profile?.email ?: "me@std.stei.itb.ac.id",
                latitude = currentUser.latitude,
                longitude = currentUser.longitude,
                rotation = currentUser.rotation,
                batteryLevel = currentUser.batteryLevel,
                isCharging = currentUser.isCharging,
                internetStatus = currentUser.internetStatus,
                lastUpdateTimestamp = System.currentTimeMillis(),
            )
        userInfoBottomSheet =
            UserInfoBottomSheet(requireContext()).apply {
                setUser(myUserOnMap)
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
        locationRepository.stopLocationUpdates()
        dismissUserInfoBottomSheet()

        locationCollectionJob?.cancel()
        locationCollectionJob = null
        offlineCleanupJob?.cancel()
        offlineCleanupJob = null

        markers.clear()
        currentUserMarker = null
        currentUserMarkerView = null
        isMapStyleReady = false

        mapView.onDestroy()
    }
}
