package no.nordicsemi.android.kotlin.ble.server.main.service

import no.nordicsemi.android.common.core.DataByteArray

fun DataByteArray.getChunk(offset: Int, mtu: Int): DataByteArray {
    val maxSize = mtu - 3
    val sizeLeft = this.size - offset
    return if (sizeLeft > 0) {
        if (sizeLeft > maxSize) {
            this.copyOfRange(offset, offset + maxSize)
        } else {
            this.copyOfRange(offset, this.size)
        }
    } else {
        DataByteArray()
    }
}

fun ByteArray.toReadableString(): String {
    return this.joinToString(separator = "-") { eachByte -> "%02x".format(eachByte) }
}
