plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

group = "no.nordicsemi.android.kotlin.ble"

android {
    namespace = "no.nordicsemi.android.kotlin.ble.app.mock"
}

dependencies {
    implementation(project(":advertiser"))
    implementation(project(":core"))
    implementation(project(":scanner"))
    implementation(project(":server"))
    implementation(project(":client"))

    implementation(libs.nordic.theme)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.permission)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.iconsExtended)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}