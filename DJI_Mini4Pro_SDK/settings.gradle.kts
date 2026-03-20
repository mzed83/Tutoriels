pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
        // Repository DJI SDK
        maven { url = uri("https://terra-1-g.djicdn.com/repo/") }
    }
}

rootProject.name = "DJIMini4Pro"
include(":app")
