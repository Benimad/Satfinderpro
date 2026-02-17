package com.example.satfinderpro.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

class CompassManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var azimuth: Float = 0f
        private set
    var pitch: Float = 0f
        private set
    var roll: Float = 0f
        private set

    var onCompassChanged: ((azimuth: Float) -> Unit)? = null

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
            }
        }

        SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accelerometerReading, magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        // Normalize azimuth to 0-360
        if (azimuth < 0) {
            azimuth += 360f
        }

        onCompassChanged?.invoke(azimuth)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}

class AccelerometerManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var elevation: Float = 0f
        private set

    var onElevationChanged: ((elevation: Float) -> Unit)? = null

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate elevation angle from accelerometer
            // Assuming the device is held vertically
            val magnitude = sqrt(x * x + y * y + z * z)
            elevation = Math.toDegrees(
                atan2(z.toDouble(), sqrt((x * x + y * y).toDouble())).toDouble()
            ).toFloat()

            // Normalize elevation to 0-90 degrees
            elevation = if (elevation < 0) elevation + 90f else elevation

            onElevationChanged?.invoke(elevation)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}
