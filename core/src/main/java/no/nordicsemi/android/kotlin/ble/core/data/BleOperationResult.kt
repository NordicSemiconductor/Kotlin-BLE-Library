package no.nordicsemi.android.kotlin.ble.core.data

import android.util.Log

sealed interface BleOperationResult<T> {

    companion object {
        fun createEmptyResult(status: BleGattOperationStatus): BleOperationResult<Unit> {
            return createResult(Unit, status)
        }

        fun <T> createResult(data: T, status: BleGattOperationStatus): BleOperationResult<T> {
            return if(status.isSuccess) {
                BleSuccessResult(data)
            } else {
                BleErrorResult(status)
            }
        }
    }
}

data class BleSuccessResult<T>(val data: T) : BleOperationResult<T>

data class BleErrorResult<T>(val status: BleGattOperationStatus) : BleOperationResult<T>

fun <T> BleOperationResult<T>.toLogLevel(): Int {
    return when (this) {
        is BleSuccessResult -> Log.DEBUG
        is BleErrorResult -> Log.ERROR
    }
}
