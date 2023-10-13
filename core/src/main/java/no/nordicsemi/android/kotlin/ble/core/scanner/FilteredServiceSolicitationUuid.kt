package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid

/**
 * Set filter on partial service Solicitation uuid.
 *
 * @property uuid Solicitation uuid.
 * @property mask The SolicitationUuidMask is the bit mask for the serviceSolicitationUuid.
 * Set any bit in the mask to 1 to indicate a match is needed for the bit in
 * serviceSolicitationUuid, and 0 to ignore that bit.
 */
data class FilteredServiceSolicitationUuid(
    val uuid: ParcelUuid,
    val mask: ParcelUuid? = null
)