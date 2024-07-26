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

package no.nordicsemi.kotlin.ble.advertiser.android.mock

import no.nordicsemi.kotlin.ble.advertiser.exception.GenericBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingPayload
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.android.mock.MockEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration


/**
 * Creates an instance of a mock [GenericBluetoothLeAdvertiser] for Android.
 *
 * The behavior of a mock advertiser should mimic one from the same Android version.
 *
 * @param environment The mock environment to use.
 * @return A mock instance of the [BluetoothLeAdvertiser].
 */
@Suppress("unused")
fun BluetoothLeAdvertiser.Factory.mock(
    environment: MockEnvironment = MockEnvironment.Api31(),
): BluetoothLeAdvertiser = BluetoothLeAdvertiserMock(
    environment = environment
)

/**
 * A mock implementation of Bluetooth LE advertiser.
 *
 * Use this implementation to emulate Bluetooth LE advertising in tests.
 *
 * @property environment The mock environment.
 */
class BluetoothLeAdvertiserMock internal constructor(
    private val environment: MockEnvironment,
): BluetoothLeAdvertiser {
    private val logger: Logger = LoggerFactory.getLogger(BluetoothLeAdvertiserMock::class.java)

    override var name: String? = if (environment.isBluetoothConnectPermissionGranted)
        environment.deviceName else null

    override fun getMaximumAdvertisingDataLength(legacy: Boolean): Int {
        if (!environment.isBluetoothSupported) return 0
        if (legacy ||
            environment.androidSdkVersion < 26 /* Oreo */ ||
            !environment.isLeExtendedAdvertisingSupported) return 31
        return environment.leMaximumAdvertisingDataLength
    }

    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        timeout: Duration,
        block: ((txPower: Int) -> Unit)?
    ) = suspendCoroutine<Unit> { continuation ->
        // Mocking advertising has no impact on other features.
        // Local advertising is not visible on scanner nor can be used to connect.
        // Let's just pretend advertising has started.
        logger.info("Advertising initiated")

        // TODO implement validation using above environment variables
        // TODO implement timeout
    }
}