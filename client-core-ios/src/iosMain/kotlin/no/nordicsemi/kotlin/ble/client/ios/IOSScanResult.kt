package no.nordicsemi.kotlin.ble.client.ios

import no.nordicsemi.kotlin.ble.client.ScanResult
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy

class IOSScanResult(
    override val peripheral: IOSPeripheral,
    override val isConnectable: Boolean,
    override val advertisingData: IOSAdvertisingData,
    override val rssi: Int,
    override val txPowerLevel: Int?,
    override val primaryPhy: PrimaryPhy,
    override val secondaryPhy: Phy?,
    override val timestamp: Long,
) : ScanResult<IOSPeripheral, IOSAdvertisingData>
