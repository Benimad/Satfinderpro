package com.example.satfinderpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satfinderpro.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Satellite orbit animation
    val orbitProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )
    
    // Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Radar sweep animation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    
    // Fade in animation for text
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textFade"
    )
    
    // Launch effect to navigate after delay
    LaunchedEffect(Unit) {
        delay(3500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DarkScannerSurface,
                        DarkScannerBackground,
                        Color(0xFF050810)
                    ),
                    center = Offset(0.5f, 0.4f),
                    radius = 1.5f
                )
            )
    ) {
        // Animated background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40f
            val alpha = 0.1f
            
            // Draw horizontal lines
            for (i in 0 until (size.height / gridSpacing).toInt()) {
                drawLine(
                    color = ScannerGrid.copy(alpha = alpha),
                    start = Offset(0f, i * gridSpacing),
                    end = Offset(size.width, i * gridSpacing),
                    strokeWidth = 1f
                )
            }
            
            // Draw vertical lines
            for (i in 0 until (size.width / gridSpacing).toInt()) {
                drawLine(
                    color = ScannerGrid.copy(alpha = alpha),
                    start = Offset(i * gridSpacing, 0f),
                    end = Offset(i * gridSpacing, size.height),
                    strokeWidth = 1f
                )
            }
        }

        // Central animated satellite
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Outer rotating ring
            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .alpha(0.3f)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.width / 2 - 20
                
                drawCircle(
                    color = ScannerGrid.copy(alpha = 0.4f),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
                
                // Orbital satellites
                val satelliteCount = 3
                for (i in 0 until satelliteCount) {
                    val angle = orbitProgress + (i * 120f)
                    rotate(angle, Offset(centerX, centerY)) {
                        drawCircle(
                            color = Primary,
                            radius = 8f,
                            center = Offset(centerX, centerY - radius)
                        )
                    }
                }
            }
            
            // Radar sweep
            Canvas(
                modifier = Modifier.size(220.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                
                rotate(sweepAngle, Offset(centerX, centerY)) {
                    drawArc(
                        color = ScannerSweep.copy(alpha = 0.4f),
                        startAngle = 0f,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                }
            }
            
            // Central satellite icon with pulse
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.3f),
                                PrimaryDark.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Inner ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    
                    drawCircle(
                        color = Primary,
                        radius = 50f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3f)
                    )
                }
                
                // Satellite icon
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Satellite",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }
            
            // Signal waves
            Canvas(
                modifier = Modifier.size(350.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                
                repeat(3) { index ->
                    val animatedRadius = 60f + (index * 40f) + (pulseScale - 1f) * 50f
                    drawCircle(
                        color = ScannerSweep.copy(alpha = 0.2f - index * 0.05f),
                        radius = animatedRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        // Bottom text content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SatFinderPro",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Professional Satellite Alignment System",
                fontSize = 14.sp,
                color = ScannerTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading indicator
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = Primary,
                    strokeWidth = 3.dp,
                    trackColor = ScannerTextMuted.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Initializing...",
                fontSize = 12.sp,
                color = ScannerTextMuted,
                modifier = Modifier.alpha(textAlpha)
            )
        }
        
        // Version text
        Text(
            text = "v2.0 PRO",
            fontSize = 10.sp,
            color = ScannerTextMuted.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
