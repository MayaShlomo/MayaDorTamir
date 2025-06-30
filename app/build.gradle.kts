// build.gradle (Module: app) - תיקון
import java.util.Properties
import java.io.FileInputStream

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.navigation.safe.args)
    alias(libs.plugins.hilt.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.mycinema"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mycinema"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // קריאה מ-local.properties לבניית BuildConfig
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        val tmdbApiKey = localProperties.getProperty("TMDB_API_KEY") ?: "fce70c7f74fa363de535f143b1409243"
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")

        // Google Maps API Key
        val googleMapsApiKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: "YOUR_API_KEY_HERE"
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export directory - תיקון
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kapt {
        correctErrorTypes = true
        useBuildCache = false // תיקון לבעיית KAPT
    }

    // Packaging options לטיפול בקונפליקטים
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)

    // Jetpack Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    // Fragment & Activity KTX
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Image Loading
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Google Services
    implementation(libs.play.services.location)
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Work Manager (אם נדרש)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}