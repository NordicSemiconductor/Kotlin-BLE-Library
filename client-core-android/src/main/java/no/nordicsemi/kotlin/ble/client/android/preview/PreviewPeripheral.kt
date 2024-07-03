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

package no.nordicsemi.kotlin.ble.client.android.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.RssiRead
import no.nordicsemi.kotlin.ble.client.ServicesChanged
import no.nordicsemi.kotlin.ble.client.android.ConnectionParametersChanged
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.MtuChanged
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.client.android.PhyChanged
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.ConnectionParameters
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.PhyOption
import no.nordicsemi.kotlin.ble.core.Service
import no.nordicsemi.kotlin.ble.core.WriteType
import java.util.UUID

/**
 * A stub implementation of [Peripheral.Executor] for Android.
 *
 * It does not depend on any Android API and can be used to preview the UI in the Compose Preview.
 *
 * The stub implementation provides some mocking functionality, for example [connect]
 * immediately changes the connection state to [ConnectionState.Connected] and [disconnect]
 * to [ConnectionState.Disconnected], etc.
 *
 * @param identifier The MAC address of the peripheral.
 * @param name An optional name of the peripheral.
 * @param type The type of the peripheral, defaults to [PeripheralType.LE].
 * @param initialState The initial connection state of the peripheral.
 * @param rssi The signal strength of the peripheral in dBm.
 * @param hasBondInformation `true` if the Android device has the bond information for the peripheral,
 * that is, if the peripheral is bonded to the device.
 */
private class StubExecutor(
    override val identifier: String,
    override val name: String?,
    override val type: PeripheralType,
    override val initialState: ConnectionState,
    override val initialServices: List<StubRemoteService>,
    private val rssi: Int,
    private val phy: PhyInUse,
    hasBondInformation: Boolean,
): Peripheral.Executor {
    private val _events = MutableSharedFlow<GattEvent>(replay = 1)
    override val events: Flow<GattEvent> = _events.asSharedFlow()

    private val _bondState = MutableStateFlow(if (hasBondInformation) BondState.BONDED else BondState.NONE)
    override val bondState: StateFlow<BondState> = _bondState.asStateFlow()

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Connected))
    }

    override fun discoverServices() {
        _events.tryEmit(ServicesChanged(initialServices))
    }

    override fun requestConnectionPriority(priority: ConnectionPriority) {
        _events.tryEmit(ConnectionParametersChanged(ConnectionParameters.Connected(15, 0, 0)))
    }

    override fun requestMtu(mtu: Int) {
        _events.tryEmit(MtuChanged(mtu))
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption) {
        _events.tryEmit(PhyChanged(PhyInUse(txPhy, rxPhy)))
    }

    override fun readPhy() {
        _events.tryEmit(PhyChanged(phy))
    }

    override fun readRssi() {
        _events.tryEmit(RssiRead(rssi))
    }

    override fun disconnect() {
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Disconnected()))
    }

    override fun close() {
        // Do nothing
    }
}

