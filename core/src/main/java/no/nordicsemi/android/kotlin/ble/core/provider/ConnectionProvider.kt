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

package no.nordicsemi.android.kotlin.ble.core.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.core.data.Mtu

/**
 * Provides a connection parameters.
 *
 * MTU and connection state is shared between many components. To avoid propagating those values changes to
 * all of the components, the [ConnectionProvider] is shared in a constructor.
 *
 */
class ConnectionProvider(val bufferSize: Int) {

    private val _mtu = MutableStateFlow(Mtu.min)

    /**
     * Returns last observed [GattConnectionState] with it's corresponding status [BleGattConnectionStatus].
     */
    val connectionStateWithStatus = MutableStateFlow<GattConnectionStateWithStatus?>(null)

    /**
     * Returns last [GattConnectionState] without it's status.
     */
    val connectionState = connectionStateWithStatus.mapNotNull { it?.state }

    /**
     * Returns whether a device is connected.
     */
    val isConnected
        get() = connectionStateWithStatus.value?.state == GattConnectionState.STATE_CONNECTED

    /**
     * Most recent MTU value.
     */
    val mtu = _mtu.asStateFlow()

    /**
     * Updates MTU value and notifies observers.
     *
     * @param mtu New MTU value.
     */
    fun updateMtu(mtu: Int) {
        _mtu.value = mtu
    }

    /**
     * Calculates available size for write operation when a particular [writeType] is going to be
     * used.
     *
     * @param writeType Selected write type.
     * @return Available space for value in write operation in bytes.
     */
    fun availableMtu(writeType: BleWriteType): Int {
        return when (writeType) {
            BleWriteType.DEFAULT -> mtu.value - Mtu.defaultWrite
            BleWriteType.NO_RESPONSE -> mtu.value - Mtu.defaultWrite
            BleWriteType.SIGNED -> mtu.value - Mtu.signedWrite
        }
    }
}
