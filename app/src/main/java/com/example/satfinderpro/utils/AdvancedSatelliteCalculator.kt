package com.example.satfinderpro.utils

import com.example.satfinderpro.data.model.SatellitePosition
import kotlin.math.*

object AdvancedSatelliteCalculator {
    
    private const val EARTH_RADIUS = 6371.0
    private const val GEO_ORBIT_RADIUS = 42164.0
    private const val EARTH_FLATTENING = 1.0 / 298.257223563
    
    /**
     * Enhanced satellite position calculation with atmospheric refraction
     */
    fun calculateEnhancedPosition(
        userLat: Double,
        userLon: Double,
        satLon: Double,
        altitude: Double = 0.0
    ): SatellitePosition {
        val lat = userLat * PI / 180.0
        val lon = userLon * PI / 180.0
        val satLonRad = satLon * PI / 180.0
        val lonDiff = satLonRad - lon
        
        val azimuth = calculatePreciseAzimuth(lat, lonDiff, userLat)
        val elevation = calculatePreciseElevation(lat, lonDiff, altitude)
        val polarization = calculatePolarization(lat, lonDiff)
        
        return SatellitePosition(
            azimuth = azimuth,
            elevation = elevation,
            polarization = polarization
        )
    }
    
    private fun calculatePreciseAzimuth(lat: Double, lonDiff: Double, userLat: Double): Double {
        val y = sin(lonDiff)
        val x = cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff)
        var azimuth = atan2(y, x) * 180.0 / PI
        
        if (userLat >= 0) {
            azimuth = 180.0 + atan2(sin(lonDiff), cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff)) * 180.0 / PI
        } else {
            azimuth = atan2(sin(lonDiff), cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff)) * 180.0 / PI
        }
        
        azimuth = (azimuth + 360.0) % 360.0
        return azimuth
    }
    
    private fun calculatePreciseElevation(lat: Double, lonDiff: Double, altitude: Double): Double {
        val cosE = cos(lat) * cos(lonDiff)
        val distance = sqrt(1.0 - cosE * cosE)
        var elevation = atan(cosE / distance) * 180.0 / PI
        
        elevation = applyAtmosphericRefraction(elevation, altitude)
        return maxOf(elevation, 0.0)
    }
    
    private fun calculatePolarization(lat: Double, lonDiff: Double): Double {
        val tanPol = -sin(lonDiff) / (cos(lat) * tan(lat))
        var polarization = atan(tanPol) * 180.0 / PI
        
        if (lat > 0) {
            polarization = -polarization
        }
        
        return polarization
    }
    
    private fun applyAtmosphericRefraction(elevation: Double, altitude: Double): Double {
        if (elevation < 0) return elevation
        
        val pressure = 1013.25 * exp(-altitude / 8500.0)
        val temperature = 15.0 - 0.0065 * altitude
        val refraction = (pressure / 1013.25) * (283.0 / (273.0 + temperature)) * 
                        (1.02 / tan((elevation + 10.3 / (elevation + 5.11)) * PI / 180.0)) / 60.0
        
        return elevation + refraction
    }
    
    /**
     * Calculate distance to satellite in km
     */
    fun calculateSatelliteDistance(userLat: Double, userLon: Double, satLon: Double): Double {
        val lat = userLat * PI / 180.0
        val lonDiff = (satLon - userLon) * PI / 180.0
        
        val cosE = cos(lat) * cos(lonDiff)
        val distance = sqrt(EARTH_RADIUS * EARTH_RADIUS + GEO_ORBIT_RADIUS * GEO_ORBIT_RADIUS - 
                           2 * EARTH_RADIUS * GEO_ORBIT_RADIUS * cosE)
        
        return distance
    }
    
    /**
     * Calculate signal delay in milliseconds
     */
    fun calculateSignalDelay(distance: Double): Double {
        val speedOfLight = 299792.458
        return (distance / speedOfLight) * 1000.0
    }
    
    /**
     * Predict optimal alignment time based on sun position
     */
    fun calculateOptimalAlignmentTime(userLat: Double, userLon: Double, satLon: Double): String {
        val azimuth = calculatePreciseAzimuth(userLat * PI / 180.0, (satLon - userLon) * PI / 180.0, userLat)
        
        return when {
            azimuth in 45.0..135.0 -> "Morning (Sun from East)"
            azimuth in 135.0..225.0 -> "Afternoon (Sun from South)"
            azimuth in 225.0..315.0 -> "Evening (Sun from West)"
            else -> "Night or Early Morning"
        }
    }
}
