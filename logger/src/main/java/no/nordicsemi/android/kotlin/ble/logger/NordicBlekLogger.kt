/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.logger

import android.content.Context
import android.util.Log
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.Logger

/**
 * Helper class implementing [BlekLoggerAndLauncher] responsible for printing logs to console and
 * nRF Logger and responsible for starting nRF Logger in most basic manner.
 *
 * @property context An application context.
 * @property key The session key, which is used to group sessions.
 * @param profile Application profile which will be concatenated to the application name.
 * @param name The human readable session name.
 */
class NordicBlekLogger private constructor(
    private val context: Context,
    profile: String?,
    private val key: String,
    name: String?
) : BlekLoggerAndLauncher {
    private val logSession = Logger.newSession(context, profile, key, name)

    override fun log(priority: Int, log: String) {
        Log.println(priority, key, log)
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), log)
    }

    override fun launch() {
        LoggerLauncher.launch(context, logSession?.sessionUri)
    }

    companion object {

        fun create(context: Context, profile: String?, key: String, name: String?): BlekLoggerAndLauncher {
            return NordicBlekLogger(context, profile, key, name)
        }
    }
}
