/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.server

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.service.BleGattServerServiceType
import no.nordicsemi.android.kotlin.ble.server.service.BleServerGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.service.BleServerGattServiceConfig
import java.util.*
import javax.inject.Inject

object BlinkySpecifications {
    /** Nordic Blinky Service UUID. */
    val UUID_SERVICE_DEVICE: UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123")

    /** LED characteristic UUID. */
    val UUID_LED_CHAR: UUID by lazy { UUID.fromString("00001525-1212-efde-1523-785feabcd123") }

    /** BUTTON characteristic UUID. */
    val UUID_BUTTON_CHAR: UUID by lazy { UUID.fromString("00001524-1212-efde-1523-785feabcd123") }
}

@SuppressLint("MissingPermission")
@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ViewModel() {

    init {
        val server = BleGattServer()

        val ledCharacteristic = BleServerGattCharacteristicConfig(
            BlinkySpecifications.UUID_LED_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)
        )

        val buttonCharacteristic = BleServerGattCharacteristicConfig(
            BlinkySpecifications.UUID_BUTTON_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_NOTIFY),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)
        )

        val serviceConfig = BleServerGattServiceConfig(
            BlinkySpecifications.UUID_SERVICE_DEVICE,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(ledCharacteristic, buttonCharacteristic)
        )

        server.start(context, serviceConfig)
    }
}
