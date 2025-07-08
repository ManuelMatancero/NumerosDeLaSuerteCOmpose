plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.matancita.loteria"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.matancita.loteria"
        minSdk = 24
        targetSdk = 35
        versionCode = 28
        versionName = "8.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended) // Para más iconos

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Lifecycle utilities for collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.play.services.ads)

    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.cardview:cardview:1.0.0")


    // Retrofit para networking
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Moshi para p(arsear JSON
    implementation ("com.squareup.moshi:moshi-kotlin:1.15.0")

    // BoM de Firebase (Bill of Materials) para manejar versiones
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    // Dependencia específica para ML Kit On-Device Translation
    implementation("com.google.mlkit:translate:17.0.2")
}