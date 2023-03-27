plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble.client.api"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.client.api"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.nordic.core)
}
