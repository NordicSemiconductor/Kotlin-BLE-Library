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

package no.nordicsemi.kotlin.ble.client.preview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.GenericPeripheral
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.WriteType
import java.util.UUID

/**
 * A preview peripheral allows to display peripherals in view previews.
 *
 * The don't have any implementation and can only be used to show a name or an identifier.
 *
 * @param currentState The initial state of the peripheral. It can be changed to
 * [ConnectionState.Disconnected] using [disconnect].
 */
open class PreviewPeripheral<ID>(
    override val identifier: ID,
    override val name: String?,
    currentState: ConnectionState = ConnectionState.Disconnected(),
    private val maximumWriteValueLength: Int = 23,
    private val rssi: Int = -40,
) : GenericPeripheral<ID> {
    private val _state = MutableStateFlow(currentState)
    override val state: StateFlow<ConnectionState> = _state.asStateFlow()

    override fun services(uuids: List<UUID>): StateFlow<List<RemoteService>> = MutableStateFlow(emptyList())

    override fun maximumWriteValueLength(type: WriteType): Int = maximumWriteValueLength - 3

    override suspend fun readRssi(): Int = rssi

    override suspend fun disconnect() {
        _state.update { ConnectionState.Disconnected() }
    }
}