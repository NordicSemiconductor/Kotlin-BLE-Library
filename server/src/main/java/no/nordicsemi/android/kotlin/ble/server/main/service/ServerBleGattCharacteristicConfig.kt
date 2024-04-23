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

package no.nordicsemi.android.kotlin.ble.server.main.service

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import java.util.UUID

/**
 * A configuration class which is used as a prescription to create [IBluetoothGattCharacteristic].
 *
 * @property uuid [UUID] of a characteristic being created.
 * @property properties Properties of a characteristic being created.
 * @property permissions Permissions of a characteristic being created.
 * @property descriptorConfigs Descriptor configs of a characteristic being created.
 * @property initialValue Initial value of a characteristic being created.
 */
data class ServerBleGattCharacteristicConfig(
    val uuid: UUID,
    val properties: List<BleGattProperty> = emptyList(),
    val permissions: List<BleGattPermission> = emptyList(),
    val descriptorConfigs: List<ServerBleGattDescriptorConfig> = emptyList(),
    val initialValue: DataByteArray? = null
) {

    /**
     * Helper property telling if the characteristic will have indication/notification feature.
     */
    val hasNotifications: Boolean
        get() = properties.contains(BleGattProperty.PROPERTY_NOTIFY) or properties.contains(BleGattProperty.PROPERTY_INDICATE)
}
