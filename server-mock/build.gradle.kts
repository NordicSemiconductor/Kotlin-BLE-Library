plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.server.mock"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mock"))
    implementation(project(":server-api"))

    implementation(libs.nordic.core)
}
