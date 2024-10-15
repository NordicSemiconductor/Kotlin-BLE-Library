/*
 * Copyright (c) 2024, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.le.ScanFilter
import android.os.Build
import android.os.ParcelUuid
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.DisjunctionFilterScope
import no.nordicsemi.kotlin.ble.core.AdvertisingDataType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * A filter that requires all the criteria to be satisfied.
 */
@OptIn(ExperimentalUuidApi::class)
internal class ConjunctionFilter: ConjunctionFilterScope {
    /**
     * The builder that will be used to build the [ScanFilter] set up using this filter.
     */
    private var builder: ScanFilter.Builder? = null

    /**
     * A list of [DisjunctionFilter]s that will be cross merged with the
     * each other and criteria set in this filter.
     */
    private var disjunctionFilters = mutableListOf<DisjunctionFilter>()

    /**
     * A filter that will be used in the library to filter out devices. These
     * criteria are not passed to [BluetoothLeScanner][android.bluetooth.le.BluetoothLeScanner], but are
     * checked in the library after the scan results are received.
     */
    private var runtimeFilter: RuntimeScanFilter? = null

    /**
     * Returns a list of filters that should be used for scanning.
     *
     * These filters are used in [BluetoothLeScanner][android.bluetooth.le.BluetoothLeScanner.startScan].
     */
    val filters: List<ScanFilter>?
        get() {
            // If there is no DisjunctionFilter, build and return the single filter.
            if (disjunctionFilters.isEmpty()) return builder?.let { listOf(it.build()) }
            // Create a list of filters to be merged with disjunction filters.
            var input = mutableListOf<ScanFilter>()
            // Add the local filter, if such was set.
            builder?.build()?.let { input.add(it) }
            // Create a temporary list of merged filters.
            var output = mutableListOf<ScanFilter>()
            // Now, cross merge each DisjunctionFilter with all filters from the input list,
            // replacing the input list with the output list after each iteration.
            disjunctionFilters.forEach { disjunctionFilter ->
                if (input.isEmpty())
                    input.add(ScanFilter.Builder().build())
                input.forEach { localFilter ->
                    disjunctionFilter.filters?.forEach { scanFilter ->
                        output.add(localFilter.merged(scanFilter))
                    }
                }
                // Merged filters are now in the output list, swap the lists and clear the output.
                if (output.isNotEmpty()) {
                    input = output
                    output = mutableListOf()
                }
            }
            return input
        }
    val runtimeFilters: List<RuntimeScanFilter>?
        get() {
            // If there is no DisjunctionFilter, return the single filter.
            if (disjunctionFilters.isEmpty()) return runtimeFilter?.let { listOf(it) }
            // Create a list of filters to be merged with disjunction filters.
            var input = mutableListOf<RuntimeScanFilter>()
            // Add the local filter, if such was set.
            runtimeFilter?.let { input.add(it) }
            // Create a temporary list of merged filters.
            var output = mutableListOf<RuntimeScanFilter>()
            // Now, cross merge each DisjunctionFilter with all filters from the input list,
            // replacing the input list with the output list after each iteration.
            disjunctionFilters.forEach { disjunctionFilter ->
                input.forEach { localFilter ->
                    disjunctionFilter.runtimeScanFilter?.forEach { runtimeFilter ->
                        output.add(localFilter.merged(runtimeFilter))
                    }
                }
                // Merged filters are now in the output list, swap the lists and clear the output.
                if (output.isNotEmpty()) {
                    input = output
                    output = mutableListOf()
                }
            }
            return input
        }

    override fun Any(filter: DisjunctionFilterScope.() -> Unit) {
        disjunctionFilters.add(DisjunctionFilter().apply(filter))
    }

    override fun All(filter: ConjunctionFilterScope.() -> Unit) {
        // Simply apply the filter here.
        filter()
    }

    override fun Name(name: String) {
        builder = (builder ?: ScanFilter.Builder()).also {
            it.setDeviceName(name)
        }
    }

