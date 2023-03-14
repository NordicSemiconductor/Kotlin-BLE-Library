package no.nordicsemi.android.kotlin.ble.profile.common

enum class LongFormat(valueFormat: ValueFormat) {
    FORMAT_UINT32_LE(ValueFormat.FORMAT_UINT32_LE),
    FORMAT_UINT32_BE(ValueFormat.FORMAT_UINT32_BE),
    FORMAT_SINT32_LE(ValueFormat.FORMAT_SINT32_LE),
    FORMAT_SINT32_BE(ValueFormat.FORMAT_SINT32_BE);

    val value: Int = valueFormat.value
}

/**
 * Returns the size of a give value type.
 */
internal fun getTypeLen(formatType: LongFormat): Int {
    return formatType.value and 0xF
}
