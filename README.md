# Kotlin BLE Library for Android

The library simplifies usage of Android Bluetooth Low Energy on Android. It is a wrapper around
native API and uses Kotlin Coroutines for asynchronous operations. The usage is designed to be more
natural according to the BLE specification.

## BLE Core
This module contains shared classes that are used between other modules. Currently the most important
class is [BleDevice](core/src/main/java/no/nordicsemi/android/kotlin/ble/core/BleDevice.kt) which is a wrapper around Android [BluetoothDevice](https://developer.android.com/reference/android/bluetooth/BluetoothDevice).

## BLE Scanner
This module contains scanner class which provides the list of available Bluetooth LE devices. Scanning
works as long as a Flow has an attached consumer. After the Flow is closes the scanning should stop.

```kotlin
val scanner = NordicScanner(context)

scanner.scan().onEach { devices ->
    //Consume the devices obtained during scanning.
}.launchIn(viewModelScope)
```

## BLE Client
This module is responsible for handling connection between the phone and the BLE device. It uses
Kotlin Coroutines instead of JAVA callbacks to handle asynchronous requests.

Below example is based on [the Blinky app](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

Connection to the Blinky DK may look like that:
```kotlin
viewModelScope.launch {
    //Connect a Bluetooth LE device. This is a suspend function which waits until device is in conncted state.
    val connection = blinkyDevice.connect(context)

    //Discover services on the Bluetooth LE Device. This is a suspend function which waits until device discovery is finished.
    val services = connection.getServices()

    //Remember needed service and characteristics which are used to communicate with the DK.
    val service = services.findService(BlinkySpecifications.UUID_SERVICE_DEVICE)!!
    ledCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_LED_CHAR)!!
    buttonCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_BUTTON_CHAR)!!

    //Observe button characteristics which detects when button is pressed.
    buttonCharacteristic.notification.onEach {
        //_state is a MutableStateFlow which propagates data to UI.
        _state.value = _state.value.copy(isButtonPressed = BlinkyButtonParser.isButtonPressed(it))
    }.launchIn(viewModelScope)

    //Enables notifications on DK. This is a suspend function which waits until notification is enabled.
    buttonCharacteristic.enableNotifications()

    //Check initial state of the Led. Read() is a suspend function which waits until the value is read from the DK.
    val isLedOn = BlinkyLedParser.isLedOn(ledCharacteristic.read())
    _state.value = _state.value.copy(isLedOn = isLedOn)
}
```

Turning on/off a LED light can looks like that:
```kotlin
viewModelScope.launch {
    if (state.value.isLedOn) {
        //_state is a MutableStateFlow which propagates data to UI.
        _state.value = _state.value.copy(isLedOn = false)
        //Write is a suspend function which waits for the operation to finish.
        ledCharacteristic.write(byteArrayOf(0x00))
    } else {
        //_state is a MutableStateFlow which propagates data to UI.
        _state.value = _state.value.copy(isLedOn = true)
        //Write is a suspend function which waits for the operation to finish.
        ledCharacteristic.write(byteArrayOf(0x01))
    }
}
```

## BLE Server
TBD

## BLE Mock
TBD
