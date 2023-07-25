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

package no.nordicsemi.android.kotlin.ble.core.data

/**
 * Permissions representing read/write operation restrictions.
 *
 * @property value Native Android value.
 */
enum class BleGattPermission(internal val value: Int) {

    /**
     * Descriptor read permission.
     */
    PERMISSION_READ(1),

    /**
     * Descriptor permission: Allow encrypted read operations.
     */
    PERMISSION_READ_ENCRYPTED(2),

    /**
     * Descriptor permission: Allow encrypted read operations.
     */
    PERMISSION_READ_ENCRYPTED_MITM(4),

    /**
     * Descriptor write permission.
     */
    PERMISSION_WRITE(16),

    /**
     * Descriptor permission: Allow encrypted writes.
     */
    PERMISSION_WRITE_ENCRYPTED(32),

    /**
     * Descriptor permission: Allow encrypted writes with person-in-the-middle protection.
     */
    PERMISSION_WRITE_ENCRYPTED_MITM(64),

    /**
     * Descriptor permission: Allow signed write operations.
     */
    PERMISSION_WRITE_SIGNED(128),

    /**
     * Descriptor permission: Allow signed write operations with person-in-the-middle protection.
     */
    PERMISSION_WRITE_SIGNED_MITM(256);

    companion object {

        /**
         * Creates all permissions encoded in [Int] value.
         *
         * @param permissions [Int] value where each permission is represented by a separate bit.
         * @return [List] of permissions. The list may be empty.
         */
        fun createPermissions(permissions: Int): List<BleGattPermission> {
            return values().filter { (it.value and permissions) > 0 }
        }

        /**
         * Creates a single permission from [Int] value.
         *
         * @throws IllegalStateException when permission cannot be decoded.
         *
         * @param value [Int] value of a permission.
         * @return Decoded permission.
         */
        fun create(value: Int): BleGattPermission {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalStateException("Cannot create permission for value: $value")
        }

        /**
         * Decodes permissions into single [Int] value.
         *
         * @param permissions [List] of permissions to be encoded.
         * @return Single [Int] value representing all the permissions.
         */
        fun toInt(permissions: List<BleGattPermission>): Int {
            return permissions.fold(0) { current, next ->
                current or next.value
            }
        }
    }
}
