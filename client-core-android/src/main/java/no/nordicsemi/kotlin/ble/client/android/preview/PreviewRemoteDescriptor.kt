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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.client.android.preview

import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.core.util.fromShortUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A remote descriptor that can be used for compose previews.
 *
 * If has no-op implementation.
 */
@OptIn(ExperimentalUuidApi::class)
class PreviewRemoteDescriptor: RemoteDescriptor {
    override val characteristic: RemoteCharacteristic
    override val uuid: Uuid
    override val instanceId: Int

    internal constructor(
        characteristic: RemoteCharacteristic,
        uuid: Uuid,
        instanceId: Int = 0,
    ) {
        this.characteristic = characteristic
        this.uuid = uuid
        this.instanceId = instanceId
    }

    /**
     * Creates a preview descriptor.
     *
     * @param shortUuid A 16 or 32 bit short UUID assigned by Bluetooth SIG.
     * @param instanceId The instance ID of the descriptor.
     */
    constructor(
        shortUuid: Int,
        instanceId: Int = 0,
    ): this(Uuid.fromShortUuid(shortUuid), instanceId)

    /**
     * Creates a preview descriptor.
     *
     * @param uuid The UUID of the descriptor.
     * @param instanceId The instance ID of the descriptor.
     */
    constructor(
        uuid: Uuid,
        instanceId: Int = 0,
    ) {
        // The point of this constructor is to create a descriptor with a parent characteristic
        // and service, with this descriptor instance as a child.
        this.characteristic = PreviewRemoteService(Uuid.random(), 0, this).characteristic!!
        this.uuid = uuid
        this.instanceId = instanceId
    }

    override suspend fun read(): ByteArray = byteArrayOf()

    override suspend fun write(data: ByteArray) {}
}