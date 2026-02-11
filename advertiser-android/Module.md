# Module advertiser-android

Android implementation for the Bluetooth Low Energy advertiser.

## Overview

This package contains the native Android implementation of the Bluetooth Low Energy advertiser.

Bluetooth LE advertising is available since Android 5 Lollipop, and has been extended in Android 8 Oreo
with adding Advertising Extension.

## Usage

This section shows how to use the native Android implementation of the Bluetooth Low Energy advertiser.

### Advertiser

Create an instance of the `BluetoothLeAdvertiser`.
```kotlin
val advertiser = BluetoothLeAdvertiser.native(environment)
```

### Starting advertising

Begin Bluetooth LE advertising by providing the parameters and advertising data:
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

# Package no.nordicsemi.kotlin.ble.advertiser.android

This package contains a Factory method to create an Android implementation for the Bluetooth LE Advertiser.