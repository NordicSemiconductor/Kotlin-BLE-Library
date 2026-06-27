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

package no.nordicsemi.kotlin.ble.android.sample.scanner.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

/**
 * A definition of the LED Button Service (LBS) profile.
 *
 * Read more: [Documentation / Peripheral LBS](https://docs.nordicsemi.com/bundle/ncs-latest/page/nrf/samples/bluetooth/peripheral_lbs/README.html)
 */
interface LedButtonProfile {
    companion object {
        /** If a button is pressed for more than this value it is reported as long press. */
        val LONG_PRESS_TIMEOUT = 2.seconds
        /** The LED Button Service UUID. */
        val SERVICE_UUID: Uuid = Uuid.parse("00001523-1212-efde-1523-785feabcd123")
        /** The UUID of the Button characteristic. */
        val BUTTON_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00001524-1212-efde-1523-785feabcd123")
        /** The UUID of the LED characteristic. */
        val LED_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00001525-1212-efde-1523-785feabcd123")
    }

    /**
     * The state of the LBS device.
     *
     * This interface represents the state of a device with LED Button Service (Blinky) device.
     */
    interface State {

        /**
         * The current state of the LED.
         *
         * Set the [value][MutableStateFlow.value] to change the LED state.
         */
        val led: MutableStateFlow<Boolean>

        /**
         * The current state of the button.
         *
         * This flow emits the current state of the button: `true` when pressed and `false` when
         * released.
         *
         * Use [buttonPressed] and [buttonLongPressed] flows to handle button events.
         * @see buttonPressed
         * @see buttonLongPressed
         */
        val button: StateFlow<Boolean>

        /**
         * The flow of button click events.
         *
         * This flow emits an event when the button is clicked.
         */
        val buttonPressed: Flow<Unit>

        /**
         * The flow of long button clicks events.
         *
         * This flow emits an event when the button is pressed for [2 seconds].
         */
        val buttonLongPressed: Flow<Unit>
    }
}