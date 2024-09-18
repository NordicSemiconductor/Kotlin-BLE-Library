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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.RemoteIncludedService
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.RssiRead
import no.nordicsemi.kotlin.ble.client.ServicesChanged
import no.nordicsemi.kotlin.ble.client.ServicesDiscovered
import no.nordicsemi.kotlin.ble.client.android.ConnectionParametersChanged
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.MtuChanged
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.client.android.PhyChanged
import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.Characteristic
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.ConnectionParameters
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.Permission
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.PhyOption
import no.nordicsemi.kotlin.ble.core.ServerScope
import no.nordicsemi.kotlin.ble.core.Service
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.internal.CharacteristicDefinition
import no.nordicsemi.kotlin.ble.core.internal.DescriptorDefinition
import no.nordicsemi.kotlin.ble.core.internal.ServerScopeImpl
import no.nordicsemi.kotlin.ble.core.internal.ServiceDefinition
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
    override val events: SharedFlow<GattEvent> = _events.asSharedFlow()

    private val _bondState = MutableStateFlow(if (hasBondInformation) BondState.BONDED else BondState.NONE)
    override val bondState: StateFlow<BondState> = _bondState.asStateFlow()

    override val isClosed: Boolean
        get() = false

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Connected))
    }

    override fun discoverServices(): Boolean {
        _events.tryEmit(ServicesDiscovered(initialServices))
        return true
    }

    override fun createBond(): Boolean {
        _bondState.tryEmit(BondState.BONDED)
        return true
    }

    override fun removeBond(): Boolean {
        _bondState.tryEmit(BondState.NONE)
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.TerminateLocalHost)))
        return true
    }

    override fun refreshCache(): Boolean {
        _events.tryEmit(ServicesChanged)
        return true
    }

    override fun requestConnectionPriority(priority: ConnectionPriority): Boolean {
        _events.tryEmit(ConnectionParametersChanged(ConnectionParameters.Connected(15, 0, 0)))
        return true
    }

    override fun requestMtu(mtu: Int): Boolean {
        _events.tryEmit(MtuChanged(mtu))
        return true
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption): Boolean {
        _events.tryEmit(PhyChanged(PhyInUse(txPhy, rxPhy)))
        return true
    }

    override fun readPhy(): Boolean {
        _events.tryEmit(PhyChanged(phy))
        return true
    }

    override fun readRssi(): Boolean {
        _events.tryEmit(RssiRead(rssi))
        return true
    }

    override fun disconnect(): Boolean {
        _events.tryEmit(
            ConnectionStateChanged(
                ConnectionState.Disconnected(
                    reason = ConnectionState.Disconnected.Reason.Success
                )
            )
        )
        return true
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
    includedServices: List<ServiceDefinition> = emptyList(),
    characteristics: List<CharacteristicDefinition> = emptyList(),
): RemoteService() {

    override val characteristics: List<StubRemoteCharacteristic> = characteristics
        .mapIndexed { index, cd ->
            StubRemoteCharacteristic(
                service = this,
                uuid = cd.uuid,
                instanceId = index,
                properties = cd.properties,
                permissions = cd.permissions,
                descriptors = cd.descriptors,
            )
        }

    override val includedServices: List<RemoteIncludedService> = includedServices
        .mapIndexed { index, sd ->
            StubRemoteIncludedService(
                service = this,
                uuid = sd.uuid,
                instanceId = index,
                characteristics = sd.characteristics,
                includedServices = sd.innerServices,
            )
        }

    override fun toString(): String = uuid.toString()
}

