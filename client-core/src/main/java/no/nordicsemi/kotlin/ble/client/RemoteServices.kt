package no.nordicsemi.kotlin.ble.client

import kotlin.uuid.Uuid

/**
 * State of the service discovery process.
 */
sealed class RemoteServices {

    /**
     * Remote services are unknown.
     *
     * The service discovery might not have been started initiated, i.e. due to the peripheral not
     * being connected. This state is also reported when the peripheral invalidates its services
     * using Service Changed indication, in which case the state should change to [Discovering]
     * and then to [Discovered] again.
     *
     * @see Peripheral.services
     */
    data object Unknown : RemoteServices()

    /**
     * Remote services are being discovered.
     */
    data object Discovering : RemoteServices()

    /**
     * Remote services have been discovered.
     *
     * This list contains filtered list of services if [Peripheral.services] was called with a
     * UUID filter.
     *
     * @param services The list of discovered services matching the filter.
     */
    data class Discovered(val services: List<RemoteService>) : RemoteServices()

    /**
     * Remote services discovery has failed.
     *
     * @param reason The reason of the failure.
     */
    data class Failed(val reason: Reason) : RemoteServices() {

        /**
         * The reason of service discovery failure.
         */
        sealed class Reason {
            /** Service discovery returned empty list of services. */
            data object EmptyResult : Reason()
            /** Service discovery finished with a different status than SUCCESS. */
            data class Unknown(val status: Int) : Reason()
        }
    }

    /**
     * Invalidates the discovered services.
     */
    internal fun invalidate() {
        if (this is Discovered) {
            services.forEach { it.owner = null }
        }
    }

    /**
     * Returns filtered service discovery state.
     */
        internal fun filteredBy(uuids: List<Uuid>): RemoteServices = when (this) {
        is Discovered -> Discovered(
            services = services.filter { service ->
                uuids.any { it == service.uuid }
            }
        )
        else -> this
    }

}