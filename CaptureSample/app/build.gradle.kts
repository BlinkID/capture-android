plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.microblink.capture.sample"
    compileSdk =  33

    defaultConfig {
        applicationId = "com.microblink.capture.sample"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material3:material3:1.2.0-alpha02")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")

    implementation(files("../../libs/capture-1.0.2.aar"))
    // additional capture lib dependencies
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.databinding:viewbinding:8.0.2")
    implementation("androidx.camera:camera-core:${rootProject.extra["camerax_version"]}")
    implementation("androidx.camera:camera-camera2:${rootProject.extra["camerax_version"]}")
    implementation("androidx.camera:camera-view:${rootProject.extra["camerax_version"]}")
    implementation("androidx.camera:camera-lifecycle:${rootProject.extra["camerax_version"]}")
}