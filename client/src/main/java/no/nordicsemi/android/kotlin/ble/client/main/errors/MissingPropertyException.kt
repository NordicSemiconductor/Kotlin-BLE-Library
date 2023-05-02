package no.nordicsemi.android.kotlin.ble.client.main.errors

import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty

class MissingPropertyException(property: BleGattProperty) : GattException(
    "Operation cannot be performed because of the missing property: $property"
)
