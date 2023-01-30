package no.nordicsemi.android.kotlin.ble.server.nativ.service

fun ByteArray.getChunk(offset: Int, mtu: Int): ByteArray {
    val maxSize = mtu - 3
    val sizeLeft = this.size - offset
    return if (sizeLeft > 0) {
        if (sizeLeft > maxSize) {
            this.copyOfRange(offset, offset + maxSize)
        } else {
            this.copyOfRange(offset, this.size)
        }
    } else {
        byteArrayOf()
    }
}

fun ByteArray.toReadableString(): String {
    return this.joinToString(separator = "-") { eachByte -> "%02x".format(eachByte) }
}
