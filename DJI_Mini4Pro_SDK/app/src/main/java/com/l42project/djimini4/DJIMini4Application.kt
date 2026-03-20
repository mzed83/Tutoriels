package com.l42project.djimini4

import android.app.Application
import android.content.Context
import android.util.Log
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback

/**
 * Classe Application: point d'entree pour l'initialisation du DJI MSDK v5.
 *
 * Le SDK DJI doit etre initialise le plus tot possible dans le cycle de vie
 * de l'application. La classe Application est donc l'endroit ideal.
 */
class DJIMini4Application : Application() {

    companion object {
        private const val TAG = "DJIMini4App"
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Initialisation du module MSDK v5
        // Cette methode charge les librairies natives (.so) du SDK
        com.secneo.sdk.Helper.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        initSDK()
    }

    /**
     * Initialise le SDK DJI et enregistre l'application.
     *
     * Le processus d'enregistrement:
     * 1. init() - charge le SDK
     * 2. registerApp() - valide la cle API aupres des serveurs DJI
     * 3. Callback informe du succes/echec
     */
    private fun initSDK() {
        SDKManager.getInstance().init(this, object : SDKManagerCallback {

            override fun onRegisterSuccess() {
                // La cle API est valide, le SDK est pret
                Log.i(TAG, "SDK DJI enregistre avec succes !")
            }

            override fun onRegisterFailure(error: IDJIError?) {
                // Verifiez votre cle API et la connexion Internet
                Log.e(TAG, "Echec enregistrement SDK: ${error?.description()}")
            }

            override fun onProductDisconnect(productId: Int) {
                Log.i(TAG, "Drone deconnecte (id=$productId)")
            }

            override fun onProductConnect(productId: Int) {
                Log.i(TAG, "Drone connecte (id=$productId)")
            }

            override fun onProductChanged(productId: Int) {
                Log.i(TAG, "Produit change (id=$productId)")
            }

            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
                Log.i(TAG, "Init SDK: event=$event, progress=$totalProcess%")
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                // Telechargement de la base de donnees des zones de vol (fly zones)
                Log.d(TAG, "DB fly zones: $current / $total")
            }
        })
    }
}
