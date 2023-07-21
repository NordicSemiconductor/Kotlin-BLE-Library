package no.nordicsemi.android.kotlin.ble.core.splitter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

fun Flow<ByteArray>.splitWithMtu(mtu: Int) {
    this.flatMapConcat {
        flow {
            it.split(mtu).forEach {
                emit(it)
            }
        }
    }
}

/**
 * Splits [ByteArray] into elements which satisfies [size] requirement.
 *
 * @param size Maximum size of a chunk.
 * @return [List] containing chunked elements.
 */
fun ByteArray.split(size: Int): List<ByteArray> {
    return this.asList().chunked(size).map { it.toByteArray() }
}
