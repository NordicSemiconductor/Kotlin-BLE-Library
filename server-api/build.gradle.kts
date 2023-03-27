plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble.server.api"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.server.api"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.nordic.core)
}
