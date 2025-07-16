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
    implementation(project(":advertiser-android"))
    implementation(project(":advertiser-android-mock"))
    implementation(project(":client-android"))
    implementation(project(":client-android-mock"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Binder SLF4J -> Timber
    implementation(libs.slf4j.timber)
    debugImplementation(libs.leakcanary)
}