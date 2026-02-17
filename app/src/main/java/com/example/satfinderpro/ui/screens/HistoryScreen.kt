package com.example.satfinderpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satfinderpro.data.model.Alignment as AlignmentModel
import com.example.satfinderpro.ui.theme.*
import com.example.satfinderpro.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val alignments by viewModel.alignments.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAlignment by remember { mutableStateOf<AlignmentModel?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Alignment History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${alignments.size} saved alignments",
                            fontSize = 12.sp,
                            color = ScannerTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (alignments.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = Error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkScannerBackground
                )
            )
        },
        containerColor = DarkScannerBackground
    ) { paddingValues ->
        if (alignments.isEmpty()) {
            EmptyHistoryState(onNavigateBack)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = alignments.sortedByDescending { it.timestamp },
                    key = { it.id }
                ) { alignment ->
                    ProfessionalHistoryItem(
                        alignment = alignment,
                        onDelete = {
                            selectedAlignment = alignment
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Delete Single Dialog
    if (showDeleteDialog && selectedAlignment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DarkScannerSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Alignment?",
                    fontWeight = FontWeight.Bold,
                    color = ScannerText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the alignment for ${selectedAlignment?.satelliteName}? This action cannot be undone.",
                    color = ScannerTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedAlignment?.let { viewModel.deleteAlignment(it) }
                        showDeleteDialog = false
                        selectedAlignment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ScannerText),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = DarkScannerSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Clear All History?",
                    fontWeight = FontWeight.Bold,
                    color = ScannerText
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all ${alignments.size} saved alignments. This action cannot be undone.",
                    color = ScannerTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        alignments.forEach { viewModel.deleteAlignment(it) }
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearAllDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ScannerText),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfessionalHistoryItem(
    alignment: AlignmentModel,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(alignment.timestamp))
    
    val signalColor = when {
        alignment.signalQuality >= 80 -> SignalExcellent
        alignment.signalQuality >= 60 -> SignalGood
        alignment.signalQuality >= 40 -> SignalFair
        else -> SignalPoor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = DarkScannerCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with satellite name and signal quality
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Primary.copy(alpha = 0.8f), PrimaryDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = alignment.satelliteName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScannerText
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = ScannerTextMuted
                        )
                    }
                }
                
                // Signal Quality Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = signalColor.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (alignment.signalQuality >= 60) 
                                Icons.Default.Info else 
                                Icons.Default.Warning,
                            contentDescription = null,
                            tint = signalColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${alignment.signalQuality}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = signalColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = ScannerTextMuted.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Alignment Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryDetailItem(
                    icon = Icons.Default.Info,
                    label = "Azimuth",
                    value = "${String.format(Locale.getDefault(), "%.1f", alignment.azimuth)}°",
                    color = Primary
                )
                HistoryDetailItem(
                    icon = Icons.Default.Info,
                    label = "Elevation",
                    value = "${String.format(Locale.getDefault(), "%.1f", alignment.elevation)}°",
                    color = Secondary
                )
                HistoryDetailItem(
                    icon = Icons.Default.Build,
                    label = "Polarization",
                    value = "${String.format(Locale.getDefault(), "%.1f", alignment.polarization)}°",
                    color = Info
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = ScannerTextMuted.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ScannerTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.4f", alignment.latitude)}, ${String.format(Locale.getDefault(), "%.4f", alignment.longitude)}",
                        fontSize = 12.sp,
                        color = ScannerTextMuted
                    )
                }
                
                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Error.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = ScannerTextMuted,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = ScannerText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyHistoryState(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ScannerTextMuted.copy(alpha = 0.3f),
                                ScannerTextMuted.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = ScannerTextMuted
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "No Alignments Yet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ScannerText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Successful satellite alignments will appear here. Start by finding and aligning to a satellite.",
                fontSize = 14.sp,
                color = ScannerTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = PrimaryGradient
                        )
                    )
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "START ALIGNING",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