    override fun Name(regex: Regex) {
        // ScanFilter only supports exact name matching.
        // Save the regex for runtime filtering.
        runtimeFilter = (runtimeFilter ?: RuntimeScanFilter()).also {
            it.nameRegex = regex
        }
    }

    override fun Address(address: String) {
        // The address checked natively by setDeviceAddress is of type PUBLIC.
        // Save the address for runtime filtering.
        runtimeFilter = (runtimeFilter ?: RuntimeScanFilter()).also {
            it.address = address
        }
    }

    override fun ServiceUUID(uuid: Uuid, mask: Uuid?) {
        builder = (builder ?: ScanFilter.Builder()).also {
            it.setServiceUuid(ParcelUuid(uuid.toJavaUuid()), mask?.let { mask -> ParcelUuid(mask.toJavaUuid()) })
        }
    }

    override fun ServiceSolicitationUUID(uuid: Uuid, mask: Uuid?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder = (builder ?: ScanFilter.Builder()).also {
                it.setServiceSolicitationUuid(ParcelUuid(uuid.toJavaUuid()), mask?.let { mask -> ParcelUuid(mask.toJavaUuid()) })
            }
        } else {
            runtimeFilter = (runtimeFilter ?: RuntimeScanFilter()).also {
                it.serviceSolicitationUuid = uuid
                it.serviceSolicitationUuidMask = mask
            }
        }
    }

    override fun ServiceData(uuid: Uuid, data: ByteArray, mask: ByteArray?) {
        builder = (builder ?: ScanFilter.Builder()).also {
            it.setServiceData(ParcelUuid(uuid.toJavaUuid()), data, mask)
        }
    }

    override fun ManufacturerData(companyId: Int, data: ByteArray, mask: ByteArray?) {
        builder = (builder ?: ScanFilter.Builder()).also {
            it.setManufacturerData(companyId, data, mask)
        }
    }

    override fun Custom(type: AdvertisingDataType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder = (builder ?: ScanFilter.Builder()).also {
                it.setAdvertisingDataType(type.type)
            }
        } else {
            runtimeFilter = (runtimeFilter ?: RuntimeScanFilter()).also {
                it.customAdvertisingDataType = type
                it.customAdvertisingData = null
                it.customAdvertisingDataMask = null
            }
        }
    }

    override fun Custom(type: AdvertisingDataType, data: ByteArray, mask: ByteArray?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For some reason, the mask is required when data is provided.
            // This is an Android API bug.
            // Let's work around it by setting the mask to 0xFF for each byte.
            val nonNullMask = mask ?: data.map { 0xFF.toByte() }.toByteArray()
            builder = (builder ?: ScanFilter.Builder()).also {
                it.setAdvertisingDataTypeWithData(type.type, data, nonNullMask)
            }
        } else {
            runtimeFilter = (runtimeFilter ?: RuntimeScanFilter()).also {
                it.customAdvertisingDataType = type
                it.customAdvertisingData = data
                it.customAdvertisingDataMask = mask
            }
        }
    }
}

/**
 * A filter that requires at least one of the criteria to be satisfied.
 */
@OptIn(ExperimentalUuidApi::class)
internal class DisjunctionFilter: DisjunctionFilterScope {
    /**
     * A list of [ConjunctionFilter]s. Each filter will be applied separately.
     */
    private val conjunctionFilters = mutableListOf<ConjunctionFilter>()
    /** A flag used to mark whether an empty name regex filter was added. */
    private var hasAnyNameRegex= false

    /**
     * Returns a list of filters that should be used for scanning.
     */
    val filters: List<ScanFilter>?
        get() {
            if (conjunctionFilters.isEmpty()) {
                return null
            }
            val filters = mutableListOf<ScanFilter>()
            conjunctionFilters.forEach { conjunction ->
                conjunction.filters?.let { filters.addAll(it) }
            }
            return filters
        }
    val runtimeScanFilter: List<RuntimeScanFilter>?
        get() {
            if (conjunctionFilters.isEmpty()) {
                return null
            }
            val filters = mutableListOf<RuntimeScanFilter>()
            conjunctionFilters.forEach { conjunction ->
                conjunction.runtimeFilters?.let { filters.addAll(it) }
            }
            return filters
        }

