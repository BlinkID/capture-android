plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.microblink.capture.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.microblink.capture.sample"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["kotlin_compiler_extension_version"] as String
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val cameraxVersion = rootProject.extra["camerax_version"] as String

dependencies {
    implementation("androidx.core:core-ktx:${rootProject.extra["core_ktx_version"]}")
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompat_version"]}")
    implementation(platform("androidx.compose:compose-bom:${rootProject.extra["compose_bom_version"]}"))
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:${rootProject.extra["navigation_compose_version"]}")

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    // capture SDK dependency, here we need only capture-core, because we will build our own UI
    implementation("com.microblink:capture-core:${rootProject.extra["capture_version"]}")
    implementation(project(":lib-common"))
}