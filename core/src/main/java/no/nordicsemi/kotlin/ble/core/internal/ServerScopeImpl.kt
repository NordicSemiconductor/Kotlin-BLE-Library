/*
 * Copyright (c) 2024, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.kotlin.ble.core.internal

import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.CharacteristicScope
import no.nordicsemi.kotlin.ble.core.Descriptor
import no.nordicsemi.kotlin.ble.core.Permission
import no.nordicsemi.kotlin.ble.core.ServerScope
import no.nordicsemi.kotlin.ble.core.ServiceScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ServerScopeImpl: ServerScope {
    private val _services = mutableListOf<ServiceDefinition>()

    /**
     * Builds the list of services from the scope.
     */
    fun build(): List<ServiceDefinition> = _services.toList()

    override fun Service(uuid: Uuid, builder: ServiceScope.() -> Unit) {
        ServiceScopeImpl()
            .apply(builder)
            .let { scope ->
                _services.add(
                    ServiceDefinition(
                        uuid = uuid,
                        characteristics = scope.characteristics,
                        innerServices = scope.innerServices
                    )
                )
            }
    }
}

@OptIn(ExperimentalUuidApi::class)
private open class ServiceScopeImpl: ServiceScope {
    val characteristics = mutableListOf<CharacteristicDefinition>()
    val innerServices = mutableListOf<ServiceDefinition>()

    override fun Characteristic(
        uuid: Uuid,
        properties: List<CharacteristicProperty>,
        permissions: List<Permission>,
        builder: CharacteristicScope.() -> Unit
    ): Unit = CharacteristicScopeImpl()
        .apply(builder)
        .let { scope ->
            // Add CCCD (Client Characteristic Configuration Descriptor) if NOTIFY or INDICATE is
            // supported and it wasn't added by the user.
            if ((properties.contains(CharacteristicProperty.NOTIFY) || properties.contains(CharacteristicProperty.INDICATE))
                && scope.descriptors.none { it.uuid == Descriptor.CLIENT_CHAR_CONF_UUID }) {
                scope.descriptors.add(CCCD())
            }
            print("AAA Adding characteristic: $uuid")
            characteristics.add(
                CharacteristicDefinition(
                    uuid = uuid,
                    properties = properties,
                    permissions = permissions,
                    descriptors = scope.descriptors
                )
            )
        }

    override fun IncludedService(
        uuid: Uuid,
        builder: ServiceScope.() -> Unit
    ) {
        ServiceScopeImpl()
            .apply(builder)
            .let { scope ->
                innerServices.add(
                    ServiceDefinition(
                        uuid = uuid,
                        characteristics = scope.characteristics,
                        innerServices = scope.innerServices,
                    )
                )
            }
    }
}

@OptIn(ExperimentalUuidApi::class)
private class CharacteristicScopeImpl: CharacteristicScope {
    val descriptors = mutableListOf<DescriptorDefinition>()

    override fun Descriptor(uuid: Uuid, permissions: List<Permission>) {
        descriptors.add(DescriptorDefinition(uuid, permissions))
    }

    override fun CharacteristicUserDescriptionDescriptor(description: String, writable: Boolean) {
        descriptors.add(CUD(description, writable))
    }
}

