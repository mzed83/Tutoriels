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
- Un DJI Mini 4 Pro avec la telecommande **RC-N2 ou RC-N3** (sans ecran integre)
- **ATTENTION**: La telecommande RC 2 (avec ecran integre) n'est PAS compatible avec le MSDK

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
- Le DJI Mini 4 Pro est supporte a partir du MSDK v5 (version actuelle: 5.17.0)
- Le SDK ne fonctionne PAS sur emulateur, un appareil physique est obligatoire
- La telecommande RC-N2/RC-N3 doit etre connectee au telephone par cable USB
- La RC 2 (avec ecran integre) ne supporte PAS le MSDK
- Le MSDK v5 est Android uniquement (pas d'iOS)
- Le Mini 4 Pro supporte les waypoints embarques (mission executee sur le drone)
- Il n'existe PAS de SDK Python pour le Mini 4 Pro (seul le DJI Tello a un SDK Python)

## Ressources
- Documentation officielle: https://developer.dji.com/doc/mobile-sdk-tutorial/en/
- Code source sample DJI: https://github.com/dji-sdk/Mobile-SDK-Android-V5
- API Reference: https://developer.dji.com/api-reference-v5/android-api/
