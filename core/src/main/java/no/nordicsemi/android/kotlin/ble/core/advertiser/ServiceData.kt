package no.nordicsemi.android.kotlin.ble.core.advertiser

import android.os.ParcelUuid
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

/**
 * A helper class which groups service id and it's data.
 *
 * @property uuid Service id.
 * @property data Service data.
 * @property mask Service data mask. For any bit in the mask, set it to 1 if it needs to match the
 * one in service data, otherwise set it to 0 to ignore that bit.
 */
data class ServiceData(
    val uuid: ParcelUuid,
    val data: DataByteArray,
    val mask: DataByteArray? = null
)
