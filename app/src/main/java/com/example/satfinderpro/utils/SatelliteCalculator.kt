package com.example.satfinderpro.utils

import com.example.satfinderpro.data.model.SatellitePosition
import kotlin.math.*

object SatelliteCalculator {
    /**
     * Calculate azimuth and elevation angles for satellite positioning
     * 
     * @param userLat User's latitude in degrees
     * @param userLon User's longitude in degrees
     * @param satLon Satellite's longitude in degrees (positive for East, negative for West)
     * @return SatellitePosition containing azimuth, elevation, and polarization angles
     */
    fun calculateSatellitePosition(
        userLat: Double,
        userLon: Double,
        satLon: Double
    ): SatellitePosition {
        // Convert degrees to radians
        val lat = userLat * PI / 180.0
        val lon = userLon * PI / 180.0
        val satLonRad = satLon * PI / 180.0

        // Earth radius in km
        val earthRadius = 6371.0
        
        // Satellite orbital radius (geostationary orbit)
        val satRadius = 42164.0

        // Calculate the difference in longitude
        val lonDiff = satLonRad - lon

        // Calculate azimuth
        val azimuth = calculateAzimuth(lat, lonDiff)

        // Calculate elevation
        val elevation = calculateElevation(lat, lonDiff)

        // Calculate polarization (skew angle)
        val polarization = calculatePolarization(lat, lonDiff)

        return SatellitePosition(
            azimuth = azimuth,
            elevation = elevation,
            polarization = polarization
        )
    }

    /**
     * Calculate azimuth angle (0° = North, 90° = East, 180° = South, 270° = West)
     */
    private fun calculateAzimuth(lat: Double, lonDiff: Double): Double {
        val y = sin(lonDiff)
        val x = cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff)
        var azimuth = atan2(y, x) * 180.0 / PI
        
        // Normalize to 0-360 degrees
        azimuth = (azimuth + 360.0) % 360.0
        
        return azimuth
    }

    /**
     * Calculate elevation angle (angle above horizon)
     */
    private fun calculateElevation(lat: Double, lonDiff: Double): Double {
        val cosE = cos(lat) * cos(lonDiff)
        val elevation = atan(cosE / sqrt(1.0 - cosE * cosE)) * 180.0 / PI
        
        return maxOf(elevation, 0.0) // Elevation cannot be negative
    }

    /**
     * Calculate polarization angle (skew angle)
     * This is the rotation angle of the LNB (Low Noise Block)
     */
    private fun calculatePolarization(lat: Double, lonDiff: Double): Double {
        val tanPol = -sin(lonDiff) / (cos(lat) * tan(lat))
        var polarization = atan(tanPol) * 180.0 / PI
        
        // Adjust based on hemisphere
        if (lat > 0) {
            polarization = -polarization
        }
        
        return polarization
    }

    /**
     * Calculate signal quality based on elevation angle
     * Higher elevation = better signal
     * 
     * @param elevation Elevation angle in degrees
     * @return Signal quality as percentage (0-100)
     */
    fun calculateSignalQuality(elevation: Double): Int {
        return when {
            elevation < 0 -> 0
            elevation < 5 -> (elevation / 5 * 20).toInt()
            elevation < 10 -> 20 + ((elevation - 5) / 5 * 20).toInt()
            elevation < 20 -> 40 + ((elevation - 10) / 10 * 20).toInt()
            elevation < 30 -> 60 + ((elevation - 20) / 10 * 20).toInt()
            else -> 100
        }
    }

    /**
     * Check if satellite is aligned (within acceptable tolerance)
     * 
     * @param currentAzimuth Current compass azimuth
     * @param targetAzimuth Target satellite azimuth
     * @param currentElevation Current antenna elevation
     * @param targetElevation Target satellite elevation
     * @param azimuthTolerance Acceptable azimuth tolerance in degrees
     * @param elevationTolerance Acceptable elevation tolerance in degrees
     * @return True if aligned within tolerance
     */
    fun isAligned(
        currentAzimuth: Double,
        targetAzimuth: Double,
        currentElevation: Double,
        targetElevation: Double,
        azimuthTolerance: Double = 2.0,
        elevationTolerance: Double = 2.0
    ): Boolean {
        val azimuthDiff = minOf(
            abs(currentAzimuth - targetAzimuth),
            360.0 - abs(currentAzimuth - targetAzimuth)
        )
        val elevationDiff = abs(currentElevation - targetElevation)

        return azimuthDiff <= azimuthTolerance && elevationDiff <= elevationTolerance
    }
}
