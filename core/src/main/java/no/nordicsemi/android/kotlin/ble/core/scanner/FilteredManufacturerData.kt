package no.nordicsemi.android.kotlin.ble.core.scanner

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

/**
 * A helper class which groups manufacturer id and it's data.
 *
 * @property id Manufacturer id.
 * @property data Manufacturer data.
 * @property mask Manufacturer data mask. For any bit in the mask, set it the 1 if it needs to
 * match the one in manufacturer data, otherwise set it to 0.
 */
data class FilteredManufacturerData(
    val id: Int,
    val data: DataByteArray,
    val mask: DataByteArray? = null
)
