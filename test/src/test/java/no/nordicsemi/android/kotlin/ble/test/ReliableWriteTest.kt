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

package no.nordicsemi.android.kotlin.ble.test

import android.content.Context
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ReliableWriteTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @RelaxedMockK
    lateinit var context: Context

    @Inject
    lateinit var serverDevice: MockServerDevice

    @Inject
    lateinit var clientDevice: MockClientDevice

    @Inject
    lateinit var server: ReliableWriteServerProvider

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun release() {
        Dispatchers.resetMain()
    }

    @Before
    fun before() {
        runBlocking {
            server.start(context, serverDevice)
        }
    }

    @Test
    fun `when connected should return state connected`() = runTest {
        val connectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_CONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        val client = ClientBleGatt.connect(context, serverDevice, scope = scope)

        assertEquals(connectedState, client.connectionStateWithStatus.value)
    }

    @Test
    fun `when connected should return state disconnected`() = runTest {
        val connectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_DISCONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        val client = ClientBleGatt.connect(context, serverDevice, scope = scope)

        server.stopServer()

        assertEquals(connectedState, client.connectionStateWithStatus.value)
    }

    @Test
    fun `when reliable aborted should return previous value`() = runTest {
        val client: ClientBleGatt = ClientBleGatt.connect(context, serverDevice, scope = scope)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)

        firstCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(DataByteArray.from(0x02))

        client.abortReliableWrite()

        assertEquals(initValue, firstCharacteristic.read())
    }

    @Test
    fun `when reliable aborted should return previous value on each characteristic`() = runTest {
        val client: ClientBleGatt = ClientBleGatt.connect(context, serverDevice, scope = scope)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        val secondCharacteristic = theService.findCharacteristic(SECOND_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)

        firstCharacteristic.write(initValue)
        secondCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(DataByteArray.from(0x02))
        secondCharacteristic.write(DataByteArray.from(0x02))

        client.abortReliableWrite()

        assertEquals(initValue, firstCharacteristic.read())
        assertEquals(initValue, secondCharacteristic.read())
    }

    @Test
    fun `when reliable executed should return new value`() = runTest {
        val client: ClientBleGatt = ClientBleGatt.connect(context, serverDevice, scope = scope)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)
        val newValue = DataByteArray.from(0x02)

        firstCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(newValue)

        client.executeReliableWrite()

        assertEquals(newValue, firstCharacteristic.read())
    }

    @Test
    fun `when reliable executed should return new value on each characteristic`() = runTest {
        val client: ClientBleGatt = ClientBleGatt.connect(context, serverDevice, scope = scope)

        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        val secondCharacteristic = theService.findCharacteristic(SECOND_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)
        val newValue = DataByteArray.from(0x02)

        firstCharacteristic.write(initValue)
        secondCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(newValue)
        secondCharacteristic.write(newValue)

        client.executeReliableWrite()

        val firstReadValue = firstCharacteristic.read()
        val secondReadValue = secondCharacteristic.read()

        assertEquals(newValue, firstReadValue)
        assertEquals(newValue, secondReadValue)
    }
}
