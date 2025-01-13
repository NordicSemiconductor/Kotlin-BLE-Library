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
import dagger.hilt.components.SingletonComponent
// import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.android.mock.MockAdvertiser
import no.nordicsemi.kotlin.ble.android.mock.MockEnvironment
// import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import timber.log.Timber
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class SdkModule {

    @Provides
    @Named("sdkVersion")
    fun provideSdkVersion() = 34 // Build.VERSION.SDK_INT

    @Provides
    fun providesEnvironment(@Named("sdkVersion") sdkVersion: Int): MockEnvironment {
        // Setting an advertiser callback allows to simulate different behaviors
        // of the advertiser, such as returning a different TX power, or failing.
        val advertiser: MockAdvertiser = { requestedTxPower, advertisingData, scanResponse ->
            Timber.d("Mocking advertisement of: $advertisingData, $scanResponse")

            // Mock advertisement can return a failure:
            // Result.failure(AdvertisingNotStartedException(AdvertisingNotStartedException.Reason.FEATURE_UNSUPPORTED))

            // Or a success by providing the mock TX power to return to the advertiser:
            Result.success(requestedTxPower - 1)
        }

        // Return an environment based on the mock SDK version.
        fun Int.toMockEnvironment() = when (this) {
            in 21..22 -> MockEnvironment.Api21(advertiser = advertiser)
            in 23..25 -> MockEnvironment.Api23(advertiser = advertiser)
            in 26..30 -> MockEnvironment.Api26(advertiser = advertiser)
            else -> MockEnvironment.Api31(
                advertiser = advertiser,

                // Uncomment to disable LE Coded PHY support.
                // isLeCodedPhySupported = false,

                // If LE Coded PHY is supported, uncommenting this will make the scanner NOT
                // return packets sent on Coded PHY as primary PHY. Some phones can't scan on
                // Coded PHY, but can receive packets sent on Coded PHY when connected.
                // isScanningOnLeCodedPhySupported = false,

                // Uncomment to make the scanner throw an exception as if ScanCallback.onScanFailed was called.
                // scanner = { Result.failure(ScanningFailedToStartException(ScanningFailedToStartException.Reason.ScanningTooFrequently)) }

                // Uncomment to pretend the scanner has started (onScanFailed not called), but
                // the scanner won't return any results.
                // scanner = { Result.success(false) }
            )
        }
        return sdkVersion.toMockEnvironment()
    }

}