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

package no.nordicsemi.kotlin.ble.advertiser.exception

import kotlin.time.Duration

/**
 * Base advertiser interface.
 */
interface GenericBluetoothLeAdvertiser<
        P: GenericBluetoothLeAdvertiser.Parameters,
        D: GenericBluetoothLeAdvertiser.Payload
> {
    /**
     * Starts Bluetooth LE advertising using given parameters.
     *
     * @param parameters Advertising parameters describing how the data are to be advertised.
     * @param payload Advertising data to be broadcast.
     * @param timeout The advertising time limit. By default there is no timeout set.
     * @param block A block that will be called when the advertising is started. The block will
     * receive the actual TX power (in dBm) used for advertising.
     * @throws SecurityException If the required permission is denied.
     * @throws AdvertisingNotStartedException If the advertising could not be started.
     * @throws ValidationException If the advertising data is invalid.
     */
    suspend fun advertise(
        parameters: P,
        payload: D,
        timeout: Duration = Duration.INFINITE,
        block: ((txPower: Int) -> Unit)? = null,
    )

    /**
     * Advertising set parameters define how the data should be advertised.
     */
    interface Parameters

    /**
     * Base class for the advertising data.
     *
     * Different OSes may allow different types of data to be advertised.
     */
    interface Payload
}