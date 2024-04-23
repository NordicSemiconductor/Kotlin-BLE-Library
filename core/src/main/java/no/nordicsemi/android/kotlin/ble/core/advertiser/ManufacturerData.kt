package no.nordicsemi.android.kotlin.ble.core.advertiser

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray


/**
 * A helper class which groups manufacturer id and it's data.
 *
 * @property id Manufacturer id.
 * @property data Manufacturer data.
 */
data class ManufacturerData(
    val id: Int,
    val data: DataByteArray
)
