package no.nordicsemi.android.kotlin.ble.core.mutex

enum class RequestedLockedFeature {
    CONNECTION,
    BONDING,
    MTU,
    PHY_READ,
    PHY_UPDATE,
    READ_REMOTE_RSSI,
    DESCRIPTOR_WRITE,
    DESCRIPTOR_READ,
    CHARACTERISTIC_WRITE,
    CHARACTERISTIC_READ,
    SERVICES_DISCOVERED
}
