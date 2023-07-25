package no.nordicsemi.android.kotlin.ble.client.main.errors

import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty

/**
 * An exception indicating that the operation cannot be performed, because of a missing property
 * i.e. an attempt to read from a characteristic which doesn't have [BleGattProperty.PROPERTY_READ].
 *
 * @constructor
 * Creates exception instance.
 *
 * @param property A missing property which causes exception.
 */
class MissingPropertyException(property: BleGattProperty) : GattException(
    "Operation cannot be performed because of the missing property: $property"
)