    override fun Any(filter: DisjunctionFilterScope.() -> Unit) {
        // Simply apply the filter here.
        filter()
    }

    override fun All(filter: ConjunctionFilterScope.() -> Unit) {
        conjunctionFilters.add(ConjunctionFilter().apply(filter))
    }

    override fun Name(name: String) {
        conjunctionFilters.add(ConjunctionFilter().apply { Name(name) })
        // An empty Regex name filter is added to pass runtime checks for filters
        // set using the native name filter.
        //
        // This is useful, when one is trying to filter for:
        // Any {
        //    Name(<Some name>)
        //    Name(Regex(<Some pattern>))
        // }
        // Without this filter, the runtime filter would be set to only pass the devices
        // with the name matching the regex, and not the one with the exact name, which were already
        // filtered by the OS.
        if (!hasAnyNameRegex) {
            conjunctionFilters.add(ConjunctionFilter().apply { Name(Regex(".*")) })
            hasAnyNameRegex = true
        }
    }

    override fun Name(regex: Regex) {
        conjunctionFilters.add(ConjunctionFilter().apply { Name(regex) })
    }

    override fun Address(address: String) {
        conjunctionFilters.add(ConjunctionFilter().apply { Address(address) })
    }

    override fun ServiceUUID(uuid: Uuid, mask: Uuid?) {
        conjunctionFilters.add(ConjunctionFilter().apply { ServiceUUID(uuid, mask) })
    }

    override fun ServiceSolicitationUUID(uuid: Uuid, mask: Uuid?) {
        conjunctionFilters.add(ConjunctionFilter().apply { ServiceSolicitationUUID(uuid, mask) })
    }

    override fun ServiceData(uuid: Uuid, data: ByteArray, mask: ByteArray?) {
        conjunctionFilters.add(ConjunctionFilter().apply { ServiceData(uuid, data, mask) })
    }

    override fun ManufacturerData(companyId: Int, data: ByteArray, mask: ByteArray?) {
        conjunctionFilters.add(ConjunctionFilter().apply { ManufacturerData(companyId, data, mask) })
    }

    override fun Custom(type: AdvertisingDataType) {
        conjunctionFilters.add(ConjunctionFilter().apply { Custom(type) })
    }

    override fun Custom(type: AdvertisingDataType, data: ByteArray, mask: ByteArray?) {
        conjunctionFilters.add(ConjunctionFilter().apply { Custom(type, data, mask) })
    }

}

/**
 * A filter that will be used in the library to filter out devices.
 *
 * They are either not supported by the Android API, or are not supported on some Android versions.
 */
@OptIn(ExperimentalUuidApi::class)
internal class RuntimeScanFilter {
    /** Private or public MAC address. */
    var address: String? = null
    /** Regular expression for the device name. */
    var nameRegex: Regex? = null
    /** Service solicitation UUID. */
    var serviceSolicitationUuid: Uuid? = null
    /** Service solicitation UUID mask. */
    var serviceSolicitationUuidMask: Uuid? = null
    /** Custom advertising data type. */
    var customAdvertisingDataType: AdvertisingDataType? = null
    /** Custom advertising data. */
    var customAdvertisingData: ByteArray? = null
    /** Custom advertising data mask. */
    var customAdvertisingDataMask: ByteArray? = null

    fun merged(other: RuntimeScanFilter): RuntimeScanFilter {
        return RuntimeScanFilter().also {
            it.address = address ?: other.address
            it.nameRegex = nameRegex ?: other.nameRegex
            it.serviceSolicitationUuid = serviceSolicitationUuid ?: other.serviceSolicitationUuid
            it.serviceSolicitationUuidMask = serviceSolicitationUuidMask ?: other.serviceSolicitationUuidMask
            it.customAdvertisingDataType = customAdvertisingDataType ?: other.customAdvertisingDataType
            it.customAdvertisingData = customAdvertisingData ?: other.customAdvertisingData
            it.customAdvertisingDataMask = customAdvertisingDataMask ?: other.customAdvertisingDataMask
        }
    }
}
