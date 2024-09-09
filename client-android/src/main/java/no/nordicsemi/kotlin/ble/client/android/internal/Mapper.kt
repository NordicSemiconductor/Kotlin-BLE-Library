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

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.Build
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.core.util.forEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import no.nordicsemi.kotlin.ble.client.android.AdvertisementData
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import android.bluetooth.le.ScanResult as NativeScanResult
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.WriteType

internal fun Int.toState(): Manager.State = when (this) {
    BluetoothAdapter.STATE_OFF -> Manager.State.POWERED_OFF
    BluetoothAdapter.STATE_ON -> Manager.State.POWERED_ON
    BluetoothAdapter.STATE_TURNING_OFF,
    BluetoothAdapter.STATE_TURNING_ON -> Manager.State.RESETTING
    else -> Manager.State.UNKNOWN
}

internal fun Int.toBondState(): BondState = when (this) {
    BluetoothDevice.BOND_BONDED -> BondState.BONDED
    BluetoothDevice.BOND_BONDING -> BondState.BONDING
    else -> BondState.NONE
}

internal fun Int.toConnectionState(status: Int): ConnectionState = when (this) {
    BluetoothGatt.STATE_CONNECTED -> ConnectionState.Connected
    BluetoothGatt.STATE_CONNECTING -> ConnectionState.Connecting
    BluetoothGatt.STATE_DISCONNECTED -> ConnectionState.Disconnected(status.toDisconnectionReason())
    BluetoothGatt.STATE_DISCONNECTING -> ConnectionState.Disconnecting
    else -> ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.Unknown(this))
}

private fun Int.toDisconnectionReason(): ConnectionState.Disconnected.Reason = when (this) {
    BluetoothGatt.GATT_SUCCESS -> ConnectionState.Disconnected.Reason.Success
    0x08 /* GATT_CONN_TIMEOUT */ -> ConnectionState.Disconnected.Reason.LinkLoss
    0x13 /* GATT_CONN_TERMINATE_PEER_USER */ -> ConnectionState.Disconnected.Reason.TerminatePeerUser
    0x16 /* GATT_CONN_TERMINATE_LOCAL_HOST */ -> ConnectionState.Disconnected.Reason.TerminateLocalHost
    else -> ConnectionState.Disconnected.Reason.Unknown(this)
}

internal fun Int.toPeripheralType(): PeripheralType = when (this) {
    BluetoothDevice.DEVICE_TYPE_CLASSIC -> PeripheralType.CLASSIC
    BluetoothDevice.DEVICE_TYPE_DUAL -> PeripheralType.DUAL
    BluetoothDevice.DEVICE_TYPE_LE -> PeripheralType.LE
    else -> PeripheralType.UNKNOWN
}

internal fun Int.errorCodeToReason(): ScanningFailedToStartException.Reason = when (this) {
    ScanCallback.SCAN_FAILED_ALREADY_STARTED -> ScanningFailedToStartException.Reason.AlreadyStarted
    ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> ScanningFailedToStartException.Reason.ApplicationRegistrationFailed
    ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> ScanningFailedToStartException.Reason.FeatureUnsupported
    ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> ScanningFailedToStartException.Reason.InternalError
    ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> ScanningFailedToStartException.Reason.OutOfResources
    ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> ScanningFailedToStartException.Reason.ScanningTooFrequently
    else -> ScanningFailedToStartException.Reason.Unknown(this)
}

internal fun NativeScanResult.toScanResult(scope: CoroutineScope, context: Context): ScanResult? {
    val scanRecord = scanRecord ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ScanResult(
            peripheral = Peripheral(
                scope = scope,
                impl = NativeExecutor(context, device, scanRecord.deviceName ?: device.name),
            ),
            isConnectable =  isConnectable,
            advertisementData = scanRecord.toAdvertisementData(),
            rssi = rssi,
            txPowerLevel =
                if (txPower != NativeScanResult.TX_POWER_NOT_PRESENT)
                    txPower
                else
                    if (scanRecord.txPowerLevel != Int.MIN_VALUE)
                        scanRecord.txPowerLevel
                    else
                        null,
            primaryPhy = primaryPhy.toPrimaryPhy(),
            secondaryPhy = secondaryPhy.toPhy(),
            timestamp = Clock.System.now()
        )
    } else {
        ScanResult(
            peripheral = Peripheral(
                scope = scope,
                impl = NativeExecutor(context, device, device.name),
            ),
            isConnectable =  true,
            advertisementData = scanRecord.toAdvertisementData(),
            rssi = rssi,
            txPowerLevel =
                if (scanRecord.txPowerLevel != Int.MIN_VALUE)
                    scanRecord.txPowerLevel
                else
                    null,
            primaryPhy = PrimaryPhy.PHY_LE_1M,
            secondaryPhy = null,
            timestamp = Clock.System.now()
        )
    }
}

