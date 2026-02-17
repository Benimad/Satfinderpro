package com.example.satfinderpro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.satfinderpro.data.SatFinderDatabase
import com.example.satfinderpro.data.repository.AlignmentRepository
import com.example.satfinderpro.data.repository.UserRepository
import com.example.satfinderpro.ui.navigation.AppNavigation
import com.example.satfinderpro.ui.theme.SatFinderProTheme
import com.example.satfinderpro.ui.viewmodel.AuthViewModel
import com.example.satfinderpro.ui.viewmodel.HistoryViewModel
import com.example.satfinderpro.ui.viewmodel.SatFinderViewModel
import com.example.satfinderpro.utils.AccelerometerManager
import com.example.satfinderpro.utils.CompassManager

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var satFinderViewModel: SatFinderViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var compassManager: CompassManager
    private lateinit var accelerometerManager: AccelerometerManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            satFinderViewModel.requestLocation(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repositories
        val database = SatFinderDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        val alignmentRepository = AlignmentRepository(database.alignmentDao())

        // Initialize ViewModels with factory
        val factory = com.example.satfinderpro.ui.viewmodel.ViewModelFactory(userRepository, alignmentRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        satFinderViewModel = ViewModelProvider(this, factory).get(SatFinderViewModel::class.java)
        historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        // Initialize sensors
        compassManager = CompassManager(this)
        accelerometerManager = AccelerometerManager(this)

        // Initialize location client
        satFinderViewModel.initializeLocationClient(this)

        setContent {
            SatFinderProTheme {
                val navController = rememberNavController()
                val currentUser by authViewModel.currentUser.collectAsState()
                val isLoggedIn = currentUser != null

                // Setup sensor listeners
                LaunchedEffect(Unit) {
                    compassManager.onCompassChanged = { azimuth ->
                        satFinderViewModel.updateCompassReading(azimuth)
                    }
                    accelerometerManager.onElevationChanged = { elevation ->
                        satFinderViewModel.updateElevationReading(elevation)
                    }
                    compassManager.startListening()
                    accelerometerManager.startListening()
                }

                DisposableEffect(Unit) {
                    onDispose {
                        compassManager.stopListening()
                        accelerometerManager.stopListening()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        satFinderViewModel = satFinderViewModel,
                        historyViewModel = historyViewModel,
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            satFinderViewModel.requestLocation(this)
        }
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermissions()
        compassManager.startListening()
        accelerometerManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        compassManager.stopListening()
        accelerometerManager.stopListening()
    }
}
