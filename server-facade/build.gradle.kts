plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble.server.facade"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.server.facade"
}

dependencies {
    api(project(":server-api"))
    implementation(project(":core"))
    implementation(project(":mock"))
    implementation(project(":server-mock"))
    implementation(project(":server-android"))

    implementation(libs.nordic.core)
}
