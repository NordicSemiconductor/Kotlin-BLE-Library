# Module environment-android-mock

Provides types for customizing a mock Android environment.

## Overview

The `MockAndroidEnvironment` class emulates a native Android environment, allowing you to test 
Bluetooth LE functionality on an emulator or a device with a different OS version than the one you 
are targeting. This is particularly useful for simulating different permission states and hardware 
capabilities.

## Usage

Whether your application acts as a client or a server, the first step is to set up the environment.

Start by adding the dependency to your `build.gradle.kts` file:
```kotlin
implementation("no.nordicsemi.kotlin.ble:environment-android-mock:<VERSION>")
```

**Note:** This library is also available through the [Nordic BOM](https://github.com/nordicsemi/Nordic-Version-Catalog).

With the dependency added, you can define a mock environment:

```kotlin
val environment = MockAndroidEnvironment.Api31(
    isLocationPermissionGranted = false,
    isBluetoothScanPermissionGranted = false,
    isBluetoothConnectPermissionGranted = false,
    isBluetoothAdvertisePermissionGranted = false,
    // ... other parameters

    // Check your AndroidManifest and set this flag based on the 'neverForLocation'
    // attribute in the BLUETOOTH_SCAN permission.
    isNeverForLocationFlagSet = true,
)
```

It's crucial to close the environment when it's no longer needed to release system resources.

```kotlin
environment.close()
```

### Hilt Integration

Here is an example of how `MockAndroidEnvironment` can be provided and managed within an 
`ActivityRetainedComponent` using Hilt:

```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
object EnvironmentModule {

    @ActivityRetainedScoped
    @Provides
    fun provideEnvironment(
        @ApplicationContext context: Context,
        lifecycle: ActivityRetainedLifecycle
    ): MockAndroidEnvironment {
        val env = MockAndroidEnvironment.Api31(
            // Configure the mock environment here.
            isBluetoothScanPermissionGranted = false,
            // ...

            // Check your AndroidManifest and set this flag based on the 'neverForLocation'
            // attribute in the BLUETOOTH_SCAN permission.
            isNeverForLocationFlagSet = false,
        )
        // Ensure the environment is closed when the lifecycle is cleared.
        lifecycle.addOnClearedListener { env.close() }
        return env
    }
}
```

To allow for easy swapping between mock and native environments, provide a binding that casts 
`MockAndroidEnvironment` to the `AndroidEnvironment` interface:
```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class AndroidEnvironmentModule {

    @Binds
    abstract fun bindEnvironment(environment: MockAndroidEnvironment): AndroidEnvironment
}
```

# Package no.nordicsemi.kotlin.ble.environment.android.mock

This package contains classes for defining a mock environment on Android.