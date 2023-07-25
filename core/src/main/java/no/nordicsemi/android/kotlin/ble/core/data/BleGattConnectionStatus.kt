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

package no.nordicsemi.android.kotlin.ble.core.data

/**
 * Possible statuses of BLE connection.
 *
 * @property value Native Android API value.
 */
enum class BleGattConnectionStatus(internal val value: Int) {

    /**
     * Unknown error.
     */
    UNKNOWN(-1),

    /** The disconnection was initiated by the user.  */
    SUCCESS(0),

    /** The local device initiated disconnection.  */
    TERMINATE_LOCAL_HOST(1),

    /** The remote device initiated graceful disconnection.  */
    TERMINATE_PEER_USER(2),

    /**
     * This reason will only be reported when [ConnectRequest.useAutoConnect]} was
     * called with parameter set to true, and connection to the device was lost for any reason
     * other than graceful disconnection initiated by the peer user.
     *
     * Android will try to reconnect automatically.
     */
    LINK_LOSS(3),

    /** The device does not have required services.  */
    NOT_SUPPORTED(4),

    /** Connection attempt was cancelled.  */
    CANCELLED(5),

    /**
     * The connection timed out. The device might have reboot, is out of range, turned off
     * or doesn't respond for another reason.
     */
    TIMEOUT(10);

    val isLinkLoss
        get() = this != SUCCESS && this != TERMINATE_PEER_USER

    companion object {
        fun create(value: Int): BleGattConnectionStatus {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
