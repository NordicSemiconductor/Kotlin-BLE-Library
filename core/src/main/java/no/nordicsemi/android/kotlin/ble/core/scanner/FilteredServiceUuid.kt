package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid

/**
 * Filter on service uuid.
 *
 * @property uuid Service uuid.
 * @property mask  The uuidMask is the bit mask for the [uuid].
 * Set any bit in the mask to 1 to indicate a match is needed for the bit in serviceUuid,
 * and 0 to ignore that bit.
 */
data class FilteredServiceUuid(
    val uuid: ParcelUuid,
    val mask: ParcelUuid? = null,
)
