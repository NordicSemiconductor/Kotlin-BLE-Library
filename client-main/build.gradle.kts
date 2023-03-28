plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

group = "no.nordicsemi.android.kotlin.ble.client.facade"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.client.facade"
}

dependencies {
    api(project(":client-api"))

    implementation(project(":core"))
    implementation(project(":mock"))
    implementation(project(":client-android"))
    implementation(project(":client-mock"))

    implementation(libs.nordic.core)
}
