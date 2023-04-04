plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.nordic.nexus)
}

group = "no.nordicsemi.android.kotlin.ble"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "mock"
    POM_NAME = "Nordic Kotlin library for BLE mock."

    POM_DESCRIPTION = "Nordic Android Kotlin BLE library"
    POM_URL = "https://github.com/NordicPlayground/Kotlin-BLE-Library"
    POM_SCM_URL = "https://github.com/NordicPlayground/Kotlin-BLE-Library"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicPlayground/Kotlin-BLE-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicPlayground/Kotlin-BLE-Library.git"

    POM_DEVELOPER_ID = "syzi"
    POM_DEVELOPER_NAME = "Sylwester Zieliński"
    POM_DEVELOPER_EMAIL = "sylwester.zielinski@nordicsemi.no"
}

android {
    namespace = "no.nordicsemi.android.kotlin.ble.mock"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":client-api"))
    implementation(project(":server-api"))

    implementation(libs.nordic.core)
}
