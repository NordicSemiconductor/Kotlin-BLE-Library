plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.server.real"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":server-api"))

    implementation(libs.nordic.core)
}
