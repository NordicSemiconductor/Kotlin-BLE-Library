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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.ble.client

/**
 * An event indicating that the scanning device entered region of the monitored peripheral.
 *
 * @property peripheral The peripheral.
 */
sealed class MonitoringEvent<P: GenericPeripheral<*>>(
    val peripheral: P
)

/**
 * An event indicating that the scanning device entered region of the monitored peripheral.
 */
class PeripheralEnteredRange<P: GenericPeripheral<*>>(peripheral: P): MonitoringEvent<P>(peripheral)

/**
 * An event indicating that the scanning device left region of the monitored peripheral
 * and no advertising packets were received.
 */
class PeripheralLeftRange<P: GenericPeripheral<*>>(peripheral: P): MonitoringEvent<P>(peripheral)

/**
 * An event indicating ranging events for the peripheral.
 *
 * @property peripheral The peripheral.
 */
sealed class RangeEvent<P: GenericPeripheral<*>>(
    val peripheral: P
)

/**
 * An event indicating that the proximity of the peripheral has changed.
 *
 * Note, that the proximity is estimated based on the received signal strength indicator (RSSI)
 * and is only approximate. RSSI depends on many factors, like the distance, the obstacles,
 * the antenna orientation, and the environment.
 *
 * @property previousProximity The previous proximity.
 * @property proximity The new recorded proximity.
 */
class ProximityChanged<P: GenericPeripheral<*>>(
    peripheral: P,
    val previousProximity: Proximity,
    val proximity: Proximity
): RangeEvent<P>(peripheral) {

    /**
     * Proximity to the peripheral.
     *
     * This is estimated based on the received signal strength indicator (RSSI) and is only
     * approximate.
     */
    enum class Proximity {
        /** The proximity of the beacon could not be determined. */
        UNKNOWN,
        /** The beacon is in the user’s immediate vicinity. */
        IMMEDIATE,
        /** The beacon is relatively close to the user. */
        NEAR,
        /** The beacon is far away. */
        FAR,
    }
}