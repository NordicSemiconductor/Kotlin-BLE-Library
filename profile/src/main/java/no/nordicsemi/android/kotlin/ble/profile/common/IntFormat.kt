package no.nordicsemi.android.kotlin.ble.profile.common

enum class IntFormat(valueFormat: ValueFormat) {
    FORMAT_UINT8(ValueFormat.FORMAT_UINT8),
    FORMAT_UINT16_LE(ValueFormat.FORMAT_UINT16_LE),
    FORMAT_UINT16_BE(ValueFormat.FORMAT_UINT16_BE),
    FORMAT_UINT24_LE(ValueFormat.FORMAT_UINT24_LE),
    FORMAT_UINT24_BE(ValueFormat.FORMAT_UINT24_BE),
    FORMAT_UINT32_LE(ValueFormat.FORMAT_UINT32_LE),
    FORMAT_UINT32_BE(ValueFormat.FORMAT_UINT32_BE),
    FORMAT_SINT8(ValueFormat.FORMAT_SINT8),
    FORMAT_SINT16_LE(ValueFormat.FORMAT_SINT16_LE),
    FORMAT_SINT16_BE(ValueFormat.FORMAT_SINT16_BE),
    FORMAT_SINT24_LE(ValueFormat.FORMAT_SINT24_LE),
    FORMAT_SINT24_BE(ValueFormat.FORMAT_SINT24_BE),
    FORMAT_SINT32_LE(ValueFormat.FORMAT_SINT32_LE),
    FORMAT_SINT32_BE(ValueFormat.FORMAT_SINT32_BE);

    val value: Int = valueFormat.value
}

/**
 * Returns the size of a give value type.
 */
internal fun getTypeLen(formatType: IntFormat): Int {
    return formatType.value and 0xF
}
