package no.nordicsemi.android.kotlin.ble.profile.hts.data

enum class TemperatureUnit(private val value: Int) {
    CELSIUS(0),
    FAHRENHEIT(1);

    companion object {
        fun create(value: Int): TemperatureUnit? {
            return values().firstOrNull { it.value == value }
        }
    }
}