/**
 * A stub implementation of [RemoteService] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteService internal constructor(
    override val uuid: UUID,
    override val instanceId: Int = 0,
    includedServices: List<InnerServiceBuilder> = emptyList(),
    characteristics: List<StubRemoteCharacteristic.Builder> = emptyList(),
): RemoteService {
    override lateinit var owner: PreviewPeripheral

    class Builder(
        val uuid: UUID,
        val instanceId: Int = 0,
        val includedServices: List<InnerServiceBuilder> = emptyList(),
        val characteristics: List<StubRemoteCharacteristic.Builder> = emptyList(),
    )

    class InnerServiceBuilder(
        val uuid: UUID,
        val instanceId: Int = 0,
        val characteristics: List<StubRemoteCharacteristic.Builder> = emptyList(),
    )

    override val characteristics: List<StubRemoteCharacteristic> = characteristics
        .map { cb ->
            StubRemoteCharacteristic(
                service = this,
                uuid = cb.uuid,
                instanceId = cb.instanceId,
                initialValue = cb.initialValue,
                descriptors = cb.descriptors,
            )
        }

    override val includedServices: List<Service<RemoteCharacteristic>> = includedServices
        .map { sb ->
            StubRemoteService(
                uuid = sb.uuid,
                instanceId = sb.instanceId,
                characteristics = sb.characteristics,
            )
        }
}

/**
 * A stub implementation of [RemoteCharacteristic] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteCharacteristic internal constructor(
    override val service: RemoteService,
    override val uuid: UUID,
    override val instanceId: Int = 0,
    initialValue: ByteArray = byteArrayOf(),
    override val properties: List<CharacteristicProperty> = emptyList(),
    descriptors: List<StubRemoteDescriptor.Builder> = emptyList(),
): RemoteCharacteristic {

    class Builder(
        val uuid: UUID,
        val instanceId: Int = 0,
        val initialValue: ByteArray = byteArrayOf(),
        val descriptors: List<StubRemoteDescriptor.Builder> = emptyList(),
    )

    override val descriptors: List<RemoteDescriptor> = descriptors
        .map { db ->
            StubRemoteDescriptor(
                characteristic = this,
                uuid = db.uuid,
                instanceId = db.instanceId,
                initialValue = db.initialValue
            )
        }

    private val _value = MutableStateFlow(initialValue)

    override suspend fun read(): ByteArray = _value.value

    override suspend fun write(data: ByteArray, writeType: WriteType) {
        _value.update { data }
    }

    override suspend fun subscribe(): Flow<ByteArray> = _value.asStateFlow()

    override suspend fun waitForValueChange(): ByteArray = _value.first()
}

/**
 * A stub implementation of [RemoteDescriptor] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteDescriptor internal constructor(
    override val characteristic: RemoteCharacteristic,
    override val uuid: UUID,
    override val instanceId: Int = 0,
    private var initialValue: ByteArray,
): RemoteDescriptor {

    class Builder(
        val uuid: UUID,
        val instanceId: Int = 0,
        val initialValue: ByteArray = byteArrayOf(),
    )

    override suspend fun read(): ByteArray = initialValue

    override suspend fun write(data: ByteArray) {
        initialValue = data
    }
}

/**
 * A preview implementation of [Peripheral] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 *
 * @param scope The coroutine scope. This can be set to `rememberCoroutineScope()`.
 * @param address The MAC address of the peripheral.
 * @param name An optional name of the peripheral.
 * @param type The type of the peripheral, defaults to [PeripheralType.LE].
 * @param rssi The signal strength of the peripheral in dBm.
 * @param state The connection state of the peripheral.
 * @param services The list of fake services discovered on the peripheral.
 * @param hasBondInformation `true` if the Android device has the bond information for the peripheral,
 * that is, if the peripheral is bonded to the device. Defaults to `false`.
 */
open class PreviewPeripheral(
    scope: CoroutineScope,
    address: String = "00:11:22:33:44:55",
    name: String? = "My Device",
    type: PeripheralType = PeripheralType.LE,
    rssi: Int = -40, // dBm
    phy: PhyInUse = PhyInUse.LE_1M,
    state: ConnectionState = ConnectionState.Disconnected(),
    services: List<StubRemoteService.Builder> = when (state) {
        ConnectionState.Connected ->
            listOf(
                StubRemoteService.Builder(
                    uuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"),
                    characteristics = emptyList()
                )
            )
        else -> emptyList()
    },
    hasBondInformation: Boolean = false
): Peripheral(
    scope = scope,
    impl = StubExecutor(
        identifier = address,
        name = name,
        type = type,
        initialState = state,
        initialServices = services.map {
            StubRemoteService(
                uuid = it.uuid,
                instanceId = it.instanceId,
                characteristics = it.characteristics
            )
        },
        rssi = rssi,
        phy = phy,
        hasBondInformation = hasBondInformation
    )
) {
    // TODO assign this as services owner

    override fun toString(): String {
        return name ?: address
    }
}