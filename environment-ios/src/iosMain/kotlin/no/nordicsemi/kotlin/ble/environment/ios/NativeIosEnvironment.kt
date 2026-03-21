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

package no.nordicsemi.kotlin.ble.environment.ios

import no.nordicsemi.kotlin.ble.core.ios.IosEnvironment
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

/**
 * iOS environment
 */
class NativeIosEnvironment(
    private val cbCentralManager: CBCentralManager
) : IosEnvironment {

    init {
        checkBluetoothUsageDescription()
    }

    override val deviceName: String
        get() = UIDevice.currentDevice.name

    override val isBluetoothSupported: Boolean
        get() = cbCentralManager.state != CBManagerStateUnsupported

    override val isBluetoothEnabled: Boolean
        get() = cbCentralManager.state == CBManagerStatePoweredOn

    private fun hasInfoPlistStringKey(key: String): Boolean {
        val value = NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String
        return !value.isNullOrBlank()
    }

    private val isIos13OrHigher: Boolean
        get() = UIDevice
            .currentDevice
            .systemVersion
            .compareTo("13.0", ignoreCase = true) >= 0

    private fun checkBluetoothUsageDescription() {
        check(hasRequiredBluetoothUsageDescription) {
            "Missing required Bluetooth usage description in Info.plist"
        }
    }

    private val hasRequiredBluetoothUsageDescription: Boolean
        get() = if (isIos13OrHigher) {
            hasInfoPlistStringKey("NSBluetoothAlwaysUsageDescription")
        } else {
            hasInfoPlistStringKey("NSBluetoothPeripheralUsageDescription")
        }
}
