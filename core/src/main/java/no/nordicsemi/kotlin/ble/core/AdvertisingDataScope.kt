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

package no.nordicsemi.kotlin.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Common scope for building advertising data.
 */
@OptIn(ExperimentalUuidApi::class)
interface AdvertisingDataScope {

    /**
     * Adds the service UUID to the advertising data, which will be added to the AD field
     * "Complete List or X bit Service UUIDs", depending on the UUID length.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param uuid The 128-bit service UUID.
     */
    @Suppress("FunctionName")
    fun ServiceUuid(uuid: Uuid)

    /**
     * Adds the service UUID to the advertising data, which will be added to the AD field
     * "Complete List or X bit Service UUIDs", depending on the UUID length.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param shortUuid The 16-bit or 32-bit service UUID.
     */
    @Suppress("FunctionName")
    fun ServiceUuid(shortUuid: Int)
}