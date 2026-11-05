plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.WalterMusa.qrscanner"
    compileSdk {
        version = release(37)
        buildFeatures {
            viewBinding = true
        }
    }

    defaultConfig {
        applicationId = "com.WalterMusa.qrscanner"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // CameraX
    implementation("androidx.camera:camera-core:1.6.2")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

// ML Kit barcode scanning (bundled model, works offline)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation(libs.camera.view)
}