private fun ScanRecord.toAdvertisementData(): AdvertisementData {
    @Suppress("UNNECESSARY_SAFE_CALL", "USELESS_ELVIS")
    return AdvertisementData(
        name = deviceName,
        serviceUuids = serviceUuids?.map { it.uuid } ?: emptyList(),
        serviceSolicitationUuids =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                serviceSolicitationUuids?.map { it.uuid } ?: emptyList()
            else emptyList(),
        serviceData = serviceData?.mapKeys { it.key.uuid } ?: emptyMap(),
        manufacturerData = manufacturerSpecificData?.toMap() ?: emptyMap(),
        raw = bytes,
    )
}

private fun SparseArray<ByteArray>.toMap(): Map<Int, ByteArray> {
    val map = mutableMapOf<Int, ByteArray>()
    forEach { key, value -> map[key] = value }
    return map
}

private fun Int.toPrimaryPhy(): PrimaryPhy = when (this) {
    3 /* BluetoothDevice.PHY_LE_CODED */ -> PrimaryPhy.PHY_LE_CODED
    else -> PrimaryPhy.PHY_LE_1M
}

internal fun Int.toPhy(): Phy = when (this) {
    2 /* BluetoothDevice.PHY_LE_2M */ -> Phy.PHY_LE_2M
    3 /* BluetoothDevice.PHY_LE_CODED */ -> Phy.PHY_LE_CODED
    else -> Phy.PHY_LE_1M
}

internal fun Phy.toPhy(): Int = when (this) {
    Phy.PHY_LE_2M -> 2 /* BluetoothDevice.PHY_LE_2M */
    Phy.PHY_LE_CODED -> 3 /* BluetoothDevice.PHY_LE_CODED */
    else -> 1 /* BluetoothDevice.PHY_LE_1M */
}

internal fun PhyOption.toOption(): Int = when (this) {
    PhyOption.NO_PREFERRED -> 0 /* BluetoothDevice.PHY_OPTION_NO_PREFERRED */
    PhyOption.S2 -> 1 /* BluetoothDevice.PHY_OPTION_S2 */
    PhyOption.S8 -> 2 /* BluetoothDevice.PHY_OPTION_S8 */
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun List<Phy>.toMask(): Int {
    var mask = 0
    forEach {
        mask = mask or when (it) {
            Phy.PHY_LE_1M -> 1 /* BluetoothDevice.PHY_LE_1M_MASK */
            Phy.PHY_LE_2M -> 2 /* BluetoothDevice.PHY_LE_2M_MASK */
            Phy.PHY_LE_CODED -> 4 /* BluetoothDevice.PHY_LE_CODED_MASK */
        }
    }
    return mask
}

internal fun ConnectionPriority.toPriority() = when (this) {
    ConnectionPriority.BALANCED -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
    ConnectionPriority.HIGH -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    ConnectionPriority.LOW_POWER -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
    ConnectionPriority.DIGITAL_CAR_KEY ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            BluetoothGatt.CONNECTION_PRIORITY_DCK
        else
            BluetoothGatt.CONNECTION_PRIORITY_BALANCED
}

internal fun Int.toOperationStatus(): OperationStatus = when (this) {
    BluetoothGatt.GATT_SUCCESS -> OperationStatus.SUCCESS
    BluetoothGatt.GATT_CONNECTION_CONGESTED -> OperationStatus.CONNECTION_CONGESTED
    BluetoothGatt.GATT_READ_NOT_PERMITTED -> OperationStatus.READ_NOT_PERMITTED
    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> OperationStatus.WRITE_NOT_PERMITTED
    BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> OperationStatus.INSUFFICIENT_AUTHENTICATION
    BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION -> OperationStatus.INSUFFICIENT_AUTHORIZATION
    BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> OperationStatus.INSUFFICIENT_ENCRYPTION
    BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> OperationStatus.REQUEST_NOT_SUPPORTED
    BluetoothGatt.GATT_INVALID_OFFSET -> OperationStatus.INVALID_OFFSET
    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> OperationStatus.INVALID_ATTRIBUTE_LENGTH
    133 -> OperationStatus.GATT_ERROR
    else -> OperationStatus.UNKNOWN_ERROR
}

internal fun WriteType.toInt() = when (this) {
    WriteType.WITH_RESPONSE -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    WriteType.WITHOUT_RESPONSE -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    WriteType.SIGNED -> BluetoothGattCharacteristic.WRITE_TYPE_SIGNED
}