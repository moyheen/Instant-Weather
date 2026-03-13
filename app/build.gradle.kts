import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mayokunadeniyi.instantweather"
    compileSdk = 34

    if (project.hasProperty("keystore.properties")) {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            signingConfigs {
                getByName("debug") {
                    keyAlias = keystoreProperties["keyAlias"]?.toString()
                    keyPassword = keystoreProperties["keyPassword"]?.toString()
                    storeFile = keystoreProperties["storeFile"]?.toString()?.let { file(rootDir.absolutePath + it) }
                    storePassword = keystoreProperties["storePassword"]?.toString()
                }
                create("release") {
                    keyAlias = keystoreProperties["keyAlias"]?.toString()
                    keyPassword = keystoreProperties["keyPassword"]?.toString()
                    storeFile = keystoreProperties["storeFile"]?.toString()?.let { file(rootDir.absolutePath + it) }
                    storePassword = keystoreProperties["storePassword"]?.toString()
                }
            }
        }
    }

    defaultConfig {
        applicationId = "com.mayokunadeniyi.instantweather"
        minSdk = 24
        targetSdk = 34
        versionCode = 5
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        fun getValidProperty(key: String, default: String): String {
            val value = localProperties.getProperty(key)
            if (value.isNullOrBlank() || value == "\"\"" || value == "''") {
                return default
            }
            return if (!value.startsWith("\"")) "\"$value\"" else value
        }

        val API_KEY = getValidProperty("API_KEY", "\"YOUR_API_KEY\"")
        val ALGOLIA_API_KEY = getValidProperty("ALGOLIA_API_KEY", "\"YOUR_ALGOLIA_KEY\"")
        val ALGOLIA_APP_ID = getValidProperty("ALGOLIA_APP_ID", "\"YOUR_ALGOLIA_APP_ID\"")
        val ALGOLIA_INDEX_NAME = getValidProperty("ALGOLIA_INDEX_NAME", "\"YOUR_INDEX\"")

        buildConfigField("String", "API_KEY", API_KEY)
        buildConfigField("String", "ALGOLIA_API_KEY", ALGOLIA_API_KEY)
        buildConfigField("String", "ALGOLIA_APP_ID", ALGOLIA_APP_ID)
        buildConfigField("String", "ALGOLIA_INDEX_NAME", ALGOLIA_INDEX_NAME)
        buildConfigField("String", "BASE_URL", "\"http://api.openweathermap.org/\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (project.hasProperty("keystore.properties") && rootProject.file("keystore.properties").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isDebuggable = false
        }

        getByName("debug") {
            if (project.hasProperty("keystore.properties") && rootProject.file("keystore.properties").exists()) {
                signingConfig = signingConfigs.getByName("debug")
            }
            isDebuggable = true
        }
    }

    sourceSets {
        getByName("test").java.srcDir("src/sharedTest/java")
        getByName("androidTest").java.srcDir("src/sharedTest/java")
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests.apply {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes.add("**/attach_hotspot_windows.dll")
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
        resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.android)

    // AndroidX & Core
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.paging.runtime)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    // UI & Views
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Network & Serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Chucker
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Utility Libraries
    implementation(libs.timber)
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.weather.icon.view)
    implementation(libs.calendar.view)
    implementation(libs.algolia.search)
    implementation(libs.elastic.views)

    // Play Services & Firebase
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Testing - JVM
    testImplementation(libs.junit)
    testImplementation(libs.test.ext.junit)
    testImplementation(libs.test.core.ktx)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.hamcrest)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockito.core)

    // Testing - Instrumented
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(libs.espresso.idling.resource)
}
