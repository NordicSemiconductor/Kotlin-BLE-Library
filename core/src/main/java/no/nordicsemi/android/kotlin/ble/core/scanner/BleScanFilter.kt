package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.Build
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType

/**
 * BLE scanner filter. It is used to filter [BleScanResult] obtained from [BleScanner].
 *
 * @property type Advertising data types.
 * @property typeWithData Advertising data types with data.
 * @property deviceAddress BLE device address.
 * @property deviceName BLE device name.
 * @property manufacturerData Manufacturer data.
 * @property serviceData Service data.
 * @property serviceSolicitationUuid Service solicitation uuid.
 * @property serviceUuid Service uuid.
 */
data class BleScanFilter(
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val type: BleType? = null,
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val typeWithData: AdvertisingDataTypeWithData? = null,
    val deviceAddress: String? = null,
    val deviceName: String? = null,
    val manufacturerData: FilteredManufacturerData? = null,
    val serviceData: FilteredServiceData? = null,
    @RequiresApi(Build.VERSION_CODES.Q)
    val serviceSolicitationUuid: FilteredServiceSolicitationUuid? = null,
    val serviceUuid: FilteredServiceUuid? = null
)
