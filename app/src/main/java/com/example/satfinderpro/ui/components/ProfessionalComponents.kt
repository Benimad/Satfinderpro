package com.example.satfinderpro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satfinderpro.ui.theme.*
import kotlin.math.*

@Composable
fun ProfessionalCompass(
    targetAzimuth: Float,
    currentAzimuth: Float,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier
            .size(280.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(SurfaceLight, BackgroundLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width / 2 - 40
            
            drawCircle(color = Primary, radius = radius + 10, style = Stroke(width = 3f))
            drawCircle(color = Color.White, radius = radius, style = Stroke(width = 2f))
            
            val directions = listOf("N", "E", "S", "W")
            directions.forEachIndexed { index, direction ->
                val angle = index * 90f - 90f
                val rad = Math.toRadians(angle.toDouble())
                val x = centerX + (radius + 25) * cos(rad).toFloat()
                val y = centerY + (radius + 25) * sin(rad).toFloat()
                
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        direction, x, y,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
            
            rotate(targetAzimuth) {
                drawLine(
                    color = if (isAligned) AlignedGreen else ScannerTarget,
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius + 20),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
            
            rotate(currentAzimuth) {
                val needlePath = Path().apply {
                    moveTo(centerX, centerY - radius + 40)
                    lineTo(centerX - 15, centerY + 30)
                    lineTo(centerX, centerY + 10)
                    lineTo(centerX + 15, centerY + 30)
                    close()
                }
                drawPath(path = needlePath, brush = Brush.linearGradient(colors = listOf(Primary, PrimaryDark)))
            }
            
            drawCircle(color = if (isAligned) AlignedGreen else Primary, radius = 15f, center = Offset(centerX, centerY))
            
            if (isAligned) {
                drawCircle(
                    color = AlignedGreen.copy(alpha = pulseAlpha),
                    radius = 30f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3f)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        ) {
            Text(
                text = "${currentAzimuth.toInt()}°",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAligned) AlignedGreen else TextPrimary
            )
            Text(text = "Target: ${targetAzimuth.toInt()}°", fontSize = 14.sp, color = TextSecondary)
        }
    }
}

@Composable
fun ScannerSignalIndicator(
    signalQuality: Int,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "SIGNAL STRENGTH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "$signalQuality%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = getSignalColor(signalQuality))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / 20
                    val spacing = 4f
                    
                    for (i in 0 until 20) {
                        val x = i * (barWidth + spacing)
                        val barHeight = size.height * (i + 1) / 20
                        val isActive = (i + 1) * 5 <= signalQuality
                        
                        drawRect(
                            color = if (isActive) getSignalColor(signalQuality) else Color.Gray.copy(alpha = 0.3f),
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                    
                    if (isScanning) {
                        val scanX = size.width * scanProgress
                        drawLine(
                            color = ScannerGrid.copy(alpha = 0.7f),
                            start = Offset(scanX, 0f),
                            end = Offset(scanX, size.height),
                            strokeWidth = 3f
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = getSignalQualityText(signalQuality), fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ElevationIndicator(
    currentElevation: Float,
    targetElevation: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.width / 2 - 10
                
                drawArc(
                    color = Color.LightGray,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 15f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                drawArc(
                    color = Secondary.copy(alpha = 0.5f),
                    startAngle = 180f,
                    sweepAngle = (targetElevation / 90f) * 180f,
                    useCenter = false,
                    style = Stroke(width = 15f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                drawArc(
                    color = Primary,
                    startAngle = 180f,
                    sweepAngle = (currentElevation / 90f) * 180f,
                    useCenter = false,
                    style = Stroke(width = 15f, cap = StrokeCap.Round),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = "ELEVATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(text = "${currentElevation.toInt()}°", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text(text = "Target: ${targetElevation.toInt()}°", fontSize = 14.sp, color = TextSecondary)
                Text(
                    text = "Diff: ${abs(targetElevation - currentElevation).toInt()}°",
                    fontSize = 12.sp,
                    color = if (abs(targetElevation - currentElevation) < 2) AlignedGreen else MisalignedRed
                )
            }
        }
    }
}

@Composable
fun ARAlignmentOverlay(
    isAligned: Boolean,
    guidance: String,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ar")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (isAligned) AlignedGreen.copy(alpha = 0.2f) else MisalignedRed.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 30f
            for (i in 0 until (size.width / gridSpacing).toInt()) {
                drawLine(
                    color = ScannerGrid.copy(alpha = 0.2f),
                    start = Offset(i * gridSpacing, 0f),
                    end = Offset(i * gridSpacing, size.height),
                    strokeWidth = 1f
                )
            }
            
            val scanY = size.height * scanLineY
            drawLine(
                color = if (isAligned) AlignedGreen else ScannerGrid,
                start = Offset(0f, scanY),
                end = Offset(size.width, scanY),
                strokeWidth = 2f
            )
            
            val bracketSize = 40f
            val corners = listOf(
                Offset(20f, 20f),
                Offset(size.width - 20f, 20f),
                Offset(20f, size.height - 20f),
                Offset(size.width - 20f, size.height - 20f)
            )
            
            corners.forEach { corner ->
                drawLine(
                    color = if (isAligned) AlignedGreen else Primary,
                    start = corner,
                    end = Offset(corner.x + bracketSize, corner.y),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = if (isAligned) AlignedGreen else Primary,
                    start = corner,
                    end = Offset(corner.x, corner.y + bracketSize),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isAligned) "🎯 LOCKED" else guidance,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAligned) AlignedGreen else Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { confidence },
                modifier = Modifier.width(200.dp),
                color = if (isAligned) AlignedGreen else Primary
            )
            Text(text = "Confidence: ${(confidence * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
        }
    }
}

private fun getSignalColor(quality: Int): Color {
    return when {
        quality >= 80 -> SignalExcellent
        quality >= 60 -> SignalGood
        quality >= 40 -> SignalFair
        quality >= 20 -> SignalPoor
        else -> SignalNone
    }
}

private fun getSignalQualityText(quality: Int): String {
    return when {
        quality >= 80 -> "EXCELLENT - Perfect alignment"
        quality >= 60 -> "GOOD - Strong signal"
        quality >= 40 -> "FAIR - Acceptable signal"
        quality >= 20 -> "POOR - Weak signal"
        else -> "NO SIGNAL - Adjust antenna"
    }
}
