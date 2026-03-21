package no.nordicsemi.kotlin.ble.client.ios.cb

import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onFailure
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject

internal class CBPeripheralDelegate(
    private val onError: (CBPeripheralEvent, Throwable) -> Unit,
) : NSObject(), CBPeripheralDelegateProtocol {
    private val _events: Channel<CBPeripheralEvent> = Channel(Channel.UNLIMITED)
    val events = _events as ReceiveChannel<CBPeripheralEvent>

    private fun send(event: CBPeripheralEvent) {
        _events.trySend(event).onFailure { error ->
            if (error != null) onError(event, error)
        }
    }

    override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
        send(CBPeripheralEvent.DidDiscoverServices(didDiscoverServices))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidDiscoverCharacteristics(didDiscoverCharacteristicsForService, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverDescriptorsForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidDiscoverDescriptors(didDiscoverDescriptorsForCharacteristic, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidUpdateValueForCharacteristic(didUpdateValueForCharacteristic, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidWriteValueForCharacteristic(didWriteValueForCharacteristic, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForDescriptor: CBDescriptor,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidUpdateValueForDescriptor(didUpdateValueForDescriptor, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForDescriptor: CBDescriptor,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidWriteValueForDescriptor(didWriteValueForDescriptor, error))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        send(CBPeripheralEvent.DidUpdateNotificationState(didUpdateNotificationStateForCharacteristic, error))
    }

    override fun peripheral(peripheral: CBPeripheral, didReadRSSI: NSNumber, error: NSError?) {
        send(CBPeripheralEvent.DidReadRSSI(didReadRSSI, error))
    }
}
