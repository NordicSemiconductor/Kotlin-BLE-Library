package no.nordicsemi.android.kotlin.ble.core.wrapper

internal object InstanceIdGenerator {

    //To not collide with Android native
    var value = Int.MAX_VALUE

    fun nextValue(): Int {
        return value--
    }
}
