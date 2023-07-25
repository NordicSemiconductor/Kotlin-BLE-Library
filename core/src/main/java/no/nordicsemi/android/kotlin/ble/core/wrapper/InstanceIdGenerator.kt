package no.nordicsemi.android.kotlin.ble.core.wrapper

/**
 * Auxiliary class to generate instance id values. It is used to generate instance ids for mock
 * variants.
 */
internal object InstanceIdGenerator {

    /**
     * Next instance id value. It is initialized with [Int.MAX_VALUE] to not collide with native
     * Android instance ids which increments from low values.
     */
    private var value = Int.MAX_VALUE

    /**
     * Generates instance id.
     *
     * @return New instance id.
     */
    fun nextValue(): Int {
        return value--
    }
}
