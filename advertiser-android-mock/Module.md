# Module advertiser-android-mock

A mock implementation for the Bluetooth Low Energy advertiser on Android.

## Overview

This module provides a mock version of the Bluetooth LE advertiser, which can be used for testing 
purposes without requiring actual Bluetooth hardware.

## Usage

This section shows how to use the mock Android implementation of the Bluetooth Low Energy advertiser.

### Advertiser

Create an instance of the `BluetoothLeAdvertiser`.
```kotlin
val advertiser = BluetoothLeAdvertiser.mock(environment)
```

### Starting advertising

Begin mocking Bluetooth LE advertising by providing the parameters and advertising data:
```kotlin
scope.launch {
    try {
        advertiser.advertise(
            parameters = parameters,
            payload = AdvertisingPayload(
                advertisingData = {
                    // Include the device's name.
                    IncludeLocalName()
                    // Environmental Sensing Service UUID.
                    ServiceUuid(0x181A)
                },
                scanResponse = {
                    // Temperature characteristic UUID: 22'C.
                    ServiceData(0x2A6E, byteArrayOf(22))
                },
            ),
            timeout = 3.seconds,
        ) { txPower ->
            // This callback is called when the advertising starts.
            // The parameter is the actual TX power used for advertising, in dBm.
            Timber.i("Tx power: $txPower")
        }
    } catch (_: CancellationException) {
        Timber.i("Advertising cancelled")
    } catch (e: Exception) {
        Timber.e(e, "Advertising failed")
    }
}
```

The mock advertiser will not use the native Bluetooth adapter. It can be used in unit tests,
or devices that do not support some features. The mock environment is defined using `MockAndroidEnvironment`
from *environment-android-mock* module.

# Package no.nordicsemi.kotlin.ble.advertiser.android.mock

This package contains a Factory method to create a mock Android implementation for the Bluetooth LE Advertiser.