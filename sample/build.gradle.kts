plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "no.nordicsemi.kotlin.ble.android.sample"
    defaultConfig {
        applicationId = "no.nordicsemi.kotlin.ble.android.sample"
        resourceConfigurations.add("en")
    }
}

dependencies {
    implementation(project(":client-android"))
    implementation(project(":client-android-mock"))

    implementation(libs.nordic.ui)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.permissions.ble)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Binder SLF4J -> Timber
    implementation(libs.slf4j.timber)
}