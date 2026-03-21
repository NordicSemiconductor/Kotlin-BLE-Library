package no.nordicsemi.kotlin.ble.client.ios.cb

import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onFailure
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject

internal class CBCentralManagerDelegate(
    private val onError: (CBCentralManagerEvent, Throwable) -> Unit,
) : NSObject(), CBCentralManagerDelegateProtocol {
    private val _events: Channel<CBCentralManagerEvent> = Channel(Channel.Factory.UNLIMITED)
    val events = _events as ReceiveChannel<CBCentralManagerEvent>

    private fun send(event: CBCentralManagerEvent) {
        _events.trySend(event).onFailure { error ->
            if (error != null) onError(event, error)
        }
    }

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        send(CBCentralManagerEvent.StateUpdated(central.state))
    }

    override fun centralManager(
        central: CBCentralManager,
        didConnectPeripheral: CBPeripheral
    ) {
        send(CBCentralManagerEvent.DidConnect(didConnectPeripheral))
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        send(CBCentralManagerEvent.DidDisconnect(didDisconnectPeripheral, error))
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        send(CBCentralManagerEvent.DidFailToConnect(didFailToConnectPeripheral, error))
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ) {
        send(CBCentralManagerEvent.DidDiscover(didDiscoverPeripheral, advertisementData, RSSI))
    }
}