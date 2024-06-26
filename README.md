# Kotlin BLE Library for Android

The library simplifies usage of Android Bluetooth Low Energy on Android. It is a wrapper around
native API and uses Kotlin Coroutines for asynchronous operations. The usage is designed to be more
natural according to the BLE specification.

## BLE Scanner

This module contains a scanner class which provides the list of available Bluetooth LE devices. Each 
device is kept in an aggregator which keeps devices in map together with their scan records. Scanning
works as long as a Flow has an attached consumer. After the Flow is closed the scanning stops.

```kotlin
    //Create aggregator which will concat scan records with a device
    val aggregator = BleScanResultAggregator()
    BleScanner(context).scan()
        .map { aggregator.aggregateDevices(it) } //Add new device and return an aggregated list
        .onEach { _devices.value = it } //Propagated state to UI
        .launchIn(viewModelScope) //Scanning will stop after we leave the screen
```

### Dependency
```Groovy
implementation 'no.nordicsemi.android.kotlin.ble:scanner:1.1.0'
```

## BLE Client
This module is responsible for handling connection between the phone and the BLE device. It uses
Kotlin Coroutines instead of JAVA callbacks to handle asynchronous requests.

Below example is based on [the Blinky profile](https://github.com/NordicSemiconductor/Android-nRF-Blinky).

Connection to the Blinky DK may look like that:
```kotlin
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt

viewModelScope.launch {
    //Connect a Bluetooth LE device. This is a suspend function which waits until device is in connected state.
    val connection = ClientBleGatt.connect(context, blinkyDevice, viewModelScope, options) //blinkyDevice from scanner

    //Discover services on the Bluetooth LE Device. This is a suspend function which waits until device discovery is finished.
    val services = connection.discoverServices()

    //Remember needed service and characteristics which are used to communicate with the DK.
    val service = services.findService(BlinkySpecifications.UUID_SERVICE_DEVICE)!!
    ledCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_LED_CHAR)!!
    buttonCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_BUTTON_CHAR)!!

    //Observe button characteristic which detects when a button is pressed
    //getNotifications() is a suspend function which waits until notification is enabled.
    buttonCharacteristic.getNotifications().onEach {
        //_state is a MutableStateFlow which propagates data to UI.
        _state.value = _state.value.copy(isButtonPressed = BlinkyButtonParser.isButtonPressed(it))
    }.launchIn(viewModelScope)
    
    //Check the initial state of the Led. Read() is a suspend function which waits until the value is read from the DK.
    val isLedOn = BlinkyLedParser.isLedOn(ledCharacteristic.read())
    _state.value = _state.value.copy(isLedOn = isLedOn)
}
```

Turning on/off a LED light can looks like that:
```kotlin
viewModelScope.launch {
    if (state.value.isLedOn) {
        //Write is a suspend function which waits for the operation to finish.
        ledCharacteristic.write(DataByteArray.from(0x00))
        //No exception means that writing was a success. We can update the UI.
        _state.value = _state.value.copy(isLedOn = false)
    } else {
        //Write is a suspend function which waits for the operation to finish.
        ledCharacteristic.write(DataByteArray.from(0x01))
        //No exception means that writing was a success. We can update the UI.
        _state.value = _state.value.copy(isLedOn = true)
    }
}
```

### Dependency
```Groovy
implementation 'no.nordicsemi.android.kotlin.ble:client:1.1.0'
```

## BLE Advertiser
The library is used to advertise the server.

```kotlin
    val advertiser = BleAdvertiser.create(context)
    val advertiserConfig = BleAdvertisingConfig(
        settings = BleAdvertisingSettings(
            deviceName = "My Server" // Advertise a device name
        ),
        advertiseData = BleAdvertisingData(
            ParcelUuid(BlinkySpecifications.UUID_SERVICE_DEVICE) //Advertise main service uuid.
        )
    )

    viewModelScope.launch {
        advertiser.advertise(advertiserConfig) //Start advertising
            .cancellable()
            .catch { it.printStackTrace() }
            .collect { //Observe advertiser lifecycle events
                if (it is OnAdvertisingSetStarted) { //Handle advertising start event
                    _state.value = _state.value.copy(isAdvertising = true)
                }
                if (it is OnAdvertisingSetStopped) { //Handle advertising stop event
                    _state.value = _state.value.copy(isAdvertising = false)
                }
            }
    }
```

### Dependency
```Groovy
implementation 'no.nordicsemi.android.kotlin.ble:advertiser:1.1.0'
```

## BLE Server
The library is used to create a Bluetooth LE server. 

### Declaring a server definition
```kotlin
    viewModelScope.launch {
        //Define led characteristic
        val ledCharacteristic = BleServerGattCharacteristicConfig(
            BlinkySpecifications.UUID_LED_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)
        )

        //Define button characteristic
        val buttonCharacteristic = BleServerGattCharacteristicConfig(
            BlinkySpecifications.UUID_BUTTON_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_NOTIFY),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)
        )

        //Put led and button characteristics inside a service
        val serviceConfig = BleServerGattServiceConfig(
            BlinkySpecifications.UUID_SERVICE_DEVICE,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(ledCharacteristic, buttonCharacteristic)
        )

        val server = BleGattServer.create(context, viewModelScope, serviceConfig)
    }
```

### Observing server incoming connections
```kotlin
    server.connectionEvents
        .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
        .map { it.connection }
        .onEach {
            it.services.findService(BlinkySpecifications.UUID_SERVICE_DEVICE)?.let {
                setUpServices(it)
            }
        }
        .launchIn(viewModelScope)
```

### Setting up characteristic behaviour
```kotlin
    private fun setUpServices(services: BleGattServerService) {
        val ledCharacteristic = services.findCharacteristic(BlinkySpecifications.UUID_LED_CHAR)!!
        val buttonCharacteristic = services.findCharacteristic(BlinkySpecifications.UUID_BUTTON_CHAR)!!

        ledCharacteristic.value.onEach {
            _state.value = _state.value.copy(isLedOn = it != DataByteArray.from(0x00))
        }.launchIn(viewModelScope)

        buttonCharacteristic.value.onEach {
            _state.value = _state.value.copy(isButtonPressed = it != DataByteArray.from(0x00))
        }.launchIn(viewModelScope)

        this.ledCharacteristic = ledCharacteristic
        this.buttonCharacteristic = buttonCharacteristic
    }

    fun onButtonPressedChanged(isButtonPressed: Boolean) = viewModelScope.launch {
        val value = if (isButtonPressed) {
            DataByteArray.from(0x01)
        } else {
            DataByteArray.from(0x00)
        }
        buttonCharacteristic.setValueAndNotifyClient(value)
    }
```

### Dependency
```Groovy
implementation 'no.nordicsemi.android.kotlin.ble:server:1.1.0'
```
