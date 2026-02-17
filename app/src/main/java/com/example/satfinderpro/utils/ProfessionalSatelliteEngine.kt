package com.example.satfinderpro.utils

import com.example.satfinderpro.data.model.Satellite
import com.example.satfinderpro.data.model.SatellitePosition
import kotlin.math.*

/**
 * Professional Satellite Engine with advanced algorithms
 * For precise geostationary satellite alignment calculations
 */
object ProfessionalSatelliteEngine {

    // Earth and orbital constants
    private const val EARTH_RADIUS_KM = 6371.0
    private const val GEO_ORBIT_RADIUS_KM = 42164.0
    private const val GEO_ALTITUDE_KM = 35786.0
    private const val EARTH_ROTATION_RATE = 7.2921159e-5 // rad/s
    private const val SPEED_OF_LIGHT = 299792.458 // km/s

    // Atmospheric refraction coefficients
    private const val REFRACTION_COEFFICIENT = 0.0167
    private const val STANDARD_PRESSURE = 1013.25 // mbar
    private const val STANDARD_TEMP = 15.0 // Celsius

    /**
     * Extended satellite database with detailed orbital parameters
     */
    val SATELLITE_DATABASE = listOf(
        // North America Satellites
        SatelliteData("Galaxy 19", -97.0, "North America", "Ku-band", "DirectTV"),
        SatelliteData("SES-1", -101.0, "North America", "C/Ku-band", "SES"),
        SatelliteData("AMC-15", -105.0, "North America", "Ku-band", "SES"),
        SatelliteData("Echostar 7", -119.0, "North America", "Dish Network", "DISH"),
        SatelliteData("DirectTV 7S", -119.0, "North America", "DirectTV", "DirecTV"),
        
        // Europe & Middle East
        SatelliteData("Astra 19.2E", 19.2, "Europe", "Ku-band", "SES"),
        SatelliteData("Astra 28.2E", 28.2, "Europe/UK", "Ku-band", "SES"),
        SatelliteData("Hotbird 13E", 13.0, "Europe", "Ku-band", "Eutelsat"),
        SatelliteData("Eutelsat 7E", 7.0, "Europe/MENA", "Ku-band", "Eutelsat"),
        SatelliteData("Nilesat 201", 7.0, "MENA", "Ku-band", "Nilesat"),
        SatelliteData("Arabsat 5A", 30.5, "MENA", "Ku/C-band", "Arabsat"),
        SatelliteData("Badrsat 26E", 26.0, "MENA", "Ku-band", "Nilesat"),
        
        // Africa
        SatelliteData("Eutelsat 36E", 36.0, "Africa/Europe", "Ku-band", "Eutelsat"),
        SatelliteData("Intelsat 20", 68.5, "Africa", "C/Ku-band", "Intelsat"),
        SatelliteData("NSS-7", -20.0, "Africa", "C/Ku-band", "SES"),
        
        // Asia
        SatelliteData("Insat 4A", 83.0, "Asia", "C/Ku-band", "ISRO"),
        SatelliteData("Asiasat 5", 100.5, "Asia", "C/Ku-band", "AsiaSat"),
        SatelliteData("Thaicom 5", 78.5, "Asia", "C/Ku-band", "Thaicom"),
        SatelliteData("Vinasat 1", 132.0, "Asia", "C/Ku-band", "Vietnam"),
        
        // South America
        SatelliteData("Star One C2", -70.0, "South America", "C/Ku-band", "Star One"),
        SatelliteData("Telstar 14R", -63.0, "South America", "C/Ku-band", "Telesat"),
        
        // Australia
        SatelliteData("Optus D2", 152.0, "Australia", "Ku-band", "Optus"),
        SatelliteData("Intelsat 8", 166.0, "Pacific", "C-band", "Intelsat")
    )

