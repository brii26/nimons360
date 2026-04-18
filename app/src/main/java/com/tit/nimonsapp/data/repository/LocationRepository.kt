package com.tit.nimonsapp.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeviceLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rotation: Double = 0.0,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val internetStatus: String = "unknown",
)

class LocationRepository(
    private val context: Context,
) {
    private val _currentLocation = MutableStateFlow(DeviceLocation())
    val currentLocation: StateFlow<DeviceLocation> = _currentLocation.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager

    // Sensor data + orientation/rotation buffers
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    _currentLocation.update {
                        it.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            internetStatus = getInternetStatus(),
                        )
                    }
                    // printing logcat
                    val current = _currentLocation.value
                    Log.d("NIMONS_GPS", "Lat: ${current.latitude}, Lon: ${current.longitude}, Rot: ${current.rotation}")
                }
            }
        }

    private val sensorListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                synchronized(this) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                        Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    }
                    updateOrientation()
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {}
        }

    private var batteryReceiver: BroadcastReceiver? = null

    fun initialize() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Start battery monitoring langsung pas init
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging =
                        plugged != 0 && (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)

                    _currentLocation.update {
                        it.copy(
                            batteryLevel = level,
                            isCharging = isCharging,
                        )
                    }
                }
            }
        context.registerReceiver(batteryReceiver, batteryIntentFilter)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        // 1s Interval
        val locationRequest =
            LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(1000)
                .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Sensor di-register only pas active to save battery
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        accelerometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
    }

    private fun updateOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            // Azimuth is orientationAngles[0]. Convert to degrees 0-360
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuth < 0f) azimuth += 360f

            _currentLocation.update { it.copy(rotation = azimuth.toDouble()) }
        }
    }

    private fun getInternetStatus(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return "mobile"
        val actNw = cm.getNetworkCapabilities(nw) ?: return "mobile"

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "mobile"
            else -> "mobile"
        }
    }

    fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            sensorManager.unregisterListener(sensorListener)
            batteryReceiver?.let {
                context.unregisterReceiver(it)
                batteryReceiver = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}
