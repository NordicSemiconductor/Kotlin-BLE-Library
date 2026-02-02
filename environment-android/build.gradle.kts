plugins {
    alias(libs.plugins.nordic.library)
    alias(libs.plugins.nordic.kotlin.android)
    alias(libs.plugins.nordic.nexus.android)
}

group = "no.nordicsemi.kotlin.ble"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "environment-android"
    POM_NAME = "Native Android Environment Module"
    POM_DESCRIPTION = "A part of Kotlin BLE Library providing Android-specific environment implementation."
    POM_URL = "https://github.com/NordicSemiconductor/Kotlin-BLE-Library"
    POM_SCM_URL = "https://github.com/NordicSemiconductor/Kotlin-BLE-Library"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicSemiconductor/Kotlin-BLE-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicSemiconductor/Kotlin-BLE-Library.git"
}

android {
    namespace = "no.nordicsemi.kotlin.ble.environment.android"
}

dependencies {
    api(project(":core-android"))

    implementation(libs.androidx.core)
    implementation(libs.slf4j)
}

dokka {
    dokkaSourceSets.named("main") {
        includes.from("Module.md")
        perPackageOption {
            matchingRegex.set("no.nordicsemi.kotlin.ble.environment.android.internal")
            suppress.set(true)
        }
    }
}