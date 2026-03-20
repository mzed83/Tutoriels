plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.l42project.djimini4"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.l42project.djimini4"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Architecture 64 bits uniquement (requis par DJI MSDK v5)
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Necessaire pour les fichiers .so du SDK DJI
    packaging {
        jniLibs {
            pickFirsts += listOf("**/*.so")
        }
    }

    viewBinding {
        enable = true
    }
}

dependencies {
    // Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ============================================
    // DJI Mobile SDK v5 (MSDK v5)
    // ============================================
    // SDK principal
    implementation("com.dji:dji-sdk-v5-aircraft:5.9.0")
    compileOnly("com.dji:dji-sdk-v5-aircraft-provided:5.9.0")

    // Necessite le network SDK pour la connexion
    implementation("com.dji:dji-sdk-v5-networkImp:5.9.0")
}
