package com.tit.nimonsapp.ui.map.components

import org.maplibre.android.geometry.LatLng

object MapUtils {
    fun userLocationToLatLng(
        latitude: Double,
        longitude: Double,
    ): LatLng = LatLng(latitude, longitude)

    fun calculateRotationFromNorth(azimuth: Double): Float = azimuth.toFloat()

    const val DEFAULT_ZOOM = 15.0
    const val MIN_ZOOM = 10.0
    const val MAX_ZOOM = 20.0

    fun isValidCoordinate(
        lat: Double,
        lng: Double,
    ): Boolean =
        lat != 0.0 && lng != 0.0 &&
            lat >= -90.0 && lat <= 90.0 &&
            lng >= -180.0 && lng <= 180.0
}
