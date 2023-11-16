buildscript {
    val kotlin_compiler_extension_version by extra("1.4.7")
    val core_ktx_version by extra("1.12.0")
    val material_version by extra("1.10.0")
    val appcompat_version by extra("1.6.1")
    val compose_version by extra("1.4.3")
    val compose_bom_version by extra("2023.08.00")
    val navigation_compose_version by extra("2.7.5")
    val camerax_version by extra("1.2.3")
    // Capture SDK version
    val capture_version by extra("1.2.0")

}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
}