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

package no.nordicsemi.kotlin.ble.advertiser.android

import no.nordicsemi.kotlin.ble.advertiser.exception.InvalidAdvertisingDataException
import no.nordicsemi.kotlin.ble.advertiser.exception.InvalidAdvertisingDataException.Reason
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.util.BluetoothUuid
import org.jetbrains.annotations.Range

/**
 * A class that validates advertising data for given environment.
 *
 * @property deviceName The device name. On Android the advertised device name is added automatically,
 * and is set in the system settings. With longer device name the advertising data may be too large.
 * @property isLe2MPhySupported True if LE 2M PHY is supported.
 * @property isLeCodedPhySupported True if LE Coded PHY is supported.
 * @property isLeExtendedAdvertisingSupported True if extended advertising is supported.
 * @property leMaximumAdvertisingDataLength The maximum number of bytes in the advertising data.
 */
class AdvertisingDataValidator(
    private val deviceName: String,
    private val isLe2MPhySupported: Boolean,
    private val isLeCodedPhySupported: Boolean,
    private val isLeExtendedAdvertisingSupported: Boolean,
    private val leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int =
        if (isLeExtendedAdvertisingSupported) 1650 else 31,
) {
    companion object {
        /** The maximum number of bytes in the advertising data or scan response. */
        private const val MAX_LEGACY_ADVERTISING_DATA_BYTES = 31

        // Each field needs one byte for field length and another byte for field type.
        private const val OVERHEAD_BYTES_PER_FIELD = 2

        // Flags field will be set by system.
        private const val FLAGS_FIELD_BYTES = 3

        // Company Identifiers are 2 bytes long Bluetooth SIG assigned numbers.
        private const val MANUFACTURER_SPECIFIC_DATA_LENGTH = 2
    }

    /**
     * Validates the advertising parameters and payload.
     *
     * @param parameters The advertising parameters.
     * @param payload The advertising payload.
     * @throws InvalidAdvertisingDataException If the parameters or payload are invalid.
     */
    fun validate(parameters: AdvertisingSetParameters, payload: AdvertisingPayload) {
        // Android adds flags automatically when advertising is connectable and discoverable.
        val isConnectable = parameters.connectable
        val isScannable = parameters.scannable
        val isDiscoverable = parameters.discoverable
        val hasFlags = isConnectable && isDiscoverable

        // Check data length and optional features.
        if (parameters.legacy) {
            if (totalBytes(payload.advertisingData, hasFlags) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
                throw InvalidAdvertisingDataException(Reason.DATA_TOO_LARGE)
            }
            if (totalBytes(payload.scanResponse, false) > MAX_LEGACY_ADVERTISING_DATA_BYTES) {
                throw InvalidAdvertisingDataException(Reason.DATA_TOO_LARGE)
            }
        } else {
            if (parameters.primaryPhy == PrimaryPhy.PHY_LE_CODED && !isLeCodedPhySupported) {
                throw InvalidAdvertisingDataException(Reason.PHY_NOT_SUPPORTED)
            }
            if (parameters.secondaryPhy == Phy.PHY_LE_CODED && !isLeCodedPhySupported ||
                parameters.secondaryPhy == Phy.PHY_LE_2M && !isLe2MPhySupported) {
                throw InvalidAdvertisingDataException(Reason.PHY_NOT_SUPPORTED)
            }
            if (totalBytes(payload.advertisingData, hasFlags) > leMaximumAdvertisingDataLength) {
                throw InvalidAdvertisingDataException(Reason.DATA_TOO_LARGE)
            }
            if (totalBytes(payload.scanResponse, false) > leMaximumAdvertisingDataLength) {
                throw InvalidAdvertisingDataException(Reason.DATA_TOO_LARGE)
            }
            if (isScannable && payload.scanResponse == null) {
                throw InvalidAdvertisingDataException(Reason.SCAN_RESPONSE_REQUIRED)
            }
            if (!isConnectable && !isScannable && payload.scanResponse != null) {
                throw InvalidAdvertisingDataException(Reason.SCAN_RESPONSE_NOT_ALLOWED)
            }
        }
    }

    private fun totalBytes(data: AdvertisingPayload.AdvertisingData?, isFlagsIncluded: Boolean): Int {
        if (data == null) {
            return 0
        }
        // Flags field is omitted if the advertising is not connectable.
        var size = if (isFlagsIncluded) FLAGS_FIELD_BYTES else 0

        // The following fields are grouped into one field when doing advertising.
        data.serviceUuids?.let { serviceUuids ->
            var num16BitUuids = 0
            var num32BitUuids = 0
            var num128BitUuids = 0
            for (uuid in serviceUuids) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    ++num16BitUuids
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    ++num32BitUuids
                } else {
                    ++num128BitUuids
                }
            }
            // 16 bit service uuids are grouped into one field when doing advertising.
            if (num16BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * BluetoothUuid.UUID_BYTES_16_BIT
            }
            // 32 bit service uuids are grouped into one field when doing advertising.
            if (num32BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * BluetoothUuid.UUID_BYTES_32_BIT
            }
            // 128 bit service uuids are grouped into one field when doing advertising.
            if (num128BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num128BitUuids * BluetoothUuid.UUID_BYTES_128_BIT
            }
        }
        if (data.serviceSolicitationUuids != null) {
            var num16BitUuids = 0
            var num32BitUuids = 0
            var num128BitUuids = 0
            for (uuid in data.serviceSolicitationUuids) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    ++num16BitUuids
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    ++num32BitUuids
                } else {
                    ++num128BitUuids
                }
            }
            // 16 bit service uuids are grouped into one field when doing advertising.
            if (num16BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * BluetoothUuid.UUID_BYTES_16_BIT
            }
            // 32 bit service uuids are grouped into one field when doing advertising.
            if (num32BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * BluetoothUuid.UUID_BYTES_32_BIT
            }
            // 128 bit service uuids are grouped into one field when doing advertising.
            if (num128BitUuids != 0) {
                size += OVERHEAD_BYTES_PER_FIELD + num128BitUuids * BluetoothUuid.UUID_BYTES_128_BIT
            }
        }
        data.serviceData?.let { serviceData ->
            for (uuid in serviceData.keys) {
                val uuidLen = BluetoothUuid.uuidToBytes(uuid).size
                size += OVERHEAD_BYTES_PER_FIELD + uuidLen + (serviceData[uuid]?.size ?: 0)
            }
        }
        data.manufacturerData?.let { manufacturerData ->
            for (companyId in manufacturerData.keys) {
                size += OVERHEAD_BYTES_PER_FIELD + MANUFACTURER_SPECIFIC_DATA_LENGTH + (manufacturerData[companyId]?.size ?: 0)
            }
        }
        if (data.includeTxPowerLevel) {
            size += OVERHEAD_BYTES_PER_FIELD + 1 // tx power level value is one byte.
        }
        if (data.includeDeviceName) {
            size += OVERHEAD_BYTES_PER_FIELD + deviceName.length
        }
        return size
    }
}