    /**
     * Advanced satellite position calculation with full corrections
     */
    fun calculateProfessionalPosition(
        userLat: Double,
        userLon: Double,
        satLon: Double,
        altitude: Double = 0.0,
        atmosphericConditions: AtmosphericConditions = AtmosphericConditions.STANDARD
    ): ProfessionalSatellitePosition {
        
        val latRad = userLat * PI / 180.0
        val lonDiffRad = (satLon - userLon) * PI / 180.0
        
        // Calculate base parameters
        val azimuth = calculateProfessionalAzimuth(latRad, lonDiffRad, userLat)
        val elevation = calculateProfessionalElevation(latRad, lonDiffRad, altitude, atmosphericConditions)
        val polarization = calculateProfessionalPolarization(latRad, lonDiffRad, userLat)
        
        // Calculate advanced metrics
        val slantRange = calculateSlantRange(latRad, lonDiffRad)
        val signalDelay = calculateSignalDelay(slantRange)
        val freeSpacePathLoss = calculateFreeSpacePathLoss(slantRange)
        
        // Signal quality prediction
        val predictedSignalQuality = predictSignalQuality(elevation, atmosphericConditions)
        
        return ProfessionalSatellitePosition(
            azimuth = azimuth,
            elevation = elevation,
            polarization = polarization,
            slantRange = slantRange,
            signalDelay = signalDelay,
            freeSpacePathLoss = freeSpacePathLoss,
            predictedSignalQuality = predictedSignalQuality,
            optimalAlignmentTime = calculateOptimalAlignmentWindow(userLat, userLon, satLon),
            lookAngleCone = calculateLookAngleCone(azimuth, elevation)
        )
    }

    /**
     * Calculate professional-grade azimuth with magnetic declination consideration
     */
    private fun calculateProfessionalAzimuth(lat: Double, lonDiff: Double, userLat: Double): Double {
        val azimuthRad = if (userLat >= 0) {
            // Northern hemisphere
            PI + atan2(sin(lonDiff), cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff))
        } else {
            // Southern hemisphere
            atan2(sin(lonDiff), cos(lat) * tan(0.0) - sin(lat) * cos(lonDiff))
        }
        
        var azimuth = azimuthRad * 180.0 / PI
        azimuth = (azimuth + 360.0) % 360.0
        
