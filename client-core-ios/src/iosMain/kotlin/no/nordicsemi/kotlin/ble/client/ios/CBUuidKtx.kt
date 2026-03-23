package no.nordicsemi.kotlin.ble.client.ios

import platform.CoreBluetooth.CBUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val BLUETOOTH_BASE_UUID_SUFFIX = "-0000-1000-8000-00805F9B34FB"

@OptIn(ExperimentalUuidApi::class)
fun CBUUID.toKotlinUuid(): Uuid = UUIDString.toBluetoothUuid()

@OptIn(ExperimentalUuidApi::class)
fun String.toBluetoothUuid(): Uuid = Uuid.parse(toBluetoothUuidString())

internal fun String.toBluetoothUuidString(): String {
    val normalized = uppercase()
    return when (normalized.length) {
        4 -> "0000$normalized$BLUETOOTH_BASE_UUID_SUFFIX"
        8 -> "$normalized$BLUETOOTH_BASE_UUID_SUFFIX"
        else -> normalized
    }
}
