plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
<<<<<<< HEAD
    namespace = "com.shoulderguard"
=======
    namespace = "com.thambithappu"
>>>>>>> 8c5c67d (Proper working demo has been made)
    compileSdk {
        version = release(37)
    }

    defaultConfig {
<<<<<<< HEAD
        applicationId = "com.shoulderguard"
=======
        applicationId = "com.thambithappu"
>>>>>>> 8c5c67d (Proper working demo has been made)
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
<<<<<<< HEAD
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    // camera-core 1.3+ is required for ImageProxy.toBitmap(), used for the lazy face-crop
    // handoff to whoever ends up owning face identification.

    // ML Kit Face Detection - fully on-device, no network call, satisfies the privacy constraint
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Jetpack Compose camera permission handling uses AndroidX Activity's result API directly
    // (already used in CameraScreen.kt above) - no extra permissions library dependency needed.
=======
    implementation("androidx.camera:camera-core:1.6.1")
    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    implementation("com.google.mlkit:face-detection:16.1.7")

>>>>>>> 8c5c67d (Proper working demo has been made)
}