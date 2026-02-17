package com.example.satfinderpro.utils

import kotlin.math.*

/**
 * Smart Alignment Assistant with AI-powered suggestions
 * Provides intelligent guidance for satellite alignment
 */
object SmartAlignmentAssistant {
    
    data class AlignmentGuidance(
        val direction: String,
        val intensity: Float,
        val suggestion: String,
        val confidence: Float
    )
    
    data class AlignmentMetrics(
        val azimuthAccuracy: Float,
        val elevationAccuracy: Float,
        val overallScore: Float,
        val estimatedTimeToAlign: Int
    )
    
    /**
     * Provides smart guidance based on current and target positions
     */
    fun getAlignmentGuidance(
        currentAzimuth: Double,
        targetAzimuth: Double,
        currentElevation: Double,
        targetElevation: Double
    ): AlignmentGuidance {
        val azimuthDiff = normalizeAngleDifference(currentAzimuth, targetAzimuth)
        val elevationDiff = targetElevation - currentElevation
        
        val direction = when {
            abs(azimuthDiff) < 2 && abs(elevationDiff) < 2 -> "LOCKED"
            abs(azimuthDiff) > abs(elevationDiff) -> {
                if (azimuthDiff > 0) "ROTATE RIGHT" else "ROTATE LEFT"
            }
            else -> {
                if (elevationDiff > 0) "TILT UP" else "TILT DOWN"
            }
        }
        
        val intensity = calculateIntensity(azimuthDiff, elevationDiff)
        val suggestion = generateSuggestion(azimuthDiff, elevationDiff)
        val confidence = calculateConfidence(azimuthDiff, elevationDiff)
        
        return AlignmentGuidance(direction, intensity, suggestion, confidence)
    }
    
    /**
     * Calculate alignment metrics for performance tracking
     */
    fun calculateAlignmentMetrics(
        currentAzimuth: Double,
        targetAzimuth: Double,
        currentElevation: Double,
        targetElevation: Double
    ): AlignmentMetrics {
        val azimuthDiff = abs(normalizeAngleDifference(currentAzimuth, targetAzimuth))
        val elevationDiff = abs(targetElevation - currentElevation)
        
        val azimuthAccuracy = (1 - (azimuthDiff / 180.0)).toFloat().coerceIn(0f, 1f)
        val elevationAccuracy = (1 - (elevationDiff / 90.0)).toFloat().coerceIn(0f, 1f)
        val overallScore = (azimuthAccuracy * 0.6f + elevationAccuracy * 0.4f) * 100
        
        val estimatedTime = ((azimuthDiff + elevationDiff) * 2).toInt()
        
        return AlignmentMetrics(
            azimuthAccuracy * 100,
            elevationAccuracy * 100,
            overallScore,
            estimatedTime
        )
    }
    
    /**
     * Predict signal strength based on alignment
     */
    fun predictSignalStrength(
        elevation: Double,
        azimuthAccuracy: Float,
        elevationAccuracy: Float,
        weather: WeatherCondition = WeatherCondition.CLEAR
    ): Int {
        val baseSignal = SatelliteCalculator.calculateSignalQuality(elevation)
        val alignmentFactor = (azimuthAccuracy + elevationAccuracy) / 200f
        val weatherFactor = weather.factor
        
        return (baseSignal * alignmentFactor * weatherFactor).toInt().coerceIn(0, 100)
    }
    
    /**
     * Detect obstacles based on signal degradation patterns
     */
    fun detectObstacles(
        signalHistory: List<Int>,
        elevationHistory: List<Double>
    ): ObstacleDetection {
        if (signalHistory.size < 5) {
            return ObstacleDetection(false, "Insufficient data", 0f)
        }
        
        val signalVariance = calculateVariance(signalHistory.map { it.toDouble() })
        val hasObstacle = signalVariance > 100 && signalHistory.last() < 50
        
        return ObstacleDetection(
            hasObstacle,
            if (hasObstacle) "Possible obstacle detected" else "Clear line of sight",
            signalVariance.toFloat()
        )
    }
    
    private fun normalizeAngleDifference(current: Double, target: Double): Double {
        var diff = target - current
        while (diff > 180) diff -= 360
        while (diff < -180) diff += 360
        return diff
    }
    
    private fun calculateIntensity(azimuthDiff: Double, elevationDiff: Double): Float {
        val totalDiff = sqrt(azimuthDiff.pow(2) + elevationDiff.pow(2))
        return (totalDiff / 10).toFloat().coerceIn(0f, 10f)
    }
    
    private fun generateSuggestion(azimuthDiff: Double, elevationDiff: Double): String {
        return when {
            abs(azimuthDiff) < 2 && abs(elevationDiff) < 2 -> 
                "Perfect! Save this alignment."
            abs(azimuthDiff) < 5 && abs(elevationDiff) < 5 -> 
                "Almost there! Fine-tune slowly."
            abs(azimuthDiff) > 30 || abs(elevationDiff) > 20 -> 
                "Large adjustment needed. Move steadily."
            else -> 
                "Keep adjusting. You're getting closer."
        }
    }
    
    private fun calculateConfidence(azimuthDiff: Double, elevationDiff: Double): Float {
        val totalError = abs(azimuthDiff) + abs(elevationDiff)
        return (1 - (totalError / 100)).toFloat().coerceIn(0f, 1f)
    }
    
    private fun calculateVariance(values: List<Double>): Double {
        val mean = values.average()
        return values.map { (it - mean).pow(2) }.average()
    }
    
    enum class WeatherCondition(val factor: Float) {
        CLEAR(1.0f),
        CLOUDY(0.9f),
        RAINY(0.7f),
        STORMY(0.5f)
    }
    
    data class ObstacleDetection(
        val detected: Boolean,
        val message: String,
        val confidence: Float
    )
}
