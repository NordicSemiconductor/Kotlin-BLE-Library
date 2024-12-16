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

package no.nordicsemi.kotlin.ble.android.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.mock.mock
import no.nordicsemi.kotlin.ble.android.mock.MockEnvironment
import no.nordicsemi.kotlin.ble.android.sample.util.CloseableCoroutineScope
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.mock.mock
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpecEventHandler
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.LegacyAdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.Permission
import timber.log.Timber
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    private val blinkyImpl: PeripheralSpecEventHandler = object: PeripheralSpecEventHandler {

        override fun onConnectionRequest(): Result<Unit> {
            Timber.i("Connection request received")
            return Result.success(Unit)
        }

    }

    private val blinky = PeripheralSpec
        .simulatePeripheral("AA:BB:CC:DD:EE:FF") {
            advertising(
                parameters = LegacyAdvertisingSetParameters(
                    connectable = true
                ),
                isAdvertisingWhenConnected = false,
            ) {
                CompleteLocalName("Nordic_LBS")
                ServiceUuid(Uuid.parse("00001523-1212-EFDE-1523-785FEABCD123"))
                IncludeTxPowerLevel()
            }
            connectable(
                name = "Nordic_Blinky",
                maxMtu = 247,
                eventHandler = blinkyImpl,
            ) {
                Service(
                    uuid = Uuid.parse("00001523-1212-EFDE-1523-785FEABCD123")
                ) {
                    Characteristic(
                        uuid = Uuid.parse("00001524-1212-EFDE-1523-785FEABCD123"),
                        properties = listOf(CharacteristicProperty.READ, CharacteristicProperty.WRITE),
                        permissions = listOf(Permission.READ, Permission.WRITE),
                    ) {
                        // CCCD is added automatically
                        CharacteristicUserDescriptionDescriptor("Button 1")
                    }
                    Characteristic(
                        uuid = Uuid.parse("00001525-1212-EFDE-1523-785FEABCD123"),
                        properties = listOf(CharacteristicProperty.READ, CharacteristicProperty.NOTIFY),
                        permissions = listOf(Permission.READ),
                    ) {
                        // CCCD is added automatically
                        CharacteristicUserDescriptionDescriptor("LED 1")
                    }
                }
            }
        }

    @Provides
    fun provideViewModelCoroutineScope(lifecycle: ViewModelLifecycle): CoroutineScope {
        return CloseableCoroutineScope(SupervisorJob())
            .also { closeableCoroutineScope ->
                lifecycle.addOnClearedListener(closeableCoroutineScope)
            }
    }

    @Provides
    fun providesAdvertiser(environment: MockEnvironment): BluetoothLeAdvertiser {
        return BluetoothLeAdvertiser.Factory.mock(environment)
    }

    @Provides
    fun provideCentralManager(scope: CoroutineScope): CentralManager {
        return CentralManager.Factory.mock(scope)
            .apply {
                simulatePeripherals(listOf(blinky))
            }
    }

}