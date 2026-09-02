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

package no.nordicsemi.kotlin.ble.client.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.RemoteIncludedService
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.SubscriptionMode
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.client.setNotifying
import no.nordicsemi.kotlin.ble.client.subscribe
import no.nordicsemi.kotlin.ble.client.waitForValueChange
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Environment
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.util.MergeResult
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SubscriptionModeTest {
    @Test
    fun automaticModeRetainsIndicationPriorityWhenBothPropertiesExist() {
        val mode = resolveSubscriptionMode(SubscriptionMode.AUTOMATIC, dualProperties)

        assertEquals(SubscriptionMode.INDICATION, mode)
        assertContentEquals(byteArrayOf(0x02, 0x00), cccdValue(enabled = true, mode))
    }

    @Test
    fun notificationModeSelectsNotificationWhenBothPropertiesExist() {
        val mode = resolveSubscriptionMode(SubscriptionMode.NOTIFICATION, dualProperties)

        assertEquals(SubscriptionMode.NOTIFICATION, mode)
        assertContentEquals(byteArrayOf(0x01, 0x00), cccdValue(enabled = true, mode))
    }

    @Test
    fun indicationModeSelectsIndicationWhenBothPropertiesExist() {
        val mode = resolveSubscriptionMode(SubscriptionMode.INDICATION, dualProperties)

        assertEquals(SubscriptionMode.INDICATION, mode)
        assertContentEquals(byteArrayOf(0x02, 0x00), cccdValue(enabled = true, mode))
    }

    @Test
    fun automaticModeSelectsTheOnlyAdvertisedProperty() {
        assertEquals(
            SubscriptionMode.NOTIFICATION,
            resolveSubscriptionMode(
                SubscriptionMode.AUTOMATIC,
                setOf(CharacteristicProperty.NOTIFY),
            ),
        )
        assertEquals(
            SubscriptionMode.INDICATION,
            resolveSubscriptionMode(
                SubscriptionMode.AUTOMATIC,
                setOf(CharacteristicProperty.INDICATE),
            ),
        )
    }

    @Test
    fun explicitNotificationRejectsACharacteristicWithoutNotify() {
        val failure =
            runCatching {
                resolveSubscriptionMode(
                    SubscriptionMode.NOTIFICATION,
                    setOf(CharacteristicProperty.INDICATE),
                )
            }.exceptionOrNull()

        assertEquals(OperationFailedException::class, failure?.let { it::class })
        assertEquals(
            OperationStatus.SubscribeNotPermitted,
            (failure as OperationFailedException).reason,
        )
    }

    @Test
    fun explicitIndicationRejectsACharacteristicWithoutIndicate() {
        val failure =
            runCatching {
                resolveSubscriptionMode(
                    SubscriptionMode.INDICATION,
                    setOf(CharacteristicProperty.NOTIFY),
                )
            }.exceptionOrNull()

        assertEquals(OperationFailedException::class, failure?.let { it::class })
        assertEquals(
            OperationStatus.SubscribeNotPermitted,
            (failure as OperationFailedException).reason,
        )
    }

    @Test
    fun disablingWritesTheDisabledCccdValue() {
        assertContentEquals(
            byteArrayOf(0x00, 0x00),
            cccdValue(enabled = false, resolvedMode = null),
        )
    }

    @Test
    fun notificationTransitionRegistersLocallyAndWritesNotificationCccd() = runBlocking {
        val controller = SubscriptionController()
        val localRegistrations = mutableListOf<Boolean>()
        val descriptorWrites = mutableListOf<ByteArray>()

        controller.setNotifying(
            enabled = true,
            requestedMode = SubscriptionMode.NOTIFICATION,
            properties = dualProperties,
            setLocalRegistration = localRegistrations::add,
            writeCccd = descriptorWrites::add,
        )

        assertTrue(controller.isEnabled)
        assertEquals(listOf(true), localRegistrations)
        assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), descriptorWrites)
    }

    @Test
    fun baseSubscribeForwardsNotificationModeToTheCccdWrite() = runBlocking {
        val fixture = BaseCharacteristicFixture()
        val subscribed = CompletableDeferred<Unit>()
        val collection = async {
            fixture.characteristic
                .subscribe(SubscriptionMode.NOTIFICATION) { subscribed.complete(Unit) }
                .collect()
        }

        subscribed.await()

        assertEquals(listOf(true), fixture.localRegistrations)
        assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), fixture.descriptorWrites)
        collection.cancelAndJoin()
    }

    @Test
    fun baseWaitForValueChangeForwardsNotificationModeBeforeItsTrigger() = runBlocking {
        val fixture = BaseCharacteristicFixture()
        val expected = byteArrayOf(0x21, 0x43)

        val actual =
            fixture.characteristic.waitForValueChange(
                mode = SubscriptionMode.NOTIFICATION,
                rawDataFilter = { true },
                merge = { _, received, _ -> MergeResult.Completed(received) },
                filter = { true },
                trigger = {
                    assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), fixture.descriptorWrites)
                    fixture.emitValue(expected)
                },
            )

        assertContentEquals(expected, actual)
        assertEquals(listOf(true), fixture.localRegistrations)
        assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), fixture.descriptorWrites)
    }

    @Test
    fun concurrentEquivalentRequestsProduceOneSerializedTransition() = runBlocking {
        val controller = SubscriptionController()
        val firstWriteEntered = CompletableDeferred<Unit>()
        val releaseFirstWrite = CompletableDeferred<Unit>()
        val localRegistrations = mutableListOf<Boolean>()
        val descriptorWrites = mutableListOf<ByteArray>()

        val first = async {
            controller.setNotifying(
                enabled = true,
                requestedMode = SubscriptionMode.NOTIFICATION,
                properties = dualProperties,
                setLocalRegistration = localRegistrations::add,
                writeCccd = { value ->
                    descriptorWrites += value
                    firstWriteEntered.complete(Unit)
                    releaseFirstWrite.await()
                },
            )
        }
        firstWriteEntered.await()
        val second = async {
            controller.setNotifying(
                enabled = true,
                requestedMode = SubscriptionMode.NOTIFICATION,
                properties = dualProperties,
                setLocalRegistration = localRegistrations::add,
                writeCccd = descriptorWrites::add,
            )
        }

        yield()
        assertEquals(1, descriptorWrites.size)
        releaseFirstWrite.complete(Unit)
        first.await()
        second.await()

        assertEquals(listOf(true), localRegistrations)
        assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), descriptorWrites)
    }

    @Test
    fun switchingModeRewritesCccdWithoutRepeatingLocalRegistration() = runBlocking {
        val controller = SubscriptionController()
        val localRegistrations = mutableListOf<Boolean>()
        val descriptorWrites = mutableListOf<ByteArray>()

        controller.setNotifying(
            true,
            SubscriptionMode.NOTIFICATION,
            dualProperties,
            localRegistrations::add,
            descriptorWrites::add,
        )
        controller.setNotifying(
            true,
            SubscriptionMode.INDICATION,
            dualProperties,
            localRegistrations::add,
            descriptorWrites::add,
        )

        assertTrue(controller.isEnabled)
        assertEquals(listOf(true), localRegistrations)
        assertByteArraysEqual(
            listOf(byteArrayOf(0x01, 0x00), byteArrayOf(0x02, 0x00)),
            descriptorWrites,
        )
    }

    @Test
    fun failedDescriptorWriteRollsBackLocalRegistrationAndState() = runBlocking {
        val controller = SubscriptionController()
        val localRegistrations = mutableListOf<Boolean>()

        val failure = runCatching {
            controller.setNotifying(
                enabled = true,
                requestedMode = SubscriptionMode.NOTIFICATION,
                properties = dualProperties,
                setLocalRegistration = localRegistrations::add,
                writeCccd = { error("CCCD write failed") },
            )
        }.exceptionOrNull()

        assertEquals(IllegalStateException::class, failure?.let { it::class })
        assertEquals(listOf(true, false), localRegistrations)
        assertFalse(controller.isEnabled)
    }

    @Test
    fun cancelledDescriptorWriteRollsBackLocalRegistrationAndState() = runBlocking {
        val controller = SubscriptionController()
        val writeEntered = CompletableDeferred<Unit>()
        val localRegistrations = mutableListOf<Boolean>()

        val request = async {
            controller.setNotifying(
                enabled = true,
                requestedMode = SubscriptionMode.NOTIFICATION,
                properties = dualProperties,
                setLocalRegistration = localRegistrations::add,
                writeCccd = {
                    writeEntered.complete(Unit)
                    awaitCancellation()
                },
            )
        }
        writeEntered.await()
        request.cancelAndJoin()

        assertEquals(listOf(true, false), localRegistrations)
        assertFalse(controller.isEnabled)
    }

    @Test
    fun failedModeSwitchPreservesThePreviouslyCommittedMode() = runBlocking {
        val controller = SubscriptionController()
        val descriptorWrites = mutableListOf<ByteArray>()

        controller.setNotifying(
            true,
            SubscriptionMode.NOTIFICATION,
            dualProperties,
            {},
            descriptorWrites::add,
        )
        val failure = runCatching {
            controller.setNotifying(
                true,
                SubscriptionMode.INDICATION,
                dualProperties,
                { error("Local registration must not change during a mode switch") },
                { error("CCCD write failed") },
            )
        }.exceptionOrNull()
        controller.setNotifying(
            true,
            SubscriptionMode.NOTIFICATION,
            dualProperties,
            { error("The committed notification registration must remain active") },
            descriptorWrites::add,
        )

        assertTrue(controller.isEnabled)
        assertEquals(IllegalStateException::class, failure?.let { it::class })
        assertByteArraysEqual(listOf(byteArrayOf(0x01, 0x00)), descriptorWrites)
    }

    @Test
    fun disablingPerformsOneOrderedTransitionAndIsIdempotent() = runBlocking {
        val controller = SubscriptionController()
        val operations = mutableListOf<String>()

        controller.setNotifying(
            true,
            SubscriptionMode.NOTIFICATION,
            dualProperties,
            { operations += "local:$it" },
            { operations += "cccd:${it.toHex()}" },
        )
        controller.setNotifying(
            false,
            SubscriptionMode.INDICATION,
            dualProperties,
            { operations += "local:$it" },
            { operations += "cccd:${it.toHex()}" },
        )
        controller.setNotifying(
            false,
            SubscriptionMode.NOTIFICATION,
            dualProperties,
            { operations += "unexpected-local:$it" },
            { operations += "unexpected-cccd:${it.toHex()}" },
        )

        assertFalse(controller.isEnabled)
        assertEquals(
            listOf("local:true", "cccd:0100", "local:false", "cccd:0000"),
            operations,
        )
    }

    @Test
    fun explicitModeDoesNotPreventDisablingAnotherRemoteCharacteristicImplementation() = runBlocking {
        val calls = mutableListOf<Boolean>()
        val characteristic = remoteCharacteristicProxy { calls += it }

        characteristic.setNotifying(false, SubscriptionMode.NOTIFICATION)

        assertEquals(listOf(false), calls)
    }

    @Test
    fun explicitModeRejectsEnablingAnotherRemoteCharacteristicImplementation() = runBlocking {
        val calls = mutableListOf<Boolean>()
        val characteristic = remoteCharacteristicProxy { calls += it }

        val failure = runCatching {
            characteristic.setNotifying(true, SubscriptionMode.NOTIFICATION)
        }.exceptionOrNull()

        assertEquals(OperationFailedException::class, failure?.let { it::class })
        assertEquals(
            OperationStatus.SubscribeNotPermitted,
            (failure as OperationFailedException).reason,
        )
        assertTrue(calls.isEmpty())
    }

    private fun assertByteArraysEqual(
        expected: List<ByteArray>,
        actual: List<ByteArray>,
    ) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (expectedValue, actualValue) ->
            assertContentEquals(expectedValue, actualValue)
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun remoteCharacteristicProxy(onSetNotifying: (Boolean) -> Unit): RemoteCharacteristic =
        Proxy.newProxyInstance(
            RemoteCharacteristic::class.java.classLoader,
            arrayOf(RemoteCharacteristic::class.java),
        ) { proxy, method, arguments ->
            when (method.name) {
                "setNotifying" -> {
                    onSetNotifying(arguments?.first() as Boolean)
                    Unit
                }
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === arguments?.firstOrNull()
                "toString" -> "RemoteCharacteristicProxy"
                else -> error("Unexpected proxy call: ${method.name}")
            }
        } as RemoteCharacteristic

    private class BaseCharacteristicFixture {
        private val events = MutableSharedFlow<GattEvent>(extraBufferCapacity = 1)
        private val executor = executor(events)
        private val peripheral = TestPeripheral(executor)
        private val service = TestService(peripheral)
        val localRegistrations = mutableListOf<Boolean>()
        val descriptorWrites = mutableListOf<ByteArray>()
        private val implementation = TestCharacteristic(service, events, localRegistrations)
        val characteristic: RemoteCharacteristic = implementation

        init {
            implementation.installDescriptor(TestDescriptor(implementation, events, descriptorWrites))
        }

        fun emitValue(value: ByteArray) {
            check(events.tryEmit(CharacteristicChanged(implementation, value)))
        }

        private companion object {
            @Suppress("UNCHECKED_CAST")
            fun executor(events: MutableSharedFlow<GattEvent>): Peripheral.Executor<String> =
                Proxy.newProxyInstance(
                    Peripheral.Executor::class.java.classLoader,
                    arrayOf(Peripheral.Executor::class.java),
                ) { proxy, method, arguments ->
                    when (method.name) {
                        "getEnvironment" -> systemEnvironment
                        "getIdentifier" -> "test-peripheral"
                        "getInitialState" -> ConnectionState.Connected
                        "getInitialServices" -> emptyList<RemoteService>()
                        "getEvents" -> events
                        "getName" -> "Test peripheral"
                        "getLogger" -> null
                        "getLogId" -> "test-peripheral"
                        "isClosed", "isReliableWriteEnabled" -> false
                        "setLogger", "setReliableWriteEnabled" -> Unit
                        "hashCode" -> System.identityHashCode(proxy)
                        "equals" -> proxy === arguments?.firstOrNull()
                        "toString" -> "TestPeripheralExecutor"
                        else -> error("Unexpected executor call: ${method.name}")
                    }
                } as Peripheral.Executor<String>

            val systemEnvironment: Environment =
                Proxy.newProxyInstance(
                    Environment::class.java.classLoader,
                    arrayOf(Environment::class.java),
                ) { proxy, method, arguments ->
                    when (method.name) {
                        "isSystem", "isBluetoothSupported", "isBluetoothEnabled" -> true
                        "getDeviceName" -> "Test environment"
                        "hashCode" -> System.identityHashCode(proxy)
                        "equals" -> proxy === arguments?.firstOrNull()
                        "toString" -> "TestEnvironment"
                        else -> false
                    }
                } as Environment
        }
    }

    private class TestPeripheral(
        executor: Peripheral.Executor<String>,
    ) : Peripheral<String, Peripheral.Executor<String>>(
            CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
            executor,
        ) {
        override fun maximumWriteValueLength(type: WriteType): Int = 512
    }

    private class TestService(peripheral: TestPeripheral) : RemoteService() {
        override val uuid = Uuid.parse("00001800-0000-1000-8000-00805f9b34fb")
        override val instanceId: Int = 1
        override val characteristics: List<RemoteCharacteristic> = emptyList()
        override val includedServices: List<RemoteIncludedService> = emptyList()

        init {
            owner = peripheral
        }
    }

    private class TestCharacteristic(
        service: TestService,
        events: MutableSharedFlow<GattEvent>,
        private val localRegistrations: MutableList<Boolean>,
    ) : BaseRemoteCharacteristic(service, events) {
        override val uuid = Uuid.parse("00002a00-0000-1000-8000-00805f9b34fb")
        override val instanceId: Int = 1
        override val properties = dualProperties
        override var descriptors: List<RemoteDescriptor> = emptyList()
            private set

        fun installDescriptor(descriptor: RemoteDescriptor) {
            descriptors = listOf(descriptor)
        }

        override fun setCharacteristicNotification(enabled: Boolean) {
            localRegistrations += enabled
        }

        override suspend fun FlowCollector<GattEvent>.executeRead() = error("Not used")

        override suspend fun FlowCollector<GattEvent>.executeWrite(data: ByteArray, writeType: WriteType) =
            error("Not used")

        override fun OperationEvent.matches(): Boolean = subject === this@TestCharacteristic
    }

    private class TestDescriptor(
        characteristic: TestCharacteristic,
        events: MutableSharedFlow<GattEvent>,
        private val writes: MutableList<ByteArray>,
    ) : BaseRemoteDescriptor(characteristic, events) {
        override val uuid = no.nordicsemi.kotlin.ble.core.Descriptor.CLIENT_CHAR_CONF_UUID
        override val instanceId: Int = 1

        override suspend fun FlowCollector<GattEvent>.executeRead() = error("Not used")

        override suspend fun FlowCollector<GattEvent>.executeWrite(data: ByteArray) {
            writes += data.copyOf()
            emit(DescriptorWrite(this@TestDescriptor, OperationStatus.Success))
        }

        override fun OperationEvent.matches(): Boolean = subject === this@TestDescriptor
    }

    private companion object {
        val dualProperties =
            setOf(
                CharacteristicProperty.NOTIFY,
                CharacteristicProperty.INDICATE,
            )
    }
}
