package com.example.satfinderpro.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satfinderpro.ui.theme.*
import kotlin.math.*

/**
 * Professional Satellite Scanner - Advanced Radar Component
 */
@Composable
fun ProfessionalSatelliteScanner(
    targetAzimuth: Float,
    currentAzimuth: Float,
    targetElevation: Float,
    currentElevation: Float,
    signalQuality: Int,
    isAligned: Boolean,
    modifier: Modifier = Modifier,
    satelliteName: String = "SATELLITE"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    
    // Scanning animation
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanAngle"
    )
    
    // Pulse animation when aligned
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkScannerBackground),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Radar Canvas
            Canvas(modifier = Modifier.fillMaxSize(0.95f)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = size.width / 2 - 20
                
                // Draw radar grid circles
                for (i in 1..4) {
                    drawCircle(
                        color = ScannerGrid.copy(alpha = 0.3f),
                        radius = maxRadius * i / 4,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1f)
                    )
                }
                
                // Draw crosshairs
                drawLine(
                    color = ScannerGrid.copy(alpha = 0.4f),
                    start = Offset(centerX - maxRadius, centerY),
                    end = Offset(centerX + maxRadius, centerY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = ScannerGrid.copy(alpha = 0.4f),
                    start = Offset(centerX, centerY - maxRadius),
                    end = Offset(centerX, centerY + maxRadius),
                    strokeWidth = 1f
                )
                
                // Draw NESW markers
                drawDirectionMarkers(centerX, centerY, maxRadius)
                
                // Draw scanning radar sweep
                rotate(scanAngle, Offset(centerX, centerY)) {
                    drawArc(
                        color = ScannerSweep.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = Offset(centerX - maxRadius, centerY - maxRadius),
                        size = Size(maxRadius * 2, maxRadius * 2)
                    )
                }
                
                // Draw target satellite position
                val targetRad = Math.toRadians((targetAzimuth - 90).toDouble())
                val targetRadius = maxRadius * 0.7f
                val targetX = centerX + targetRadius * cos(targetRad).toFloat()
                val targetY = centerY + targetRadius * sin(targetRad).toFloat()
                
                drawCircle(
                    color = if (isAligned) AlignedGreen else ScannerTarget,
                    radius = if (isAligned) 15f * pulseScale else 12f,
                    center = Offset(targetX, targetY),
                    alpha = if (isAligned) pulseAlpha else 1f
                )
                
                // Draw satellite label
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        satelliteName.take(8),
                        targetX,
                        targetY - 25f,
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24f
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
                
                // Draw current position needle
                rotate(currentAzimuth, Offset(centerX, centerY)) {
                    drawLine(
                        color = if (isAligned) AlignedGreen else Primary,
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, centerY - maxRadius * 0.85f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
                
                // Draw center hub
                drawCircle(
                    color = if (isAligned) AlignedGreen else Primary,
                    radius = 20f,
                    center = Offset(centerX, centerY)
                )
                
                // Draw alignment ring when aligned
                if (isAligned) {
                    drawCircle(
                        color = AlignedGreen.copy(alpha = pulseAlpha),
                        radius = maxRadius * 0.9f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3f)
                    )
                }
            }
            
            // Overlay information
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAligned) "🔒 LOCKED" else "📡 SCANNING",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) AlignedGreen else ScannerGrid
                )
                Text(
                    text = "Azimuth: ${currentAzimuth.toInt()}°/${targetAzimuth.toInt()}°",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun DrawScope.drawDirectionMarkers(centerX: Float, centerY: Float, radius: Float) {
    val directions = listOf("N", "E", "S", "W")
    directions.forEachIndexed { index, dir ->
        val angle = index * 90f - 90f
        val rad = Math.toRadians(angle.toDouble())
        val x = centerX + (radius + 15) * cos(rad).toFloat()
        val y = centerY + (radius + 15) * sin(rad).toFloat()
        
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                dir, x, y,
                Paint().apply {
                    color = android.graphics.Color.parseColor("#00FF00")
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
    }
}

/**
 * Professional Signal Strength Meter with Visual Bars
 */
@Composable
fun ProfessionalSignalMeter(
    signalQuality: Int,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "signal")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signalScan"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Signal",
                        tint = getSignalColor(signalQuality),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SIGNAL STRENGTH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Text(
                    text = "$signalQuality%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = getSignalColor(signalQuality)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Signal bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 10) {
                    val isActive = (i + 1) * 10 <= signalQuality
                    val barHeight = 24.dp + (i * 4).dp
                    
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(barHeight)
                            .background(
                                color = when {
                                    isActive -> getSignalColor(signalQuality)
                                    isScanning && i == (scanProgress * 10).toInt() -> 
                                        ScannerGrid.copy(alpha = 0.5f)
                                    else -> Color.Gray.copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Signal quality description
            Text(
                text = getSignalQualityDescription(signalQuality),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Professional Elevation Gauge with Arc Display
 */
@Composable
fun ProfessionalElevationGauge(
    currentElevation: Float,
    targetElevation: Float,
    modifier: Modifier = Modifier
) {
    val alignmentError = abs(targetElevation - currentElevation)
    val isAligned = alignmentError < 2f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arc gauge
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2 + 20
                val radius = size.width / 2 - 10
                
                // Background arc
                drawArc(
                    color = Color.Gray.copy(alpha = 0.3f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                // Target arc
                val targetSweep = (targetElevation / 90f) * 180f
                drawArc(
                    color = Secondary.copy(alpha = 0.5f),
                    startAngle = 180f,
                    sweepAngle = targetSweep,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                // Current position arc
                val currentSweep = (currentElevation / 90f) * 180f
                drawArc(
                    color = if (isAligned) AlignedGreen else Primary,
                    startAngle = 180f,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                // Draw markers
                listOf(0f, 30f, 60f, 90f).forEach { angle ->
                    val markerAngle = 180 + (angle / 90f) * 180f
                    val markerRad = Math.toRadians(markerAngle.toDouble())
                    val startRadius = radius - 5
                    val endRadius = radius + 5
                    
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(
                            centerX + startRadius * cos(markerRad).toFloat(),
                            centerY + startRadius * sin(markerRad).toFloat()
                        ),
                        end = Offset(
                            centerX + endRadius * cos(markerRad).toFloat(),
                            centerY + endRadius * sin(markerRad).toFloat()
                        ),
                        strokeWidth = 2f
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Elevation info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ELEVATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currentElevation.toInt()}°",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) AlignedGreen else Primary
                )
                Text(
                    text = "Target: ${targetElevation.toInt()}°",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Error indicator
                if (!isAligned) {
                    val direction = if (currentElevation < targetElevation) "↑ TILT UP" else "↓ TILT DOWN"
                    Text(
                        text = "$direction ${alignmentError.toInt()}°",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                } else {
                    Text(
                        text = "✓ PERFECT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlignedGreen
                    )
                }
            }
        }
    }
}

/**
 * Professional Alignment Status Card
 */
@Composable
fun ProfessionalAlignmentStatus(
    isAligned: Boolean,
    azimuthError: Float,
    elevationError: Float,
    signalQuality: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statusPulse"
    )
    
    val cardColor = when {
        isAligned -> AlignedGreen.copy(alpha = 0.15f)
        signalQuality > 60 -> Warning.copy(alpha = 0.15f)
        else -> MisalignedRed.copy(alpha = 0.15f)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isAligned -> AlignedGreen.copy(alpha = pulseAlpha)
                            signalQuality > 60 -> Warning.copy(alpha = 0.5f)
                            else -> MisalignedRed.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isAligned -> Icons.Default.CheckCircle
                        signalQuality > 60 -> Icons.Default.AddCircle
                        else -> Icons.Default.Warning
                    },
                    contentDescription = "Status",
                    modifier = Modifier.size(48.dp),
                    tint = when {
                        isAligned -> AlignedGreen
                        signalQuality > 60 -> Warning
                        else -> MisalignedRed
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    isAligned -> "SATELLITE LOCKED"
                    signalQuality > 60 -> "NEAR ALIGNMENT"
                    else -> "ALIGNMENT NEEDED"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isAligned -> AlignedGreen
                    signalQuality > 60 -> Warning
                    else -> MisalignedRed
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!isAligned) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ErrorIndicator("Azimuth", azimuthError, Icons.Default.AddCircle)
                    ErrorIndicator("Elevation", elevationError, Icons.Default.AddCircle)
                }
            } else {
                Text(
                    text = "Signal Quality: $signalQuality%",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ErrorIndicator(label: String, error: Float, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = "${error.toInt()}°",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (error < 5) Warning else MisalignedRed
        )
    }
}

/**
 * Professional Guidance Panel with Smart Suggestions
 */
@Composable
fun ProfessionalGuidancePanel(
    guidance: String,
    confidence: Float,
    suggestion: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Guidance",
                    tint = Info,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SMART GUIDANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Info
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = guidance,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = suggestion,
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Confidence bar
            LinearProgressIndicator(
                progress = { confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    confidence > 0.8 -> Success
                    confidence > 0.5 -> Warning
                    else -> Error
                },
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            Text(
                text = "Confidence: ${(confidence * 100).toInt()}%",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Helper functions
private fun getSignalColor(quality: Int): Color {
    return when {
        quality >= 80 -> SignalExcellent
        quality >= 60 -> SignalGood
        quality >= 40 -> SignalFair
        quality >= 20 -> SignalPoor
        else -> SignalNone
    }
}

private fun getSignalQualityDescription(quality: Int): String {
    return when {
        quality >= 90 -> "EXCELLENT - Optimal reception guaranteed"
        quality >= 75 -> "GOOD - Strong stable signal"
        quality >= 50 -> "FAIR - Acceptable with minor adjustments"
        quality >= 25 -> "POOR - Frequent signal drops expected"
        else -> "NO SIGNAL - Realignment required"
    }
}
