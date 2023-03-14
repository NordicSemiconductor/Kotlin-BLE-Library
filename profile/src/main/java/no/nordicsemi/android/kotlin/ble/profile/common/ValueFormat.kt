package no.nordicsemi.android.kotlin.ble.profile.common

enum class ValueFormat(val value: Int) {
    /**
     * Data value format type uint8
     */
    FORMAT_UINT8(0x11),

    /**
     * Data value format type uint16
     */
    @Deprecated("")
    FORMAT_UINT16(0x12),
    FORMAT_UINT16_LE(0x12),
    FORMAT_UINT16_BE(0x112),

    /**
     * Data value format type uint24
     */
    @Deprecated("")
    FORMAT_UINT24(0x13),
    FORMAT_UINT24_LE(0x13),
    FORMAT_UINT24_BE(0x113),

    /**
     * Data value format type uint32
     */
    @Deprecated("")
    FORMAT_UINT32(0x14),
    FORMAT_UINT32_LE(0x14),
    FORMAT_UINT32_BE(0x114),

    /**
     * Data value format type sint8
     */
    FORMAT_SINT8(0x21),

    /**
     * Data value format type sint16
     */
    @Deprecated("")
    FORMAT_SINT16(0x22),
    FORMAT_SINT16_LE(0x22),
    FORMAT_SINT16_BE(0x122),

    /**
     * Data value format type sint24
     */
    @Deprecated("")
    FORMAT_SINT24(0x23),
    FORMAT_SINT24_LE(0x23),
    FORMAT_SINT24_BE(0x123),

    /**
     * Data value format type sint32
     */
    @Deprecated("")
    FORMAT_SINT32(0x24),
    FORMAT_SINT32_LE(0x24),
    FORMAT_SINT32_BE(0x124),

    /**
     * Data value format type sfloat (16-bit float, IEEE-11073)
     */
    FORMAT_SFLOAT(0x32),

    /**
     * Data value format type float (32-bit float, IEEE-11073)
     */
    FORMAT_FLOAT(0x34)
}

/**
 * Returns the size of a give value type.
 */
internal fun getTypeLen(formatType: ValueFormat): Int {
    return formatType.value and 0xF
}
