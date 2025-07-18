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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.Keep
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.kotlin.ble.client.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RssiRead
import no.nordicsemi.kotlin.ble.client.ServicesDiscovered
import no.nordicsemi.kotlin.ble.client.ServicesChanged
import no.nordicsemi.kotlin.ble.client.android.ConnectionParametersChanged
import no.nordicsemi.kotlin.ble.client.android.MtuChanged
import no.nordicsemi.kotlin.ble.client.android.PhyChanged
import no.nordicsemi.kotlin.ble.core.ConnectionParameters
import no.nordicsemi.kotlin.ble.core.PhyInUse
import org.slf4j.LoggerFactory

internal class NativeGattCallback: BluetoothGattCallback() {
    // TODO: remove logger here?
    private val logger = LoggerFactory.getLogger(NativeGattCallback::class.java)

    private val _events: MutableSharedFlow<GattEvent> = MutableSharedFlow(extraBufferCapacity = 64)
    val events: SharedFlow<GattEvent> = _events.asSharedFlow()

    /**
     * Older Android versions don't return status=8 (GATT_CONN_TIMEOUT) when the connection
     * drops due to a link loss. By checking whether it was the user who requested disconnection
     * we can improve the status.
     */
    var disconnectRequest = false

    // Handling connection state updates

    // TODO Remove all debug logs

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        logger.debug("onConnectionStateChange: status=$status, newState=$newState")
        // Pixel 4 with Android 12 does returns status 0 when link is lost to a device.
        // Newer versions (Pixel 7 with Android 16) report status 8 (timeout) in the same case.
        val betterStatus = if (
                newState == BluetoothGatt.STATE_DISCONNECTED &&
                status == BluetoothGatt.GATT_SUCCESS &&
                !disconnectRequest
            ) 0x08 /* GATT_CONN_TIMEOUT */ else status
        _events.tryEmit(ConnectionStateChanged(newState.toConnectionState(betterStatus)))
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        logger.debug("onServicesDiscovered: status=$status")
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("Services discovery failed with status $status")
            _events.tryEmit(ServicesDiscovered(emptyList()))
            return
        }
        _events.tryEmit(ServicesDiscovered(gatt.services.map { NativeRemoteService(gatt, it, events) }))
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        logger.debug("onServiceChanged")
        _events.tryEmit(ServicesChanged)
    }

    // Handling value changes.

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        logger.debug("onCharacteristicChanged: characteristic={}, value={}", characteristic.uuid, value.toHexString())
        _events.tryEmit(CharacteristicChanged(characteristic, value))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        logger.debug("onCharacteristicRead: characteristic={}, value={}, status={}", characteristic.uuid, value.toHexString(), status)
        _events.tryEmit(CharacteristicRead(characteristic, value, status.toOperationStatus()))
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        logger.debug("onCharacteristicWrite: characteristic={}, status={}", characteristic.uuid, status)
        _events.tryEmit(CharacteristicWrite(characteristic, status.toOperationStatus()))
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
        logger.debug("onDescriptorRead: descriptor={}, value={}, status={}", descriptor.uuid, value.toHexString(), status)
        _events.tryEmit(DescriptorRead(descriptor, value, status.toOperationStatus()))
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        logger.debug("onDescriptorWrite: descriptor={}, status={}", descriptor.uuid, status)
        _events.tryEmit(DescriptorWrite(descriptor, status.toOperationStatus()))
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        logger.debug("onReliableWriteCompleted: status=$status")
        // TODO implement
    }

    // Handling connection parameter updates

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("MTU request failed with status $status")
            return
        }
        logger.debug("onMtuChanged: mtu=$mtu")
        _events.tryEmit(MtuChanged(mtu))
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("RSSI request failed with status $status")
            return
        }
        logger.debug("onReadRemoteRssi: rssi=$rssi")
        _events.tryEmit(RssiRead(rssi))
    }

    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("PHY update failed with status $status")
            return
        }
        val phyInUse = PhyInUse(txPhy.toPhy(), rxPhy.toPhy())
        logger.debug("onPhyUpdate: {}", phyInUse)
        _events.tryEmit(PhyChanged(phyInUse))
    }

    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("Reading PHY failed with status $status")
            return
        }
        val phyInUse = PhyInUse(txPhy.toPhy(), rxPhy.toPhy())
        logger.debug("onPhyRead: {}", phyInUse)
        _events.tryEmit(PhyChanged(phyInUse))
    }

    @Suppress("UNUSED_PARAMETER")
    @Keep
    fun onConnectionUpdated(gatt: BluetoothGatt, interval: Int, latency: Int, timeout: Int, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            logger.warn("Connection update failed with status $status")
            return
        }
        val newParameters = ConnectionParameters.Connected(interval, latency, timeout)
        logger.debug("onConnectionUpdated: {}", newParameters)
        _events.tryEmit(ConnectionParametersChanged(newParameters))
    }

    /**
     * This method is called on Android versions prior to Android Oreo where there is no callback
     * for when the connection parameters are updated.
     *
     * It reports [ConnectionParameters.Unknown].
     */
    fun onConnectionUpdated() {
        val newParameters = ConnectionParameters.Unknown
        logger.debug("onConnectionUpdated: {}", newParameters)
        _events.tryEmit(ConnectionParametersChanged(newParameters))
    }

    // Backward compatibility

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        onCharacteristicChanged(gatt, characteristic, characteristic.value?.clone() ?: byteArrayOf())
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        onCharacteristicRead(gatt, characteristic, characteristic.value?.clone() ?: byteArrayOf(), status)
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        onDescriptorRead(gatt, descriptor, status, descriptor.value?.clone() ?: byteArrayOf())
    }
}