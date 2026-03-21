package no.nordicsemi.kotlin.ble.client.ios.cb

import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSError
import platform.Foundation.NSNumber

internal sealed class CBCentralManagerEvent {
    data class StateUpdated(val state: CBManagerState) : CBCentralManagerEvent()
    data class DidConnect(val peripheral: CBPeripheral) : CBCentralManagerEvent()
    data class DidDisconnect(val peripheral: CBPeripheral, val error: NSError?) : CBCentralManagerEvent()
    data class DidFailToConnect(val peripheral: CBPeripheral, val error: NSError?) : CBCentralManagerEvent()
    data class DidDiscover(
        val peripheral: CBPeripheral,
        val advertisementData: Map<Any?, *>,
        val rssi: NSNumber
    ) : CBCentralManagerEvent()
}
