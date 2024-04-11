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

import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.advertiser.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingPayload
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiserAndroid
import org.jetbrains.annotations.Range
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Creates an instance of a mock [BluetoothLeAdvertiser] for Android.
 *
 * The behavior of a mock advertiser should mimic one from the same Android version.
 *
 * @param multipleAdvertisementSupported Whether the mock device should support multiple advertisements.
 * @param leExtendedAdvertisingSupported Whether the mock device should support Advertising Extension
 *        from Bluetooth 5.
 * @param le2MPhySupported Whether the mock device should support PHY LE 2M.
 * @param leCodedPhySupported Whether the mock device should support PHY LE Coded.
 * @param leMaximumAdvertisingDataLength Maximum length of the advertising data. For legacy advertising
 *        this is set to 31 bytes per packet.
 * @param bluetoothAdvertisePermissionGranted Whether the mock device should emulate the permission
 *        to advertise granted, or not.
 * @return A mock instance of the [BluetoothLeAdvertiserAndroid].
 */
@Suppress("unused")
fun BluetoothLeAdvertiser.Factory.mock(
    multipleAdvertisementSupported: Boolean = true,
    leExtendedAdvertisingSupported: Boolean = true,
    le2MPhySupported: Boolean = true,
    leCodedPhySupported: Boolean = true,
    leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int = 1650,
    bluetoothAdvertisePermissionGranted: Boolean = true,
): BluetoothLeAdvertiserAndroid = BluetoothLeAdvertiserMock(
    multipleAdvertisementSupported = multipleAdvertisementSupported,
    leExtendedAdvertisingSupported = leExtendedAdvertisingSupported,
    le2MPhySupported = le2MPhySupported,
    leCodedPhySupported = leCodedPhySupported,
    leMaximumAdvertisingDataLength = leMaximumAdvertisingDataLength,
    bluetoothAdvertisePermissionGranted = bluetoothAdvertisePermissionGranted,
)

/**
 * A mock implementation of Bluetooth LE advertiser.
 *
 * Use this implementation to emulate Bluetooth LE advertising in tests.
 */
class BluetoothLeAdvertiserMock internal constructor(
    val multipleAdvertisementSupported: Boolean,
    val leExtendedAdvertisingSupported: Boolean,
    val le2MPhySupported: Boolean,
    val leCodedPhySupported: Boolean,
    val leMaximumAdvertisingDataLength: Int,
    val bluetoothAdvertisePermissionGranted: Boolean,
): BluetoothLeAdvertiserAndroid {
    private val logger: Logger = LoggerFactory.getLogger(BluetoothLeAdvertiserMock::class.java)

    override var name: String? = null

    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        block: ((txPower: Int) -> Unit)?
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        // Mocking advertising has no impact on other features.
        // Local advertising is not visible on scanner nor can be used to connect.
        // Let's just pretend advertising has started.
        logger.info("Advertising initiated")
    }
}