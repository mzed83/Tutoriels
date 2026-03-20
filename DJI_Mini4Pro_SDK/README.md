# DJI Mini 4 Pro - Programme Simple avec MSDK v5

## Description
Application Android simple (Kotlin) qui se connecte au DJI Mini 4 Pro
via le DJI Mobile SDK v5, affiche la telemetrie en temps reel
(batterie, GPS, altitude) et permet de controler la camera.

## Pre-requis
- Android Studio Hedgehog (2023.1) ou plus recent
- JDK 17
- Un compte developpeur DJI (https://developer.dji.com/)
- Une cle API DJI (creee sur https://developer.dji.com/user/apps)
- Un telephone Android compatible (Android 7.0+, 64 bits)
- Un DJI Mini 4 Pro avec sa telecommande RC 2 ou RC-N2

## Structure du projet
```
app/
  src/main/
    java/com/l42project/djimini4/
      DJIMini4Application.kt   -- Classe Application (init SDK)
      MainActivity.kt          -- Activite principale (telemetrie + camera)
    res/layout/
      activity_main.xml        -- Interface utilisateur
    AndroidManifest.xml        -- Permissions et configuration
  build.gradle.kts             -- Dependencies MSDK v5
```

## Installation
1. Cloner ce depot
2. Ouvrir le dossier `DJI_Mini4Pro_SDK/` dans Android Studio
3. Remplacer `YOUR_DJI_API_KEY` dans `AndroidManifest.xml` par votre cle API
4. Connecter votre telephone a la telecommande du Mini 4 Pro (USB)
5. Compiler et lancer l'application

## APIs utilisees
- **SDKManager**: Initialisation et enregistrement du SDK
- **KeyBatteryChargeRemaining**: Niveau de batterie en temps reel
- **KeyAircraftLocation3D**: Position GPS et altitude
- **KeyConnection**: Etat de connexion au drone
- **CameraKey / ShootPhoto**: Declenchement de photo
- **CameraKey / StartRecordVideo**: Demarrage/arret de la video
- **VirtualStickManager**: Controle basique du vol (optionnel)

## Notes importantes
- Le DJI Mini 4 Pro est supporte a partir du MSDK v5.7.0+
- Le SDK ne fonctionne PAS sur emulateur, un appareil physique est obligatoire
- La telecommande doit etre connectee au telephone par USB (ou WiFi pour RC 2)
- Les fonctions de vol automatique (waypoints) necessitent une licence FlightHub 2
