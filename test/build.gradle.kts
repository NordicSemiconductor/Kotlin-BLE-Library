plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
}

group = "no.nordicsemi.android.kotlin.ble"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.test"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":advertiser"))
    implementation(project(":scanner"))
    implementation(project(":profile"))
    implementation(project(":client"))
    implementation(project(":client-android"))
    implementation(project(":client-mock"))
    implementation(project(":client-api"))
    implementation(project(":client"))
    implementation(project(":mock"))
    implementation(project(":server"))
    implementation(project(":server-android"))
    implementation(project(":server-mock"))
    implementation(project(":server-api"))

    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.junit4)
    testImplementation(libs.test.mockk)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.test.slf4j.simple)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.kotlin.junit)

    implementation(libs.nordic.core)
}
