package no.nordicsemi.android.kotlin.ble.client.main.errors

import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus

data class GattOperationException(val status: BleGattOperationStatus) : Exception("Gatt operation failed with exception: $status")
