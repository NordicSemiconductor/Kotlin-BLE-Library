package no.nordicsemi.android.kotlin.ble.core.scanner

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType

/**
 * Set filter on advertising data with specific advertising data type.
 *
 * The values of advertisingDataType are assigned by Bluetooth SIG.
 * For more details refer to
 * [Bluetooth Generic Access Profile](https://www.bluetooth.com/specifications/assigned-numbers/)
 * The advertisingDataMask must have the same length of advertisingData.
 *
 * @property type Advertising data type.
 * @property advertisingData Advertising data bytes.
 * @property mask Advertising data mask. For any bit in the mask, set it the 1 if it needs to
 * match the one in advertising data, otherwise set it to 0.
 */
data class AdvertisingDataTypeWithData(
    val type: BleType,
    val advertisingData: DataByteArray,
    val mask: DataByteArray
)
