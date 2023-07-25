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
import android.content.Intent
import android.net.Uri

private const val LOGGER_PACKAGE_NAME = "no.nordicsemi.android.log"
private const val LOGGER_LINK = "https://play.google.com/store/apps/details?id=no.nordicsemi.android.log"

/**
 * Helper object responsible for launching nRF Logger app.
 */
object LoggerLauncher {

    /**
     * Opens the log session in nRF Logger app, or opens Google Play if the app is not installed.
     */
    fun launch(context: Context, sessionUri: Uri? = null) {
        val packageManger = context.packageManager

        if (packageManger.getLaunchIntentForPackage(LOGGER_PACKAGE_NAME) != null && sessionUri != null) {
            openLauncher(context, sessionUri)
        } else try {
            openGooglePlay(context)
        } catch (e: Exception) {
            e.printStackTrace() //Google Play not installed
        }
    }

    private fun openLauncher(context: Context, sessionUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, sessionUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.let { context.startActivity(intent) }
    }

    private fun openGooglePlay(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LOGGER_LINK))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
