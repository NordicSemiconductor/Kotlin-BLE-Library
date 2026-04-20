/*
 * Copyright (c) 2026, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "no.nordicsemi.kotlin.ble.android.sample"
    defaultConfig {
        applicationId = "no.nordicsemi.kotlin.ble.android.sample"
    }
    @Suppress("UnstableApiUsage")
    androidResources {
        localeFilters += listOf("en")
    }
    flavorDimensions += listOf("mode")
    productFlavors {
        create("native") {
            isDefault = true
            dimension = "mode"
        }
        create("mock") {
            dimension = "mode"
        }
    }
}

dependencies {
    // Add dependencies to native implementations in "native" flavor.
    "nativeImplementation"(project(":advertiser-android"))
    "nativeImplementation"(project(":client-android"))
    // For "mock" flavor, use the mock implementations.
    "mockImplementation"(project(":advertiser-android-mock"))
    "mockImplementation"(project(":client-android-mock"))
    // For debug, let's use mock (for Previews).
    "debugImplementation"(project(":environment-android-mock"))
    // This is to provide the Environment for Composables.
    implementation(project(":environment-android-compose"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.core)

    // Binder SLF4J -> Timber
    implementation(libs.slf4j.timber)
    debugImplementation(libs.leakcanary)
}