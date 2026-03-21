package no.nordicsemi.kotlin.ble.client.ios.cb

import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService
import platform.Foundation.NSError
import platform.Foundation.NSNumber

internal sealed class CBPeripheralEvent {
    data class DidDiscoverServices(val error: NSError?) : CBPeripheralEvent()
    data class DidDiscoverCharacteristics(val service: CBService, val error: NSError?) : CBPeripheralEvent()
    data class DidDiscoverDescriptors(val characteristic: CBCharacteristic, val error: NSError?) : CBPeripheralEvent()
    data class DidUpdateValueForCharacteristic(val characteristic: CBCharacteristic, val error: NSError?) : CBPeripheralEvent()
    data class DidWriteValueForCharacteristic(val characteristic: CBCharacteristic, val error: NSError?) : CBPeripheralEvent()
    data class DidUpdateValueForDescriptor(val descriptor: CBDescriptor, val error: NSError?) : CBPeripheralEvent()
    data class DidWriteValueForDescriptor(val descriptor: CBDescriptor, val error: NSError?) : CBPeripheralEvent()
    data class DidUpdateNotificationState(val characteristic: CBCharacteristic, val error: NSError?) : CBPeripheralEvent()
    data class DidReadRSSI(val rssi: NSNumber, val error: NSError?) : CBPeripheralEvent()
    data object DidInvalidateServices : CBPeripheralEvent()
}
