plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "no.nordicsemi.kotlin.ble.android.sample"
    defaultConfig {
        applicationId = "no.nordicsemi.kotlin.ble.android.sample"
    }
    androidResources {
        localeFilters += listOf("en")
    }
    flavorDimensions += listOf("mode")
    productFlavors {
        create("native") {
            isDefault = true
            dimension = "mode"
        }
        create("mock") {
            dimension = "mode"
        }
    }
}

dependencies {
    // Add dependencies to native implementations in "native" flavor.
    "nativeImplementation"(project(":advertiser-android"))
    "nativeImplementation"(project(":client-android"))
    // For "mock" flavor, use the mock implementations.
    "mockImplementation"(project(":advertiser-android-mock"))
    "mockImplementation"(project(":client-android-mock"))
    // For debug, let's use mock (for Previews).
    "debugImplementation"(project(":environment-android-mock"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.core)

    // Binder SLF4J -> Timber
    implementation(libs.slf4j.timber)
    debugImplementation(libs.leakcanary)
}