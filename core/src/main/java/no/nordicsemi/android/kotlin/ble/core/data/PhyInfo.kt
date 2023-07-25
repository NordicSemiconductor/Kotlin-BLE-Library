package no.nordicsemi.android.kotlin.ble.core.data

/**
 * Wrapper class grouping transmitter and receiver PHY values.
 *
 * @property txPhy Transmitter PHY.
 * @property rxPhy Receiver PHY.
 */
data class PhyInfo(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy
)
