plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.microblink.capture.sample.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation("androidx.core:core-ktx:${rootProject.extra["core_ktx_version"]}")
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompat_version"]}")
    implementation(platform("androidx.compose:compose-bom:${rootProject.extra["compose_bom_version"]}"))
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    // capture SDK dependency
    implementation("com.microblink:capture-core:${rootProject.extra["capture_version"]}")
}