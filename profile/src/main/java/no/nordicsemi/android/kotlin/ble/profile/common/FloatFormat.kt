package no.nordicsemi.android.kotlin.ble.profile.common

enum class FloatFormat(valueFormat: ValueFormat) {
    FORMAT_FLOAT(ValueFormat.FORMAT_FLOAT),
    FORMAT_SFLOAT(ValueFormat.FORMAT_SFLOAT);

    val value: Int = valueFormat.value
}

/**
 * Returns the size of a give value type.
 */
internal fun getTypeLen(formatType: FloatFormat): Int {
    return formatType.value and 0xF
}
