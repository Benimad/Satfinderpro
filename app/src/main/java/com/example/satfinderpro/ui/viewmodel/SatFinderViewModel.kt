package com.example.satfinderpro.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.satfinderpro.data.model.Alignment
import com.example.satfinderpro.data.model.LocationData
import com.example.satfinderpro.data.model.Satellite
import com.example.satfinderpro.data.model.SatellitePosition
import com.example.satfinderpro.data.repository.AlignmentRepository
import com.example.satfinderpro.utils.SatelliteCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class SatFinderViewModel(private val alignmentRepository: AlignmentRepository) : ViewModel() {
    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    val satellites: StateFlow<List<Satellite>> = _satellites

    private val _selectedSatellite = MutableStateFlow<Satellite?>(null)
    val selectedSatellite: StateFlow<Satellite?> = _selectedSatellite

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation

    private val _satellitePosition = MutableStateFlow<SatellitePosition?>(null)
    val satellitePosition: StateFlow<SatellitePosition?> = _satellitePosition

    private val _signalQuality = MutableStateFlow(0)
    val signalQuality: StateFlow<Int> = _signalQuality

    private val _isAligned = MutableStateFlow(false)
    val isAligned: StateFlow<Boolean> = _isAligned

    private val _currentAzimuth = MutableStateFlow(0f)
    val currentAzimuth: StateFlow<Float> = _currentAzimuth

    private val _currentElevation = MutableStateFlow(0f)
    val currentElevation: StateFlow<Float> = _currentElevation

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    private var fusedLocationClient: FusedLocationProviderClient? = null

    init {
        initializeSatellites()
    }

    private fun initializeSatellites() {
        _satellites.value = listOf(
            Satellite("Nilesat", -7.0),
            Satellite("Astra", 19.2),
            Satellite("Eutelsat", 7.0)
        )
    }

    fun selectSatellite(satellite: Satellite) {
        _selectedSatellite.value = satellite
        calculateSatellitePosition()
    }

    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun requestLocation(context: Context) {
        viewModelScope.launch {
            try {
                if (fusedLocationClient == null) {
                    initializeLocationClient(context)
                }
                
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        _currentLocation.value = LocationData(location.latitude, location.longitude)
                        _locationError.value = null
                        calculateSatellitePosition()
                    } else {
                        _locationError.value = "Unable to get location"
                    }
                }?.addOnFailureListener {
                    _locationError.value = it.message ?: "Location request failed"
                }
            } catch (e: Exception) {
                _locationError.value = e.message ?: "Location error"
            }
        }
    }

    private fun calculateSatellitePosition() {
        val location = _currentLocation.value
        val satellite = _selectedSatellite.value

        if (location != null && satellite != null) {
            val position = SatelliteCalculator.calculateSatellitePosition(
                location.latitude,
                location.longitude,
                satellite.longitude
            )
            _satellitePosition.value = position
            _signalQuality.value = SatelliteCalculator.calculateSignalQuality(position.elevation)
            checkAlignment()
        }
    }

    fun updateCompassReading(azimuth: Float) {
        _currentAzimuth.value = azimuth
        checkAlignment()
    }

    fun updateElevationReading(elevation: Float) {
        _currentElevation.value = elevation
        checkAlignment()
    }

    private fun checkAlignment() {
        val position = _satellitePosition.value
        if (position != null) {
            _isAligned.value = SatelliteCalculator.isAligned(
                _currentAzimuth.value.toDouble(),
                position.azimuth,
                _currentElevation.value.toDouble(),
                position.elevation
            )
        }
    }

    fun saveAlignment() {
        val location = _currentLocation.value
        val satellite = _selectedSatellite.value
        val position = _satellitePosition.value

        if (location != null && satellite != null && position != null) {
            viewModelScope.launch {
                val alignment = Alignment(
                    satelliteName = satellite.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    azimuth = position.azimuth,
                    elevation = position.elevation,
                    polarization = position.polarization,
                    signalQuality = _signalQuality.value,
                    timestamp = System.currentTimeMillis()
                )
                alignmentRepository.saveAlignment(alignment)
            }
        }
    }
}
