package no.nordicsemi.android.kotlin.ble.core.ext

fun ByteArray.toDisplayString(): String {
    return "(0x) " + this.joinToString(":") {
        "%02x".format(it).uppercase()
    }
}
