package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

/**
 * A helper class which groups service id, it's data and mask. Used as a scanning filter.
 *
 * @property uuid Service id.
 * @property data Service data.
 * @property mask For any bit in the mask, set it the 1 if it needs to
 * match the one in service data, otherwise set it to 0.
 */
data class FilteredServiceData(
    val uuid: ParcelUuid,
    val data: DataByteArray,
    val mask: DataByteArray? = null
)
