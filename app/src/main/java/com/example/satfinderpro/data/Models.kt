package com.example.satfinderpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String
)

@Entity(tableName = "alignments")
data class Alignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val satelliteName: String,
    val latitude: Double,
    val longitude: Double,
    val azimuth: Double,
    val elevation: Double,
    val polarization: Double,
    val signalQuality: Int,
    val timestamp: Long
)

data class Satellite(
    val name: String,
    val longitude: Double // Positive for East, Negative for West
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

data class SatellitePosition(
    val azimuth: Double,
    val elevation: Double,
    val polarization: Double
)
