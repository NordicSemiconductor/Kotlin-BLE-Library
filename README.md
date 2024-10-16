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
    - [x] Scanning
    - [ ] Ranging and monitoring device
    - [ ] Obtaining list of connected devices
    - [x] Establishing connection
    - [x] Basic GATT operations
    - [x] Advanced GATT operations
    - [ ] Mock implementation
- [ ] Peripheral role
    - [ ] Advertising
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
    .onEach {
        // Do something with the peripheral
    }
    .launchIn(scope)
```