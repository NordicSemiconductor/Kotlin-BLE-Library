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

package no.nordicsemi.kotlin.ble.android.compose

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.app.ActivityOptionsCompat
import no.nordicsemi.kotlin.ble.android.mock.MockAndroidEnvironment
import no.nordicsemi.kotlin.ble.core.android.AndroidEnvironment

private const val BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"
private const val BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
private const val BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE"
private const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"

/**
 * A Jetpack Compose provider that intercepts permission requests for a mock environment.
 *
 * This provider is essential for testing Composables that request Bluetooth-related permissions.
 *
 * It handles the following permissions by updating the state of the provided [MockAndroidEnvironment]:
 * - `BLUETOOTH_SCAN`
 * - `BLUETOOTH_CONNECT`
 * - `BLUETOOTH_ADVERTISE`
 * - `ACCESS_FINE_LOCATION` - only for scanning for Bluetooth LE devices.
 *
 * **Important Boundary:** This mock only affects the permission system for Activity Result APIs.
 * It **does not** mock the underlying `LocationManager` or other hardware APIs. A "granted"
 * `ACCESS_FINE_LOCATION` permission via this provider will enable mock BLE scanning but will **not**
 * allow your app to receive GPS location updates.
 *
 * @param environment The environment to be updated with permission results.
 * @param content The composable content that will use the mocked registry.
 */
@Composable
fun LocalEnvironmentProvider(
    environment: AndroidEnvironment,
    content: @Composable () -> Unit,
) {
    // This provider only modifies behavior for the MockAndroidEnvironment.
    if (environment !is MockAndroidEnvironment) {
        content()
        return
    }

    // Modify the LocalActivityResultRegistryOwner only in the mock environment.
    val realOwner = LocalActivityResultRegistryOwner.current
    val registry: ActivityResultRegistry = remember(environment, realOwner) {
        object : ActivityResultRegistry() {

            private val mockHandledPermissions = setOf(
                BLUETOOTH_CONNECT,
                BLUETOOTH_SCAN,
                BLUETOOTH_ADVERTISE,
                ACCESS_FINE_LOCATION
            )

            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                // The Environment only can request mock permissions.
                // Other contracts will be passed to the Activity below.
                // Note: The Location Fine permission is mocked only in the context of scanning
                //       for Bluetooth LE devices. It does not let accessing `LocationManager`.
                val permissions: Array<String>? = when (contract) {
                    is ActivityResultContracts.RequestPermission if input is String ->
                        arrayOf(input)

                    is ActivityResultContracts.RequestMultiplePermissions if input is Array<*> && input.isArrayOf<String>() ->
                        @Suppress("UNCHECKED_CAST")
                        input as Array<String>

                    else -> null
                }
                if (permissions != null) {
                    val hasMockedPermissions = permissions.any { it in mockHandledPermissions }
                    val hasOtherPermissions = permissions.any { it !in mockHandledPermissions }

                    // Only support cases when there are mocked permissions requested.
                    // Otherwise, fallback to the real owner.
                    if (hasMockedPermissions) {
                        // If there are other permissions, inform the user that they need to
                        // be requested separately. Otherwise, the result would only contain
                        // the permissions that were sent to the OS.
                        if (hasOtherPermissions) {
                            throw UnsupportedOperationException("Mock and native permissions requested together")
                        }
                        // Setters of those can only change from false to true.
                        // It's safe to set them to false, as this is a no-op.
                        if (environment.supportsRuntimePermissions) {
                            environment.isLocationPermissionGranted = permissions.contains(ACCESS_FINE_LOCATION)

                            if (environment.requiresBluetoothRuntimePermissions) {
                                environment.isBluetoothConnectPermissionGranted = permissions.contains(BLUETOOTH_CONNECT)
                                environment.isBluetoothScanPermissionGranted = permissions.contains(BLUETOOTH_SCAN)
                                environment.isBluetoothAdvertisePermissionGranted = permissions.contains(BLUETOOTH_ADVERTISE)
                            }
                        }

                        fun String.isGranted(): Boolean = when (this) {
                            BLUETOOTH_SCAN -> environment.isBluetoothScanPermissionGranted
                            BLUETOOTH_CONNECT -> environment.isBluetoothConnectPermissionGranted
                            BLUETOOTH_ADVERTISE -> environment.isBluetoothAdvertisePermissionGranted
                            ACCESS_FINE_LOCATION -> environment.isLocationPermissionGranted
                            else -> false
                        }

                        when (contract) {
                            is ActivityResultContracts.RequestPermission ->
                                dispatchResult(requestCode, permissions[0].isGranted())

                            is ActivityResultContracts.RequestMultiplePermissions ->
                                dispatchResult(requestCode, permissions.associateWith { it.isGranted() })
                        }
                        return
                    }
                }
                // Handle other contracts using native Activity.
                realOwner?.activityResultRegistry?.onLaunch(
                    requestCode,
                    contract,
                    input,
                    options
                )
            }
        }
    }
    val owner: ActivityResultRegistryOwner = remember(registry) {
        object : ActivityResultRegistryOwner {
            override val activityResultRegistry = registry
        }
    }
    CompositionLocalProvider(
        value = LocalActivityResultRegistryOwner provides owner,
        content = content
    )
}