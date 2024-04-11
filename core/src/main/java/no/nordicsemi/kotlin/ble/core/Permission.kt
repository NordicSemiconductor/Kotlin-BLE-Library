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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.core

/**
 * Permissions representing read/write operation restrictions.
 */
enum class Permission {
    /** Reading the value is permitted without encryption. */
    READ,

    /** Reading the value required encryption without authorization. */
    READ_ENCRYPTED,

    /**
     * Reading the value requires encryption with authorization, that is a PIN, passcode, etc.
     *
     * MITM stands for "Man In The Middle", which means that some part of the bonding process
     * must be performed Out Of Band (OOB).
     */
    READ_ENCRYPTED_MITM,

    /** Writing is permitted without encryption. */
    WRITE,

    /** Writing is permitted, but requires encryption without authorization. */
    WRITE_ENCRYPTED,

    /**
     * Writing the value requires encryption with authorization, that is a PIN, passcode, etc.
     *
     * MITM stands for "Man In The Middle", which means that some part of the bonding process
     * must be performed Out Of Band (OOB).
     */
    WRITE_ENCRYPTED_MITM,

    /** Signed write operations are allowed. */
    WRITE_SIGNED,

    /** Allow signed write operations with person-in-the-middle protection. */
    PERMISSION_WRITE_SIGNED_MITM;
}

infix fun Permission.and(permission: Permission): List<Permission> {
    return listOf(this, permission)
}

infix fun List<Permission>.and(permission: Permission): List<Permission> {
    return this + permission
}
