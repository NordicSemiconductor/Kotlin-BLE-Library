/*
 * Copyright (c) 2023, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.core.mapper

/**
 * Inspired by: [ScanRecord](https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/framework/java/android/bluetooth/le/ScanRecord.java)
 */
enum class BleType(val value: Int) {

    /**
     * Data type is not set for the filter. Will not filter advertising data type.
     */
    DATA_TYPE_NONE(-1),

    /**
     * Data type is Flags, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_FLAGS(0x01),

    /**
     * Data type is Incomplete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL(0x02),

    /**
     * Data type is Complete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE(0x03),

    /**
     * Data type is Incomplete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL(0x04),

    /**
     * Data type is Complete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE(0x05),

    /**
     * Data type is Incomplete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL(0x06),

    /**
     * Data type is Complete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE(0x07),

    /**
     * Data type is Shortened Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_LOCAL_NAME_SHORT(0x08),

    /**
     * Data type is Complete Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_LOCAL_NAME_COMPLETE(0x09),

    /**
     * Data type is Tx Power Level, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_TX_POWER_LEVEL(0x0A),

    /**
     * Data type is Class of Device, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_CLASS_OF_DEVICE(0x0D),

    /**
     * Data type is Simple Pairing Hash C, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SIMPLE_PAIRING_HASH_C(0x0E),

    /**
     * Data type is Simple Pairing Randomizer R, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SIMPLE_PAIRING_RANDOMIZER_R(0x0F),

    /**
     * Data type is Device ID, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_DEVICE_ID(0x10),

    /**
     * Data type is Security Manager Out of Band Flags, see the Bluetooth Generic Access Profile for
     * more details.
     */
    DATA_TYPE_SECURITY_MANAGER_OUT_OF_BAND_FLAGS(0x11),

    /**
     * Data type is Slave Connection Interval Range, see the Bluetooth Generic Access Profile for
     * more details.
     */
    DATA_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE(0x12),

    /**
     * Data type is List of 16-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT(0x14),

    /**
     * Data type is List of 128-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT(0x15),

    /**
     * Data type is Service Data - 16-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SERVICE_DATA_16_BIT(0x16),

    /**
     * Data type is Public Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_PUBLIC_TARGET_ADDRESS(0x17),

    /**
     * Data type is Random Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_RANDOM_TARGET_ADDRESS(0x18),

    /**
     * Data type is Appearance, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_APPEARANCE(0x19),

    /**
     * Data type is Advertising Interval, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_ADVERTISING_INTERVAL(0x1A),

    /**
     * Data type is LE Bluetooth Device Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_LE_BLUETOOTH_DEVICE_ADDRESS(0x1B),

    /**
     * Data type is LE Role, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_LE_ROLE(0x1C),

    /**
     * Data type is Simple Pairing Hash C-256, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SIMPLE_PAIRING_HASH_C_256(0x1D),

    /**
     * Data type is Simple Pairing Randomizer R-256, see the Bluetooth Generic Access Profile for
     * more details.
     */
    DATA_TYPE_SIMPLE_PAIRING_RANDOMIZER_R_256(0x1E),

    /**
     * Data type is List of 32-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT(0x1F),

    /**
     * Data type is Service Data - 32-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SERVICE_DATA_32_BIT(0x20),

    /**
     * Data type is Service Data - 128-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_SERVICE_DATA_128_BIT(0x21),

    /**
     * Data type is LE Secure Connections Confirmation Value, see the Bluetooth Generic Access
     * Profile for more details.
     */
    DATA_TYPE_LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE(0x22),

    /**
     * Data type is LE Secure Connections Random Value, see the Bluetooth Generic Access Profile for
     * more details.
     */
    DATA_TYPE_LE_SECURE_CONNECTIONS_RANDOM_VALUE(0x23),

    /**
     * Data type is URI, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_URI(0x24),

    /**
     * Data type is Indoor Positioning, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_INDOOR_POSITIONING(0x25),

    /**
     * Data type is Transport Discovery Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_TRANSPORT_DISCOVERY_DATA(0x26),

    /**
     * Data type is LE Supported Features, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_LE_SUPPORTED_FEATURES(0x27),

    /**
     * Data type is Channel Map Update Indication, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_CHANNEL_MAP_UPDATE_INDICATION(0x28),

    /**
     * Data type is PB-ADV, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_PB_ADV(0x29),

    /**
     * Data type is Mesh Message, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_MESH_MESSAGE(0x2A),

    /**
     * Data type is Mesh Beacon, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_MESH_BEACON(0x2B),

    /**
     * Data type is BIGInfo, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_BIG_INFO(0x2C),

    /**
     * Data type is Broadcast_Code, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_BROADCAST_CODE(0x2D),

    /**
     * Data type is Resolvable Set Identifier, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_RESOLVABLE_SET_IDENTIFIER(0x2E),

    /**
     * Data type is Advertising Interval - long, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_ADVERTISING_INTERVAL_LONG(0x2F),

    /**
     * Data type is 3D Information Data, see the Bluetooth Generic Access Profile for more details.
     */
    DATA_TYPE_3D_INFORMATION_DATA(0x3D),

    /**
     * Data type is Manufacturer Specific Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    DATA_TYPE_MANUFACTURER_SPECIFIC_DATA(0xFF);

    companion object {

        /**
         * Creates [BleType] based on [Int] value.
         *
         * @param value [Int] value.
         * @return [BleType] or null if not found.
         */
        fun createOrNull(value: Int): BleType? {
            return values().firstOrNull { it.value == value }
        }
    }
}
