# Kotlin BLE Library for Android

The library simplifies usage of Android Bluetooth Low Energy on Android. It is a wrapper around
native API and uses Kotlin Coroutines for asynchronous operations. The usage is designed to be more
natural according to the BLE specification.

> [!WARNING]
> This library is in early development stage and is not recommended for production use.
> The API is subject to change.
> Any feedback is welcome.

## Version 2

We are working on version 2 of the library. The new version will be a complete rewrite of the library.

Current status:
- [ ] Central role
    - [ ] Scanning
       - [x] Scanning for nearby Bluetooth LE devices
       - [ ] Ranging and monitoring device
       - [ ] Obtaining list of connected devices
    - [x] Establishing connection
       - [x] Direct (`autoConnect = false`)
       - [x] Using _AutoConnect_ feature
       - [x] Service discovery and subscribing to services changes
    - [x] Basic GATT operations
       - [x] Reading / writing characteristics
       - [x] Enabling notifications / indications
       - [x] Subscribing to value changes
       - [x] Requesting highest MTU
    - [ ] Advanced GATT operations
       - [x] Requesting PHY
       - [x] Subscribing to PHY changes
       - [x] Requesting connection priority
       - [x] Subscribing to connection parameter changes
       - [ ] Reliable write
    - [ ] Mock implementation
- [ ] Peripheral role
    - [x] Advertising
    - [ ] Setting up GATT server
    - [ ] GATT operations
    - [ ] Mock implementation

## Usage

### Scanning

```kotlin
val centralManager = CentralManager.Factory.native(context, scope)
centralManager
    .scan(1250.milliseconds) {
        ServiceUUID(someServiceUUID)
        Any {
            Name("MyName")
            Name("OtherName")
        }
    }
    .distinctByPeripheral()
    .map {
        it.peripheral
    }
    .onEach { peripheral ->
        // Do something with the peripheral
    }
    .launchIn(scope)
```

### Connecting

```kotlin
scope.launch {
    try {
        withTimeout(10000) {
            centralManager.connect(
                peripheral = peripheral,
                options = CentralManager.ConnectionOptions.Direct(
                    timeout = 3.seconds,
                    retry = 2,
                    retryDelay = 1.seconds,
                    Phy.PHY_LE_1M,
                ),
                // options = CentralManager.ConnectionOptions.AutoConnect,
            )
            Timber.i("Connected to ${peripheral.name}!")
        }
    
        // The first time the app connects to the peripheral it needs to initiate
        // observers for various parameters.
        // The observers will get cancelled when the connection scope gets cancelled,
        // that is when the device is manually disconnected in case of auto connect,
        // or disconnects for any reason when auto connect was false.
        peripheral.phy
            .onEach {
                Timber.i("PHY changed to: $it")
            }
            .onEmpty {
                Timber.w("PHY didn't change")
            }
            .onCompletion {
                Timber.d("PHY collection completed")
            }
            .launchIn(this)
    } catch (e: Exception) {
        Timber.e(e, "Connection attempt failed")
    }
}
```

### Service discovery

```kotlin
peripheral.services()
    .onEach { services ->
        Timber.i("Services changed: $services")

        services.forEach { remoteService ->
            // Do something with the service.
        }
    }
    .onEmpty {
        Timber.w("No services found")
    }
    .onCompletion {
        Timber.d("Service collection completed")
    }
    .launchIn(scope)
```

### GATT operations

#### Reading characteristic value

```kotlin
remoteService.characteristics.forEach { remoteCharacteristic ->
    try {
        Timber.w("Reading value of ${remoteCharacteristic.uuid}...")
        val value = remoteCharacteristic.read()
        Timber.i("Value of ${remoteCharacteristic.uuid}: 0x${value.toHexString()}")
    } catch (e: Exception) {
        // An exception is thrown when a characteristic is not readable, or an error occurs.
        Timber.e("Failed to read ${remoteCharacteristic.uuid}: ${e.message}")
    }
}
```

#### Subscibing to value changes

```kotlin
remoteService.characteristics.forEach { remoteCharacteristic ->
    try {
        Timber.w("Subscribing to ${remoteCharacteristic.uuid}...")
        remoteCharacteristic.subscribe()
            .onEach { newValue ->
                Timber.i("Value of ${remoteCharacteristic.uuid} changed: 0x${newValue.toHexString()}")
            }
            .onEmpty {
                Timber.w("No updates from ${remoteCharacteristic.uuid}")
            }
            .onCompletion {
                Timber.d("Stopped observing updates from ${remoteCharacteristic.uuid}")
            }
            .launchIn(scope)
    } catch (e: Exception) {
        // An exception is thrown when a characteristic does not have NOTFY or INDICATE property,
        // has no Client Characterisitc Configuration descriptor, or an error occurs.
        Timber.e("Failed to subscribe to ${remoteCharacteristic.uuid}: ${e.message}")
    }
}
```

For more, see [_sample_](https://github.com/NordicSemiconductor/Kotlin-BLE-Library/blob/version/2.0/sample/src/main/java/no/nordicsemi/kotlin/ble/android/sample/scanner/ScannerViewModel.kt).
