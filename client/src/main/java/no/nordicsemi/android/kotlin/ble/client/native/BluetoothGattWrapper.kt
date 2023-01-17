package no.nordicsemi.android.kotlin.ble.client.native

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.client.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.client.callback.BleGattConnection
import no.nordicsemi.android.kotlin.ble.client.callback.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.client.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.client.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.event.GattEvent
import no.nordicsemi.android.kotlin.ble.client.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.client.event.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.client.event.OnPhyRead
import no.nordicsemi.android.kotlin.ble.client.event.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.client.event.OnReadRemoteRssi
import no.nordicsemi.android.kotlin.ble.client.event.OnServiceChanged
import no.nordicsemi.android.kotlin.ble.client.event.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.client.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.client.service.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class BluetoothGattWrapper : BleGatt {

    private var gatt: BluetoothGatt? = null

    private val _event = MutableSharedFlow<GattEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val event: SharedFlow<GattEvent> = _event.asSharedFlow()

    private val _connection = MutableStateFlow(BleGattConnection())
    val connection = _connection.asStateFlow()

    private var onConnectionStateChangedCallback: ((GattConnectionState, BleGattOperationStatus) -> Unit)? = null

    private val gattProxy: BluetoothGattClientCallback = BluetoothGattClientCallback {
        _event.tryEmit(it)
        when (it) {
            is OnConnectionStateChanged -> onConnectionStateChange(it.gatt, it.status, it.newState)
            is OnServicesDiscovered -> onServicesDiscovered(it.gatt, it.status)
            is CharacteristicEvent -> _connection.value.services?.apply { onCharacteristicEvent(it) }
            is OnMtuChanged -> onEvent(it)
            is OnPhyRead -> onEvent(it)
            is OnPhyUpdate -> onEvent(it)
            is OnReadRemoteRssi -> onEvent(it)
            is OnServiceChanged -> onEvent(it)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readRemoteRssi() {
        gatt?.readRemoteRssi()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun readPhy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt?.readPhy()
        } else {
            _event.tryEmit(OnPhyRead(gatt, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun requestPhy(txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt?.setPreferredPhy(txPhy.value, rxPhy.value, phyOption.value)
        } else {
            _event.tryEmit(OnPhyUpdate(gatt, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal suspend fun connect(
        context: Context,
        options: BleGattConnectOptions,
        device: BluetoothDevice
    ) = suspendCoroutine { continuation ->
        onConnectionStateChangedCallback = { connectionState, status ->
            Log.d("AAATESTAAA", "State: $connectionState, Status: $status")
            if (connectionState == GattConnectionState.STATE_CONNECTED) {
                continuation.resume(Unit)
            } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
                continuation.resumeWithException(DeviceDisconnectedException(status))
            }
            onConnectionStateChangedCallback = null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.connectGatt(context, options.autoConnect, gattProxy, BluetoothDevice.TRANSPORT_LE, options.getPhy())
        } else {
            device.connectGatt(context, options.autoConnect, gattProxy)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onConnectionStateChange(gatt: BluetoothGatt?, status: BleGattOperationStatus, newState: Int) {
        val connectionState = GattConnectionState.create(newState)
        _connection.value = _connection.value.copy(connectionState = connectionState)
        gatt?.let { this.gatt = it }
        onConnectionStateChangedCallback?.invoke(connectionState, status)

        if (connectionState == GattConnectionState.STATE_CONNECTED) {
            gatt?.discoverServices()
        }
    }

    private fun onServicesDiscovered(gatt: BluetoothGatt?, status: BleGattOperationStatus) {
        //TODO inject?
        val services = gatt?.services?.let { BleGattServices(this, it) }
        _connection.value = _connection.value.copy(services = services)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeCharacteristic(characteristic, value, writeType.value)
        } else {
            characteristic.writeType = writeType.value
            characteristic.value = value
            gatt?.writeCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt?.readCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        gatt?.setCharacteristicNotification(characteristic, true)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        gatt?.setCharacteristicNotification(characteristic, false)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeDescriptor(descriptor, value)
        } else {
            descriptor.value = value
            gatt?.writeDescriptor(descriptor)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        gatt?.readDescriptor(descriptor)
    }

    private fun onEvent(event: OnMtuChanged) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(mtu = event.mtu))
    }

    private fun onEvent(event: OnPhyRead) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(txPhy = event.txPhy, rxPhy = event.rxPhy))
    }

    private fun onEvent(event: OnPhyUpdate) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(txPhy = event.txPhy, rxPhy = event.rxPhy))
    }

    private fun onEvent(event: OnReadRemoteRssi) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(rssi = event.rssi))
    }

    @SuppressLint("MissingPermission")
    private fun onEvent(event: OnServiceChanged) {
        gatt?.discoverServices()
    }
}
