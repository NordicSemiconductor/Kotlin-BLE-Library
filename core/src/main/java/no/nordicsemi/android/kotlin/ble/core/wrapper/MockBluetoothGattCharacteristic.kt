/*
 * Copyright (c) 2023, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.core.wrapper

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.UUID

/**
 * Mock variant of the characteristic. It's special feature is that it is independent from
 * Android dependencies and can be used for unit testing.
 *
 * Circular dependency between characteristic and descriptor results in custom [equals] and
 * [hashCode] implementations.
 */
data class MockBluetoothGattCharacteristic private constructor(
    override val uuid: UUID,
    override val permissions: Int,
    override val properties: Int,
    override val instanceId: Int, //TODO check if instance id should change during copy()
    override var value: DataByteArray,
    private var _descriptors: List<IBluetoothGattDescriptor>,
    override var writeType: Int,
) : IBluetoothGattCharacteristic {

    constructor(uuid: UUID, permissions: Int, properties: Int, value: DataByteArray) : this(
        uuid,
        permissions,
        properties,
        InstanceIdGenerator.nextValue(),
        value,
        emptyList(),
        BleWriteType.DEFAULT.value
    )

    override val descriptors: List<IBluetoothGattDescriptor>
        get() = _descriptors

    /**
     * Adds descriptor to this characteristic. It can't be passed in the constructor, because an
     * instance of a characteristics needs to be passed to descriptor.
     *
     * @param descriptor A descriptor to add.
     */
    fun addDescriptor(descriptor: IBluetoothGattDescriptor) {
        _descriptors = _descriptors + descriptor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockBluetoothGattCharacteristic

        if (uuid != other.uuid) return false
        if (permissions != other.permissions) return false
        if (properties != other.properties) return false
        if (instanceId != other.instanceId) return false
        if (value != other.value) return false
        if (_descriptors != other._descriptors) return false
        if (descriptors != other.descriptors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + permissions
        result = 31 * result + properties
        result = 31 * result + instanceId
        result = 31 * result + value.hashCode()
        result = 31 * result + _descriptors.hashCode()
        result = 31 * result + descriptors.hashCode()
        return result
    }
}
