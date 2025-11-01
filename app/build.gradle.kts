plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Compose deshabilitado - app usa Views XML
    // alias(libs.plugins.kotlin.compose)
    // Firebase deshabilitado - descomentar cuando se configure google-services.json
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.example.medirecord4"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.medirecord4"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Habilitar minify para reducir tamaño del APK
            isMinifyEnabled = true
            // Reducir recursos no usados
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
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
        // Compose deshabilitado - app usa Views XML
        compose = false
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Retrofit para APIs REST (usado en BuscarMedicamentoOnlineActivity)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Gson para JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines para operaciones asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Firebase para notificaciones (listo para cuando se configure)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Location para farmacias (solo location, no maps SDK)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // WorkManager para notificaciones programadas
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}