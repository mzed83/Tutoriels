package com.l42project.djimini4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.l42project.djimini4.databinding.ActivityMainBinding
import dji.sdk.keyvalue.key.BatteryKey
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.camera.CameraMode
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.common.LocationCoordinate3D
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.KeyManager

/**
 * Activite principale - Telemetrie et controle camera du DJI Mini 4 Pro.
 *
 * Fonctionnalites:
 * - Affichage en temps reel: batterie, GPS (lat/lon), altitude
 * - Etat de connexion du drone
 * - Prise de photo
 * - Demarrage/arret de l'enregistrement video
 * - Flux video en direct (camera FPV)
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityMainBinding
    private var isRecording = false

    // =========================================================================
    // Permissions necessaires pour le SDK DJI
    // =========================================================================
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        setupUI()
        setupTelemetryListeners()
    }

    // =========================================================================
    // PERMISSIONS
    // =========================================================================

    private fun checkPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    // =========================================================================
    // INTERFACE UTILISATEUR
    // =========================================================================

    private fun setupUI() {
        // Bouton prise de photo
        binding.btnPhoto.setOnClickListener {
            takePhoto()
        }

        // Bouton enregistrement video (toggle start/stop)
        binding.btnVideo.setOnClickListener {
            if (isRecording) {
                stopRecordVideo()
            } else {
                startRecordVideo()
            }
        }

        // Bouton decollage
        binding.btnTakeoff.setOnClickListener {
            takeoff()
        }

        // Bouton atterrissage
        binding.btnLand.setOnClickListener {
            land()
        }
    }

    // =========================================================================
    // TELEMETRIE EN TEMPS REEL
    // Utilisation du systeme KeyManager de MSDK v5 avec des listeners
    // =========================================================================

    private fun setupTelemetryListeners() {

        // ------- BATTERIE -------
        // Ecoute du niveau de batterie (0-100%)
        val batteryKey = KeyTools.createKey(BatteryKey.KeyChargeRemaining)
        KeyManager.getInstance().listen(batteryKey, this) { _, newValue ->
            runOnUiThread {
                val percent = newValue ?: 0
                binding.tvBattery.text = "Batterie: $percent%"

                // Alerte si batterie faible
                if (percent < 20) {
                    binding.tvBattery.setTextColor(getColor(android.R.color.holo_red_dark))
                } else {
                    binding.tvBattery.setTextColor(getColor(android.R.color.white))
                }
            }
        }

        // ------- POSITION GPS + ALTITUDE -------
        // LocationCoordinate3D contient: latitude, longitude, altitude
        val locationKey = KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D)
        KeyManager.getInstance().listen(locationKey, this) { _, newValue ->
            runOnUiThread {
                if (newValue != null) {
                    val loc = newValue as LocationCoordinate3D
                    binding.tvLatitude.text = "Lat: %.6f".format(loc.latitude)
                    binding.tvLongitude.text = "Lon: %.6f".format(loc.longitude)
                    binding.tvAltitude.text = "Alt: %.1f m".format(loc.altitude)
                } else {
                    binding.tvLatitude.text = "Lat: --"
                    binding.tvLongitude.text = "Lon: --"
                    binding.tvAltitude.text = "Alt: -- m"
                }
            }
        }

        // ------- ETAT DE CONNEXION -------
        val connectedKey = KeyTools.createKey(FlightControllerKey.KeyConnection)
        KeyManager.getInstance().listen(connectedKey, this) { _, newValue ->
            runOnUiThread {
                val connected = newValue ?: false
                binding.tvStatus.text = if (connected) "Connecte" else "Deconnecte"
                binding.tvStatus.setTextColor(
                    getColor(
                        if (connected) android.R.color.holo_green_light
                        else android.R.color.holo_red_dark
                    )
                )
            }
        }

        // ------- VITESSE -------
        val velocityKey = KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity)
        KeyManager.getInstance().listen(velocityKey, this) { _, newValue ->
            runOnUiThread {
                if (newValue != null) {
                    // La vitesse est un Velocity3D (x, y, z en m/s)
                    val speed = Math.sqrt(
                        newValue.x * newValue.x +
                        newValue.y * newValue.y +
                        newValue.z * newValue.z
                    )
                    binding.tvSpeed.text = "Vitesse: %.1f m/s".format(speed)
                }
            }
        }

        // ------- NOMBRE DE SATELLITES GPS -------
        val gpsCountKey = KeyTools.createKey(FlightControllerKey.KeyGPSSatelliteCount)
        KeyManager.getInstance().listen(gpsCountKey, this) { _, newValue ->
            runOnUiThread {
                binding.tvSatellites.text = "Satellites: ${newValue ?: 0}"
            }
        }
    }

    // =========================================================================
    // CONTROLE CAMERA
    // =========================================================================

    /**
     * Prend une photo avec la camera du Mini 4 Pro.
     *
     * Etapes:
     * 1. Passer la camera en mode PHOTO
     * 2. Declencher la prise de photo
     */
    private fun takePhoto() {
        // Etape 1: Mode photo
        val cameraModeKey = KeyTools.createKey(
            CameraKey.KeyCameraMode,
            ComponentIndexType.LEFT_OR_MAIN
        )
        KeyManager.getInstance().setValue(
            cameraModeKey,
            CameraMode.PHOTO_NORMAL,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    // Etape 2: Declenchement
                    val shootKey = KeyTools.createKey(
                        CameraKey.KeyStartShootPhoto,
                        ComponentIndexType.LEFT_OR_MAIN
                    )
                    KeyManager.getInstance().performAction(
                        shootKey,
                        object : CommonCallbacks.CompletionCallbackWithParam<Any> {
                            override fun onSuccess(data: Any?) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Photo prise !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Log.i(TAG, "Photo prise avec succes")
                            }

                            override fun onFailure(error: IDJIError) {
                                Log.e(TAG, "Erreur photo: ${error.description()}")
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Erreur: ${error.description()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Erreur changement mode camera: ${error.description()}")
                }
            }
        )
    }

    /**
     * Demarre l'enregistrement video.
     */
    private fun startRecordVideo() {
        // Passer en mode video
        val cameraModeKey = KeyTools.createKey(
            CameraKey.KeyCameraMode,
            ComponentIndexType.LEFT_OR_MAIN
        )
        KeyManager.getInstance().setValue(
            cameraModeKey,
            CameraMode.VIDEO_NORMAL,
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    // Demarrer l'enregistrement
                    val recordKey = KeyTools.createKey(
                        CameraKey.KeyStartRecord,
                        ComponentIndexType.LEFT_OR_MAIN
                    )
                    KeyManager.getInstance().performAction(
                        recordKey,
                        object : CommonCallbacks.CompletionCallbackWithParam<Any> {
                            override fun onSuccess(data: Any?) {
                                isRecording = true
                                runOnUiThread {
                                    binding.btnVideo.text = "Stop Video"
                                    binding.btnVideo.setBackgroundColor(
                                        getColor(android.R.color.holo_red_dark)
                                    )
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Enregistrement demarre",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(error: IDJIError) {
                                Log.e(TAG, "Erreur demarrage video: ${error.description()}")
                            }
                        }
                    )
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Erreur mode video: ${error.description()}")
                }
            }
        )
    }

    /**
     * Arrete l'enregistrement video.
     */
    private fun stopRecordVideo() {
        val stopKey = KeyTools.createKey(
            CameraKey.KeyStopRecord,
            ComponentIndexType.LEFT_OR_MAIN
        )
        KeyManager.getInstance().performAction(
            stopKey,
            object : CommonCallbacks.CompletionCallbackWithParam<Any> {
                override fun onSuccess(data: Any?) {
                    isRecording = false
                    runOnUiThread {
                        binding.btnVideo.text = "Video"
                        binding.btnVideo.setBackgroundColor(
                            getColor(android.R.color.holo_blue_dark)
                        )
                        Toast.makeText(
                            this@MainActivity,
                            "Enregistrement arrete",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Erreur arret video: ${error.description()}")
                }
            }
        )
    }

    // =========================================================================
    // CONTROLE DE VOL
    // =========================================================================

    /**
     * Decollage automatique.
     * Le drone monte a environ 1.2m et se stabilise en vol stationnaire.
     */
    private fun takeoff() {
        val takeoffKey = KeyTools.createKey(FlightControllerKey.KeyStartTakeoff)
        KeyManager.getInstance().performAction(
            takeoffKey,
            object : CommonCallbacks.CompletionCallbackWithParam<Any> {
                override fun onSuccess(data: Any?) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Decollage !", Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "Decollage reussi")
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Erreur decollage: ${error.description()}")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Erreur decollage: ${error.description()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    /**
     * Atterrissage automatique.
     * Le drone descend lentement et se pose.
     */
    private fun land() {
        val landKey = KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding)
        KeyManager.getInstance().performAction(
            landKey,
            object : CommonCallbacks.CompletionCallbackWithParam<Any> {
                override fun onSuccess(data: Any?) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Atterrissage...", Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "Atterrissage demarre")
                }

                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "Erreur atterrissage: ${error.description()}")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Erreur: ${error.description()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    // =========================================================================
    // NETTOYAGE
    // =========================================================================

    override fun onDestroy() {
        super.onDestroy()
        // Supprime tous les listeners attaches a cette activite
        KeyManager.getInstance().cancelListen(this)
    }
}
