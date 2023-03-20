package no.nordicsemi.android.kotlin.ble.profile.prx

enum class AlarmLevel(internal val value: Int) {
    NONE(0x00),
    MEDIUM(0x01),
    HIGH(0x02);

    companion object {
        internal fun create(value: Int): AlarmLevel {
            return AlarmLevel.values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot find AlarmLevel for provided value: $value")
        }
    }
}
