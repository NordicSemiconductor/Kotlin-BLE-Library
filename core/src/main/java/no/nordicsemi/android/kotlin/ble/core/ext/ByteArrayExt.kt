package no.nordicsemi.android.kotlin.ble.core.ext

fun ByteArray.toDisplayString(): String {
    return this.joinToString(":") {
        "%02x".format(it).uppercase()
    }
}
