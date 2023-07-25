package no.nordicsemi.android.kotlin.ble.client.main.errors

/**
 * Sealed class grouping GATT exceptions.
 *
 * @property message Display message describing a problem
 */
sealed class GattException(override val message: String) : Exception()
