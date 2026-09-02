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
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.kotlin.ble.client.mock.internal

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import no.nordicsemi.kotlin.ble.client.mock.WriteResponse
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.OperationStatus

class CccdStateTest {
    @Test
    fun `initial dual-property state retains notification-first mock behavior`() {
        val state = CccdState(enabled = true, properties = dualProperties)

        assertContentEquals(notificationValue, state.value)
        assertTrue(state.isEnabled)
    }

    @Test
    fun `notification and indication values are stored exactly`() {
        val state = CccdState(enabled = false, properties = dualProperties)

        assertEquals(WriteResponse.Success, state.write(indicationValue))
        assertContentEquals(indicationValue, state.value)
        assertEquals(WriteResponse.Success, state.write(notificationValue))
        assertContentEquals(notificationValue, state.value)
    }

    @Test
    fun `unsupported mode fails without changing the current value`() {
        val state = CccdState(
            enabled = false,
            properties = setOf(CharacteristicProperty.NOTIFY),
        )

        assertEquals(
            WriteResponse.Failure(OperationStatus.ValueNotAllowed),
            state.write(indicationValue),
        )
        assertContentEquals(disabledValue, state.value)
        assertFalse(state.isEnabled)
    }

    @Test
    fun `peripheral rejection is preserved without changing the current value`() {
        val state = CccdState(enabled = false, properties = dualProperties)
        val rejection = WriteResponse.Failure(
            OperationStatus.ClientCharacteristicConfigurationDescriptorImproperlyConfigured,
        )

        assertEquals(rejection, state.write(indicationValue, rejection))
        assertContentEquals(disabledValue, state.value)
        assertFalse(state.isEnabled)
    }

    @Test
    fun `disable value clears an active subscription`() {
        val state = CccdState(enabled = true, properties = dualProperties)

        assertEquals(WriteResponse.Success, state.write(disabledValue))

        assertContentEquals(disabledValue, state.value)
        assertFalse(state.isEnabled)
    }

    private companion object {
        val dualProperties = setOf(
            CharacteristicProperty.NOTIFY,
            CharacteristicProperty.INDICATE,
        )
        val disabledValue = byteArrayOf(0x00, 0x00)
        val notificationValue = byteArrayOf(0x01, 0x00)
        val indicationValue = byteArrayOf(0x02, 0x00)
    }
}