/**
 * A stub implementation of [RemoteIncludedService] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteIncludedService internal constructor(
    override val service: AnyRemoteService,
    override val uuid: UUID,
    override val instanceId: Int = 0,
    characteristics: List<CharacteristicDefinition> = emptyList(),
    includedServices: List<ServiceDefinition> = emptyList(),
): RemoteIncludedService {

    override val characteristics: List<RemoteCharacteristic> = characteristics
        .mapIndexed { index, cd ->
            StubRemoteCharacteristic(
                service = this,
                uuid = cd.uuid,
                instanceId = index,
                properties = cd.properties,
                permissions = cd.permissions,
                descriptors = cd.descriptors,
            )
        }

    override val includedServices: List<RemoteIncludedService> = includedServices
        .mapIndexed { index, sd ->
            StubRemoteIncludedService(
                service = this,
                uuid = sd.uuid,
                instanceId = index,
                characteristics = sd.characteristics,
                includedServices = sd.innerServices,
            )
        }

    override fun toString(): String = uuid.toString()
}

/**
 * A stub implementation of [RemoteCharacteristic] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteCharacteristic internal constructor(
    override val service: AnyRemoteService,
    override val uuid: UUID,
    override val instanceId: Int,
    override val properties: List<CharacteristicProperty> = emptyList(),
    private val permissions: List<Permission>,
    descriptors: List<DescriptorDefinition> = emptyList(),
): RemoteCharacteristic {
    private var _isNotifying = false

    override val isNotifying: Boolean
        get() = _isNotifying &&
                (CharacteristicProperty.NOTIFY in properties || CharacteristicProperty.INDICATE in properties)

    override suspend fun setNotifying(enabled: Boolean) = when {
        owner == null -> throw InvalidAttributeException()
        properties
            .intersect(listOf(CharacteristicProperty.NOTIFY, CharacteristicProperty.INDICATE))
            .isEmpty() -> throw OperationFailedException(OperationStatus.SUBSCRIBE_NOT_PERMITTED)
        else -> _isNotifying = enabled
    }

    override val descriptors: List<RemoteDescriptor> = descriptors
        .mapIndexed { index, dd ->
            StubRemoteDescriptor(
                characteristic = this,
                uuid = dd.uuid,
                instanceId = index,
                permissions = dd.permissions,
            )
        }

    private val _value = MutableStateFlow(byteArrayOf())

    override suspend fun read(): ByteArray = when {
        owner == null -> throw InvalidAttributeException()
        permissions
            .intersect(listOf(Permission.READ, Permission.READ_ENCRYPTED, Permission.READ_ENCRYPTED_MITM))
            .isNotEmpty() -> _value.value
        else -> throw OperationFailedException(OperationStatus.READ_NOT_PERMITTED)
    }

    override suspend fun write(data: ByteArray, writeType: WriteType) = when {
        owner == null -> throw InvalidAttributeException()
        permissions
            .intersect(listOf(Permission.WRITE, Permission.WRITE_ENCRYPTED, Permission.WRITE_ENCRYPTED_MITM))
            .isNotEmpty() -> _value.update { data }
        else -> throw OperationFailedException(OperationStatus.WRITE_NOT_PERMITTED)
    }

    override suspend fun subscribe(): Flow<ByteArray> = when {
        owner == null -> throw InvalidAttributeException()
        properties
            .intersect(listOf(CharacteristicProperty.NOTIFY, CharacteristicProperty.INDICATE))
            .isNotEmpty() -> _value.filter { _isNotifying }
        else -> throw OperationFailedException(OperationStatus.SUBSCRIBE_NOT_PERMITTED)
    }

    override suspend fun waitForValueChange(): ByteArray = subscribe().first()

    override fun toString(): String = uuid.toString()
}

/**
 * A stub implementation of [RemoteDescriptor] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 */
class StubRemoteDescriptor internal constructor(
    override val characteristic: RemoteCharacteristic,
    override val uuid: UUID,
    override val instanceId: Int,
    private val permissions: List<Permission>,
): RemoteDescriptor {
    private var value: ByteArray = byteArrayOf()

    override suspend fun read(): ByteArray = when {
        owner == null -> throw InvalidAttributeException()
        permissions
            .intersect(listOf(Permission.READ, Permission.READ_ENCRYPTED, Permission.READ_ENCRYPTED_MITM))
            .isNotEmpty() -> value
        else -> throw OperationFailedException(OperationStatus.READ_NOT_PERMITTED)
    }

    override suspend fun write(data: ByteArray) = when {
        owner == null -> throw InvalidAttributeException()
        permissions
            .intersect(listOf(Permission.WRITE, Permission.WRITE_ENCRYPTED, Permission.WRITE_ENCRYPTED_MITM))
            .isNotEmpty() -> value = data
        else -> throw OperationFailedException(OperationStatus.WRITE_NOT_PERMITTED)
    }

    override fun toString(): String = uuid.toString()
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
    phy: PhyInUse = PhyInUse.PHY_LE_1M,
    state: ConnectionState = ConnectionState.Closed,
    services: ServerScope.() -> Unit = {
        Service(Service.GENERIC_ACCESS_UUID) {
            Characteristic(
                uuid = Characteristic.DEVICE_NAME,
                property = CharacteristicProperty.READ,
                permission = Permission.READ,
            )
            Characteristic(
                uuid = Characteristic.APPEARANCE,
                property = CharacteristicProperty.READ,
                permission = Permission.READ,
            )
            Characteristic(
                uuid = Characteristic.PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS,
                property = CharacteristicProperty.READ,
                permission = Permission.READ,
            )
        }
        Service(Service.GENERIC_ATTRIBUTE_UUID) {
            Characteristic(
                uuid = Characteristic.SERVICE_CHANGED,
                property = CharacteristicProperty.INDICATE,
                permission = Permission.READ,
            )
        }
    },
    hasBondInformation: Boolean = false
): Peripheral(
    scope = scope,
    impl = StubExecutor(
        identifier = address,
        name = name,
        type = type,
        initialState = state,
        initialServices = ServerScopeImpl()
            .apply(services)
            .services.mapIndexed { index, sd ->
                StubRemoteService(
                    uuid = sd.uuid,
                    instanceId = index,
                    characteristics = sd.characteristics,
                    includedServices = sd.innerServices,
                )
            },
        rssi = rssi,
        phy = phy,
        hasBondInformation = hasBondInformation
    )
) {
    override fun toString(): String {
        return name ?: address
    }
}