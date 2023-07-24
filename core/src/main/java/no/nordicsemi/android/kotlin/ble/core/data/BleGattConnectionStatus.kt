package no.nordicsemi.android.kotlin.ble.core.data

/**
 * Possible statuses of BLE connection.
 *
 * @property value Native Android API value.
 */
enum class BleGattConnectionStatus(internal val value: Int) {

    /**
     * Unknown error.
     */
    UNKNOWN(-1),

    /** The disconnection was initiated by the user.  */
    SUCCESS(0),

    /** The local device initiated disconnection.  */
    TERMINATE_LOCAL_HOST(1),

    /** The remote device initiated graceful disconnection.  */
    TERMINATE_PEER_USER(2),

    /**
     * This reason will only be reported when [ConnectRequest.useAutoConnect]} was
     * called with parameter set to true, and connection to the device was lost for any reason
     * other than graceful disconnection initiated by the peer user.
     *
     * Android will try to reconnect automatically.
     */
    LINK_LOSS(3),

    /** The device does not have required services.  */
    NOT_SUPPORTED(4),

    /** Connection attempt was cancelled.  */
    CANCELLED(5),

    /**
     * The connection timed out. The device might have reboot, is out of range, turned off
     * or doesn't respond for another reason.
     */
    TIMEOUT(10);

    val isLinkLoss
        get() = this != SUCCESS && this != TERMINATE_PEER_USER

    companion object {
        fun create(value: Int): BleGattConnectionStatus {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