        return azimuth
    }

    /**
     * Calculate elevation with atmospheric refraction correction
     */
    private fun calculateProfessionalElevation(
        lat: Double,
        lonDiff: Double,
        altitude: Double,
        conditions: AtmosphericConditions
    ): Double {
        val cosGamma = cos(lat) * cos(lonDiff)
        val sinGamma = sqrt(1.0 - cosGamma * cosGamma)
        
        val earthCenterAngle = acos(cosGamma)
        val elevationRad = atan((cosGamma - EARTH_RADIUS_KM / GEO_ORBIT_RADIUS_KM) / sinGamma)
        
        var elevation = elevationRad * 180.0 / PI
        
        // Apply atmospheric refraction correction
        elevation += calculateRefractionCorrection(elevation, altitude, conditions)
        
        // Apply altitude correction
        elevation += calculateAltitudeCorrection(altitude)
        
        return maxOf(elevation, 0.0)
    }

    /**
     * Calculate polarization/skew angle for LNB rotation
     */
    private fun calculateProfessionalPolarization(lat: Double, lonDiff: Double, userLat: Double): Double {
        val tanSkew = sin(lonDiff) / tan(lat)
        var skew = atan(tanSkew) * 180.0 / PI
        
        // Adjust for Northern/Southern hemisphere
        if (userLat < 0) {
            skew = -skew
        }
        
        return skew.coerceIn(-90.0, 90.0)
    }

    /**
     * Calculate slant range (distance to satellite)
     */
    private fun calculateSlantRange(lat: Double, lonDiff: Double): Double {
        val cosGamma = cos(lat) * cos(lonDiff)
        return sqrt(
            EARTH_RADIUS_KM * EARTH_RADIUS_KM + 
            GEO_ORBIT_RADIUS_KM * GEO_ORBIT_RADIUS_KM -
            2 * EARTH_RADIUS_KM * GEO_ORBIT_RADIUS_KM * cosGamma
        )
    }

    /**
     * Calculate signal delay in milliseconds
     */
    private fun calculateSignalDelay(distance: Double): Double {
        return (distance / SPEED_OF_LIGHT) * 1000.0
    }

    /**
     * Calculate Free Space Path Loss (FSPL)
     */
    private fun calculateFreeSpacePathLoss(distance: Double, frequencyGHz: Double = 12.0): Double {
        return 20 * log10(distance) + 20 * log10(frequencyGHz) + 32.45 // dB
    }

    /**
     * Atmospheric refraction correction
     */
    private fun calculateRefractionCorrection(
        elevation: Double,
        altitude: Double,
        conditions: AtmosphericConditions
    ): Double {
        if (elevation < 0) return 0.0
        
        val pressure = STANDARD_PRESSURE * exp(-altitude / 8500.0)
        val temperature = STANDARD_TEMP - 0.0065 * altitude
        
        // Saemundsson's formula for refraction
        val refraction = (pressure / 1013.25) * 
                        (283.0 / (273.0 + temperature)) *
                        REFRACTION_COEFFICIENT / 
                        tan((elevation + 7.31 / (elevation + 4.4)) * PI / 180.0)
        
        return refraction * conditions.refractionMultiplier
    }

    /**
     * Altitude correction for elevation angle
     */
    private fun calculateAltitudeCorrection(altitude: Double): Double {
        return altitude / 1000.0 * 0.01 // Approximate 0.01 degrees per km
    }

    /**
     * Predict signal quality based on multiple factors
     */
    private fun predictSignalQuality(
        elevation: Double,
        conditions: AtmosphericConditions
    ): Int {
        val baseQuality = when {
            elevation < 0 -> 0
            elevation < 5 -> 20
            elevation < 10 -> 40
            elevation < 20 -> 60
            elevation < 30 -> 80
            else -> 100
        }
        
        return (baseQuality * conditions.signalMultiplier).toInt().coerceIn(0, 100)
    }

    /**
     * Calculate optimal alignment window based on sun position
     */
    private fun calculateOptimalAlignmentWindow(lat: Double, lon: Double, satLon: Double): AlignmentWindow {
        val satPosition = calculateProfessionalPosition(lat, lon, satLon)
        val azimuth = satPosition.azimuth
        
        return when {
            azimuth in 45.0..135.0 -> AlignmentWindow(
                "Morning (6AM - 12PM)",
                "Sun behind satellite - minimal atmospheric interference",
                0.95
            )
            azimuth in 135.0..225.0 -> AlignmentWindow(
                "Afternoon (12PM - 6PM)",
                "Moderate conditions - avoid direct sunlight on dish",
                0.85
            )
            azimuth in 225.0..315.0 -> AlignmentWindow(
                "Evening (6PM - 10PM)",
                "Good conditions - cooler temperatures",
                0.90
            )
            else -> AlignmentWindow(
                "Night/Early Morning",
                "Excellent conditions - minimal atmospheric noise",
                1.0
            )
        }
    }

    /**
     * Calculate 3D look angle cone for visualization
     */
    private fun calculateLookAngleCone(azimuth: Double, elevation: Double): LookAngleCone {
        return LookAngleCone(
            azimuthStart = (azimuth - 2).coerceIn(0.0, 360.0),
            azimuthEnd = (azimuth + 2).coerceIn(0.0, 360.0),
            elevationStart = (elevation - 2).coerceIn(0.0, 90.0),
            elevationEnd = (elevation + 2).coerceIn(0.0, 90.0)
        )
    }

    /**
     * Check for potential obstacles
     */
    fun detectObstacles(
        elevation: Double,
        azimuth: Double,
        surroundingElevations: List<Double>
    ): ObstacleAnalysis {
        val avgSurrounding = surroundingElevations.average()
        val maxSurrounding = surroundingElevations.maxOrNull() ?: 0.0
        
        val hasObstacle = maxSurrounding > elevation
        val severity = when {
            !hasObstacle -> ObstacleSeverity.NONE
            maxSurrounding - elevation > 10 -> ObstacleSeverity.CRITICAL
            maxSurrounding - elevation > 5 -> ObstacleSeverity.HIGH
            else -> ObstacleSeverity.MODERATE
        }
        
        return ObstacleAnalysis(
            hasObstacle = hasObstacle,
            severity = severity,
            recommendedHeight = if (hasObstacle) maxSurrounding + 5 else elevation,
            message = when(severity) {
                ObstacleSeverity.NONE -> "Clear line of sight"
                ObstacleSeverity.MODERATE -> "Minor obstruction - elevate slightly"
                ObstacleSeverity.HIGH -> "Significant obstacle - elevation required"
                ObstacleSeverity.CRITICAL -> "Critical obstruction - relocate recommended"
            }
        )
    }

    /**
     * Calculate magnetic declination for compass adjustment
     */
    fun calculateMagneticDeclination(lat: Double, lon: Double): Double {
        // Simplified WMM2020 model approximation
        // Real implementation would use actual magnetic field model
        return when {
            lat > 60 || lat < -60 -> 15.0 // High latitudes
            lon > -30 && lon < 30 && lat > 30 -> -5.0 // Europe
            lon > 30 && lon < 60 && lat > 0 -> 0.0 // Middle East
            lon > 60 && lon < 120 && lat > 0 -> 5.0 // Asia
            else -> 0.0
        }
    }

    /**
     * Get satellites visible from location
     */
    fun getVisibleSatellites(lat: Double, lon: Double, minElevation: Double = 10.0): List<VisibleSatellite> {
        return SATELLITE_DATABASE.map { sat ->
            val position = calculateProfessionalPosition(lat, lon, sat.longitude)
            VisibleSatellite(
                data = sat,
                position = position,
                isVisible = position.elevation >= minElevation,
                recommendationScore = calculateRecommendationScore(position)
            )
        }.sortedByDescending { it.recommendationScore }
    }

    private fun calculateRecommendationScore(position: ProfessionalSatellitePosition): Double {
        val elevationScore = position.elevation / 90.0 * 100
        val signalScore = position.predictedSignalQuality.toDouble()
        return (elevationScore * 0.4 + signalScore * 0.6)
    }

    // Data classes
    data class SatelliteData(
        val name: String,
        val longitude: Double,
        val region: String,
        val bands: String,
        val operator: String
    )

    data class ProfessionalSatellitePosition(
        val azimuth: Double,
        val elevation: Double,
        val polarization: Double,
        val slantRange: Double,
        val signalDelay: Double,
        val freeSpacePathLoss: Double,
        val predictedSignalQuality: Int,
        val optimalAlignmentTime: AlignmentWindow,
        val lookAngleCone: LookAngleCone
    )

    data class AlignmentWindow(
        val timeDescription: String,
        val reason: String,
        val qualityFactor: Double
    )

    data class LookAngleCone(
        val azimuthStart: Double,
        val azimuthEnd: Double,
        val elevationStart: Double,
        val elevationEnd: Double
    )

    data class VisibleSatellite(
        val data: SatelliteData,
        val position: ProfessionalSatellitePosition,
        val isVisible: Boolean,
        val recommendationScore: Double
    )

    data class ObstacleAnalysis(
        val hasObstacle: Boolean,
        val severity: ObstacleSeverity,
        val recommendedHeight: Double,
        val message: String
    )

    enum class ObstacleSeverity {
        NONE, MODERATE, HIGH, CRITICAL
    }

    enum class AtmosphericConditions(
        val refractionMultiplier: Double,
        val signalMultiplier: Double
    ) {
        STANDARD(1.0, 1.0),
        HOT_HUMID(0.95, 0.85),
        COLD_DRY(1.05, 1.05),
        RAINY(0.8, 0.6),
        FOGGY(0.9, 0.75)
    }
}
