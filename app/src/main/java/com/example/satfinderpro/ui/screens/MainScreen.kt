package com.example.satfinderpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satfinderpro.data.model.LocationData
import com.example.satfinderpro.data.model.Satellite
import com.example.satfinderpro.data.model.SatellitePosition
import com.example.satfinderpro.ui.components.*
import com.example.satfinderpro.ui.theme.*
import com.example.satfinderpro.ui.viewmodel.SatFinderViewModel
import com.example.satfinderpro.utils.ProfessionalSatelliteEngine
import com.example.satfinderpro.utils.SmartAlignmentAssistant
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SatFinderViewModel,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSatelliteDetails by remember { mutableStateOf(false) }

    val satellites by viewModel.satellites.collectAsState()
    val selectedSatellite by viewModel.selectedSatellite.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val satellitePosition by viewModel.satellitePosition.collectAsState()
    val signalQuality by viewModel.signalQuality.collectAsState()
    val isAligned by viewModel.isAligned.collectAsState()
    val currentAzimuth by viewModel.currentAzimuth.collectAsState()
    val currentElevation by viewModel.currentElevation.collectAsState()

    // Smart guidance
    val guidance = selectedSatellite?.let { sat ->
        satellitePosition?.let { pos ->
            SmartAlignmentAssistant.getAlignmentGuidance(
                currentAzimuth.toDouble(),
                pos.azimuth,
                currentElevation.toDouble(),
                pos.elevation
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SatFinderPro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Professional Satellite Aligner",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkScannerBackground,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkScannerSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("History", color = ScannerText) },
                            onClick = {
                                showMenu = false
                                onNavigateToHistory()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Primary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Satellite Info", color = ScannerText) },
                            onClick = {
                                showMenu = false
                                showSatelliteDetails = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Info)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", color = ScannerText) },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Error)
                            }
                        )
                    }
                }
            )
        },
        containerColor = DarkScannerBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Status Bar
            LocationStatusBar(
                location = currentLocation,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Professional Satellite Selector
            ProfessionalSatelliteSelector(
                satellites = satellites,
                selectedSatellite = selectedSatellite,
                onSatelliteSelected = { viewModel.selectSatellite(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Scanner Display
            if (selectedSatellite != null && satellitePosition != null) {
                // Professional Radar Scanner
                ProfessionalSatelliteScanner(
                    targetAzimuth = satellitePosition!!.azimuth.toFloat(),
                    currentAzimuth = currentAzimuth,
                    targetElevation = satellitePosition!!.elevation.toFloat(),
                    currentElevation = currentElevation,
                    signalQuality = signalQuality,
                    isAligned = isAligned,
                    satelliteName = selectedSatellite!!.name,
                    modifier = Modifier.size(320.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Signal Strength Meter
                ProfessionalSignalMeter(
                    signalQuality = signalQuality,
                    isScanning = !isAligned,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Elevation Gauge
                ProfessionalElevationGauge(
                    currentElevation = currentElevation,
                    targetElevation = satellitePosition!!.elevation.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Alignment Status
                ProfessionalAlignmentStatus(
                    isAligned = isAligned,
                    azimuthError = abs(satellitePosition!!.azimuth.toFloat() - currentAzimuth),
                    elevationError = abs(satellitePosition!!.elevation.toFloat() - currentElevation),
                    signalQuality = signalQuality,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Smart Guidance Panel
                guidance?.let {
                    ProfessionalGuidancePanel(
                        guidance = it.direction,
                        confidence = it.confidence,
                        suggestion = it.suggestion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Technical Details Card
                TechnicalDetailsCard(
                    satellitePosition = satellitePosition!!,
                    currentLocation = currentLocation,
                    selectedSatellite = selectedSatellite,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Alignment Button
                AnimatedVisibility(
                    visible = isAligned,
                    enter = fadeIn() + expandVertically()
                ) {
                    Button(
                        onClick = { viewModel.saveAlignment() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Save",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE ALIGNMENT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!isAligned) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Align",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ALIGN TO SAVE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Empty State
                EmptyScannerState()
            }
        }
    }

    // Satellite Details Dialog
    if (showSatelliteDetails && selectedSatellite != null && currentLocation != null) {
        SatelliteDetailsDialog(
            satellite = selectedSatellite!!,
            location = currentLocation!!,
            onDismiss = { showSatelliteDetails = false }
        )
    }
}

@Composable
fun LocationStatusBar(
    location: LocationData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (location != null) Success.copy(alpha = 0.2f) else Warning.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = if (location != null) Success else Warning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (location != null) {
                    "${String.format(Locale.getDefault(), "%.4f", location.latitude)}, ${String.format(Locale.getDefault(), "%.4f", location.longitude)}"
                } else {
                    "Acquiring location..."
                },
                fontSize = 14.sp,
                color = if (location != null) Color.White else Warning,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalSatelliteSelector(
    satellites: List<Satellite>,
    selectedSatellite: Satellite?,
    onSatelliteSelected: (Satellite) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkScannerSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SELECT SATELLITE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ScannerTextMuted
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSatellite?.let { "${it.name} (${if (it.longitude >= 0) "${it.longitude}°E" else "${-it.longitude}°W"})" } ?: "Choose a satellite...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkScannerCard,
                        unfocusedContainerColor = DarkScannerCard,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = ScannerTextMuted.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DarkScannerSurface)
                ) {
                    satellites.forEach { satellite ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = satellite.name,
                                        fontWeight = FontWeight.Bold,
                                        color = ScannerText
                                    )
                                    Text(
                                        text = "${if (satellite.longitude >= 0) "${satellite.longitude}° East" else "${-satellite.longitude}° West"}",
                                        fontSize = 12.sp,
                                        color = ScannerTextMuted
                                    )
                                }
                            },
                            onClick = {
                                onSatelliteSelected(satellite)
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Primary
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicalDetailsCard(
    satellitePosition: SatellitePosition,
    currentLocation: LocationData?,
    selectedSatellite: Satellite?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkScannerSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Technical",
                    tint = Info,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TECHNICAL DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Info
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TechDetailItem("AZIMUTH", "${String.format(Locale.getDefault(), "%.1f", satellitePosition.azimuth)}°")
                TechDetailItem("ELEVATION", "${String.format(Locale.getDefault(), "%.1f", satellitePosition.elevation)}°")
                TechDetailItem("POLARIZATION", "${String.format(Locale.getDefault(), "%.1f", satellitePosition.polarization)}°")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Professional satellite engine metrics
            val proPosition = currentLocation?.let { loc ->
                selectedSatellite?.let { sat ->
                    ProfessionalSatelliteEngine.calculateProfessionalPosition(
                        loc.latitude, loc.longitude, sat.longitude
                    )
                }
            }
            
            proPosition?.let { pos ->
                HorizontalDivider(color = ScannerTextMuted.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TechDetailItem("RANGE", "${(pos.slantRange).toInt()} km")
                    TechDetailItem("DELAY", "${pos.signalDelay.toInt()} ms")
                    TechDetailItem("FSPL", "${pos.freeSpacePathLoss.toInt()} dB")
                }
            }
        }
    }
}

@Composable
private fun TechDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = ScannerTextMuted,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = ScannerText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyScannerState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkScannerSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "No satellite",
                modifier = Modifier.size(80.dp),
                tint = ScannerTextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select a Satellite",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScannerText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose a satellite above to start alignment",
                fontSize = 14.sp,
                color = ScannerTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun SatelliteDetailsDialog(
    satellite: Satellite,
    location: LocationData,
    onDismiss: () -> Unit
) {
    val proPosition = remember(satellite, location) {
        ProfessionalSatelliteEngine.calculateProfessionalPosition(
            location.latitude, location.longitude, satellite.longitude
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkScannerSurface,
        title = {
            Text(
                text = satellite.name,
                fontWeight = FontWeight.Bold,
                color = ScannerText
            )
        },
        text = {
            Column {
                DetailRow("Longitude", "${satellite.longitude}° ${if (satellite.longitude >= 0) "East" else "West"}")
                DetailRow("Azimuth", "${String.format(Locale.getDefault(), "%.2f", proPosition.azimuth)}°")
                DetailRow("Elevation", "${String.format(Locale.getDefault(), "%.2f", proPosition.elevation)}°")
                DetailRow("Polarization", "${String.format(Locale.getDefault(), "%.2f", proPosition.polarization)}°")
                DetailRow("Slant Range", "${String.format(Locale.getDefault(), "%.0f", proPosition.slantRange)} km")
                DetailRow("Signal Delay", "${String.format(Locale.getDefault(), "%.1f", proPosition.signalDelay)} ms")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Optimal Alignment Time",
                            fontWeight = FontWeight.Bold,
                            color = Info,
                            fontSize = 14.sp
                        )
                        Text(
                            text = proPosition.optimalAlignmentTime.timeDescription,
                            color = ScannerText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = proPosition.optimalAlignmentTime.reason,
                            color = ScannerTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ScannerTextMuted, fontSize = 14.sp)
        Text(text = value, color = ScannerText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
