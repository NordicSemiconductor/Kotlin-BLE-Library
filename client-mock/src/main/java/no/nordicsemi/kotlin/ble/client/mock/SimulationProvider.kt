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

package no.nordicsemi.kotlin.ble.client.mock

import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException

/**
 * This class allows simulating events which would normally be emitted by the real central manager.
 *
 * @property ID The type of the peripheral identifier.
 */
interface SimulationProvider<ID> {

    /**
     * Simulates turning on Bluetooth adapter on the device.
     *
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is not supported on the device.
     */
    fun simulatePowerOn()

    /**
     * Simulates turning off Bluetooth adapter on the device.
     *
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is not supported on the device.
     */
    fun simulatePowerOff()

    /**
     * Sets a list of simulated peripherals.
     *
     * Peripherals added using this method will be available for scanning and connecting using
     * mock central managers, depending on their proximity.
     * Use [PeripheralSpec.simulateProximityChange] to modify proximity.
     */
    fun simulatePeripherals(peripherals: List<PeripheralSpec<ID>>)

    /**
     * Removes all active central manager instances and peripherals from the simulation.
     *
     * All managers will be closed and their state will change to [Manager.State.UNKNOWN].
     */
    fun tearDownSimulation()
}