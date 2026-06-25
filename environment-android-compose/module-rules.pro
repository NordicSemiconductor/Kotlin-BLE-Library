# This module depends on 2 optional modules: environment-android and environment-android-mock.
# At leat one of them must be added to the final project, but the lack of the other one
# would generate a build error. These rules silence these warnings.
-dontwarn no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment
-dontwarn no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment$Companion
-dontwarn no.nordicsemi.kotlin.ble.environment.android.mock.MockAndroidEnvironment
