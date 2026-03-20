pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Repository officiel DJI SDK
        maven { url = uri("https://developer.dji.com/maven2") }
    }
}

rootProject.name = "DJIMini4Pro"
include(":app")
