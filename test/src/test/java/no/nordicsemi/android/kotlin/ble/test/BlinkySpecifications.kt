package no.nordicsemi.android.kotlin.ble.test

import java.util.UUID

object BlinkySpecifications {
    /** Nordic Blinky Service UUID. */
    val UUID_SERVICE_DEVICE: UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123")

    /** LED characteristic UUID. */
    val UUID_LED_CHAR: UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123")

    /** BUTTON characteristic UUID. */
    val UUID_BUTTON_CHAR: UUID = UUID.fromString("00001524-1212-efde-1523-785feabcd123")

    /** Notification descriptor UUID. */
    val NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
