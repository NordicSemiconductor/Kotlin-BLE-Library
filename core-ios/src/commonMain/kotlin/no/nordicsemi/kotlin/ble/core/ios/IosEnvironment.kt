/*
 * Copyright (c) 2026, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.core.ios

import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.core.Environment
import no.nordicsemi.kotlin.ble.core.Manager

/**
 * iOS-specific environment interface for Bluetooth LE operations.
 *
 * This interface extends the base [Environment] with iOS-specific properties
 * based on CoreBluetooth framework capabilities.
 *
 * On iOS, there are no separate scan/connect/advertise permissions like on Android.
 * Instead, the app needs a single Bluetooth authorization managed by the system.
 *
 * @property bluetoothState A flow emitting the current Bluetooth state,
 * derived from `CBManagerState`.
 * @property authorizationStatus The current CoreBluetooth authorization status.
 */
interface IosEnvironment : Environment {

    /**
     * CoreBluetooth authorization statuses.
     *
     * Maps to `CBManagerAuthorization` values.
     */
    enum class AuthorizationStatus {
        /**
         * The user has not yet made a choice regarding whether the app may use Bluetooth.
         */
        NOT_DETERMINED,

        /**
         * The app is not authorized to use Bluetooth.
         * This can be due to restrictions (e.g., parental controls).
         */
        RESTRICTED,

        /**
         * The user explicitly denied Bluetooth access for this app.
         */
        DENIED,

        /**
         * The user has authorized Bluetooth access for this app.
         */
        ALLOWED,
    }

    /**
     * A flow emitting the current Bluetooth adapter state.
     */
    val bluetoothState: StateFlow<Manager.State>

    override val isBluetoothEnabled: Boolean
        get() = bluetoothState.value == Manager.State.POWERED_ON

    /**
     * A flow emitting the current CoreBluetooth authorization status.
     */
    val authorizationStatus: StateFlow<AuthorizationStatus>

    /**
     * Whether the app is authorized to use Bluetooth.
     */
    val isAuthorized: Boolean
        get() = authorizationStatus.value == AuthorizationStatus.ALLOWED

    /**
     * Closes the environment and releases underlying CoreBluetooth resources.
     */
    fun close()
}
