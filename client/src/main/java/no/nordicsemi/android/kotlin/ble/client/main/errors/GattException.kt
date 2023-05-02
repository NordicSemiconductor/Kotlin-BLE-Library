package no.nordicsemi.android.kotlin.ble.client.main.errors

sealed class GattException(override val message: String) : Exception()
